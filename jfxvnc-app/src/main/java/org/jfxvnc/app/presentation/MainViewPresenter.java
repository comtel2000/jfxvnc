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
package org.jfxvnc.app.presentation;


import java.awt.Toolkit;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.jfxvnc.app.persist.SessionContext;
import org.jfxvnc.app.presentation.detail.DetailView;
import org.jfxvnc.app.presentation.vnc.VncView;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.ui.service.VncRenderService;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class MainViewPresenter implements Initializable {

  @Inject
  SessionContext ctx;

  @Inject
  VncRenderService service;

  @FXML
  BorderPane mainPane;

  @FXML
  private Label statusLabel;

  @FXML
  private ProgressIndicator progress;

  @FXML
  private Button connectBtn;

  @FXML
  private Button disconnectBtn;

  @FXML
  private ToggleButton gearBtn;

  @FXML
  private Slider zoomSlider;

  @FXML
  private ToggleButton fullScreenBtn;

  @FXML
  private SplitPane splitPane;

  private volatile long lastPing = 0;


  private final static PseudoClass CONNECT_CLASS = PseudoClass.getPseudoClass("connect");
  private final static PseudoClass ONLINE_CLASS = PseudoClass.getPseudoClass("online");

  private final static PseudoClass WINDOW_CLASS = PseudoClass.getPseudoClass("window");

  private final StringProperty statusProperty = new SimpleStringProperty("-", "mainview.status");

  @Override
  public void initialize(URL location, ResourceBundle rb) {

    ctx.addBinding(statusProperty);

    VncView vncView = new VncView();
    DetailView detailView = new DetailView();

    splitPane.getItems().addAll(vncView.getView(), detailView.getView());
    splitPane.getDividers().get(0).setPosition(1.0);

    statusLabel.textProperty().bind(statusProperty);

    gearBtn.selectedProperty().addListener(l -> {
      SplitPane.Divider divider = splitPane.getDividers().get(0);
      KeyValue value = new KeyValue(divider.positionProperty(), gearBtn.isSelected() ? 0.80 : 1.0);
      new Timeline(new KeyFrame(Duration.seconds(0.2), value)).play();
    });
    gearBtn.setSelected(true);

    connectBtn.textProperty().bind(Bindings.createStringBinding(
        () -> service.listeningModeProperty().get() ? rb.getString("button.listening") : rb.getString("button.connect"), service.listeningModeProperty()));

    disconnectBtn.textProperty().bind(Bindings.createStringBinding(
        () -> service.listeningModeProperty().get() ? rb.getString("button.cancel") : rb.getString("button.disconnect"), service.listeningModeProperty()));
    disconnectBtn.disableProperty().bind(connectBtn.disabledProperty().not());

    fullScreenBtn.selectedProperty().bindBidirectional(service.fullSceenProperty());
    fullScreenBtn.selectedProperty().addListener((l, o, n) -> fullScreenBtn.pseudoClassStateChanged(WINDOW_CLASS, n));

    progress.visibleProperty().bind(service.connectingProperty());
    zoomSlider.valueProperty().bindBidirectional(service.zoomLevelProperty());

    vncView.getView().setOnScroll(e -> service.zoomLevelProperty().set(service.zoomLevelProperty().get() + (e.getDeltaY() > 0.0 ? 0.01 : -0.01)));

    service.zoomLevelProperty()
        .addListener((l, o, z) -> statusProperty.set(MessageFormat.format(rb.getString("status.zoom.scale"), Math.floor(z.doubleValue() * 100))));

    service.protocolStateProperty().addListener((l, o, event) -> Platform.runLater(() -> {
      switch (event) {
        case LISTENING:
          statusProperty.set(rb.getString("status.listening"));
          break;
        case CLOSED:
          statusProperty.set(rb.getString("status.closed"));
          break;
        case HANDSHAKE_STARTED:
          ProtocolConfiguration config = service.getConfiguration();
          statusProperty.set(MessageFormat.format(rb.getString("status.try.connect"), config.hostProperty().get(), config.portProperty().get()));
          break;
        case HANDSHAKE_COMPLETE:
          statusProperty.set(rb.getString("status.open"));
          gearBtn.setSelected(false);
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

    service.connectedProperty().addListener((l, o, n) -> Platform.runLater(() -> connectBtn.setDisable(n)));

    service.connectingProperty().addListener((l, o, n) -> Platform.runLater(() -> gearBtn.pseudoClassStateChanged(CONNECT_CLASS, n)));
    service.onlineProperty().addListener((l, o, n) -> Platform.runLater(() -> gearBtn.pseudoClassStateChanged(ONLINE_CLASS, n)));

    service.exceptionCaughtProperty().addListener((l, o, n) -> Platform.runLater(() -> {
      // Notifications.create().owner(mainPane).position(Pos.TOP_CENTER).text(n.getMessage()).showError();
      statusProperty.set(n.getMessage());
    }));

    service.bellProperty().addListener(l -> bell());

  }

  @FXML
  void connect(ActionEvent event) {
    service.connect();
  }

  @FXML
  void disconnect(ActionEvent event) {
    service.disconnect();
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

}
