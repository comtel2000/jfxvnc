/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package org.jfxvnc.ui.presentation;

import java.awt.Toolkit;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.StatusBar;
import org.controlsfx.tools.Borders;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.presentation.detail.DetailView;
import org.jfxvnc.ui.presentation.vnc.VncView;
import org.jfxvnc.ui.service.VncRenderService;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainViewPresenter implements Initializable {

  @Inject
  SessionContext ctx;

  @Inject
  VncRenderService con;

  @Inject
  ProtocolConfiguration config;

  @FXML
  BorderPane mainPane;

  private volatile long lastPing = 0;

  private MasterDetailPane mdPane;

  private final static PseudoClass CONNECT_CLASS = PseudoClass.getPseudoClass("connect");
  private final static PseudoClass ONLINE_CLASS = PseudoClass.getPseudoClass("online");

  private final static PseudoClass WINDOW_CLASS = PseudoClass.getPseudoClass("window");

  private final StringProperty statusProperty = new SimpleStringProperty("-", "mainview.status");

  @Override
  public void initialize(URL location, ResourceBundle rb) {

    ctx.addBinding(statusProperty);

    mdPane = new MasterDetailPane(Side.RIGHT);
    VncView vncView = new VncView();
    mdPane.setMasterNode(vncView.getView());

    DetailView detailView = new DetailView();
    mdPane.setDetailNode(detailView.getView());
    mdPane.setDividerPosition(0.75);
    ctx.bind(mdPane.dividerPositionProperty(), "detailDividerPosition");
    mdPane.setShowDetailNode(true);

    StatusBar statusBar = new StatusBar();
    statusBar.getStyleClass().add("menu-status-bar");

    mainPane.setCenter(mdPane);
    mainPane.setBottom(statusBar);

    statusBar.textProperty().bind(statusProperty);

    ToggleButton gearButton = new ToggleButton("", new Pane());
    gearButton.setId("menu-settings");
    gearButton.selectedProperty().bindBidirectional(mdPane.showDetailNodeProperty());

    Button connectBtn = new Button();
    connectBtn.textProperty().bind(Bindings.createStringBinding(
        () -> con.listeningModeProperty().get() ? rb.getString("button.listening") : rb.getString("button.connect"), con.listeningModeProperty()));
    connectBtn.setOnAction(a -> con.restart());

    Button disconnectBtn = new Button();
    disconnectBtn.textProperty().bind(Bindings.createStringBinding(
        () -> con.listeningModeProperty().get() ? rb.getString("button.cancel") : rb.getString("button.disconnect"), con.listeningModeProperty()));
    disconnectBtn.disableProperty().bind(connectBtn.disabledProperty().not());
    disconnectBtn.setOnAction(a -> con.cancel());

    ToggleButton switchFullScreen = new ToggleButton("", new Pane());
    switchFullScreen.setId("menu-fullscreen");
    switchFullScreen.selectedProperty().bindBidirectional(con.fullSceenProperty());
    switchFullScreen.selectedProperty().addListener((l, o, n) -> switchFullScreen.pseudoClassStateChanged(WINDOW_CLASS, n));

    ProgressIndicator progressIndicator = new ProgressIndicator(-1);
    progressIndicator.visibleProperty().bind(con.runningProperty());
    progressIndicator.setPrefSize(16, 16);

    PlusMinusSlider zoomSlider = new PlusMinusSlider();
    zoomSlider.setOnValueChanged(e -> con.zoomLevelProperty().set(e.getValue() + 1));

    mdPane.setOnScroll(e -> con.zoomLevelProperty().set(con.zoomLevelProperty().get() + (e.getDeltaY() > 0.0 ? 0.01 : -0.01)));

    con.zoomLevelProperty()
        .addListener((l, o, z) -> statusProperty.set(MessageFormat.format(rb.getString("status.zoom.scale"), Math.floor(z.doubleValue() * 100))));

    statusBar.getRightItems().addAll(progressIndicator, createSpace(10, 20), Borders.wrap(zoomSlider).emptyBorder().buildAll(), createSpace(10, 20),
        switchFullScreen, createSpace(10, 20), connectBtn, disconnectBtn, createSpace(10, 20), gearButton);

    con.protocolStateProperty().addListener((l, o, event) -> Platform.runLater(() -> {
      switch (event) {
        case LISTENING:
          statusProperty.set(rb.getString("status.listening"));
          break;
        case CLOSED:
          statusProperty.set(rb.getString("status.closed"));
          break;
        case HANDSHAKE_STARTED:
          statusProperty.set(MessageFormat.format(rb.getString("status.try.connect"), config.hostProperty().get(), config.portProperty().get()));
          break;
        case HANDSHAKE_COMPLETE:
          statusProperty.set(rb.getString("status.open"));
          gearButton.setSelected(false);
          break;
        case SECURITY_FAILED:
          statusProperty.set(rb.getString("status.auth.failed"));
          break;
        case SECURITY_COMPLETE:
          statusProperty.set(rb.getString("status.auth.done"));
          break;
        default:
          break;
      }

    }));

    con.connectProperty().addListener((l, o, n) -> Platform.runLater(() -> {
      connectBtn.setDisable(n);
      gearButton.pseudoClassStateChanged(CONNECT_CLASS, n);
    }));
    con.onlineProperty().addListener((l, o, n) -> Platform.runLater(() -> gearButton.pseudoClassStateChanged(ONLINE_CLASS, n)));

    con.exceptionCaughtProperty().addListener((l, o, n) -> Platform.runLater(() -> {
      Notifications.create().owner(mainPane).position(Pos.TOP_CENTER).text(n.getMessage()).showError();
      statusProperty.set(n.getMessage());
    }));

    con.bellProperty().addListener(l -> bell());

  }

  private void bell() {
    long time = System.currentTimeMillis();
    if (lastPing > time - 2000) {
      return;
    }
    lastPing = time;
    Toolkit.getDefaultToolkit().beep();
    Platform.runLater(() -> statusProperty.set("Bell"));
  }

  private Pane createSpace(double w, double h) {
    Pane space = new Pane();
    space.setPrefSize(w, h);
    return space;
  }



}
