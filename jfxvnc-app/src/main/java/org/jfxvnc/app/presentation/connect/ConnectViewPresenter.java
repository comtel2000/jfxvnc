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
package org.jfxvnc.app.presentation.connect;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.jfxvnc.app.persist.HistoryEntry;
import org.jfxvnc.app.persist.SessionContext;
import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.jfxvnc.net.rfb.codec.security.SecurityType;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.ui.service.VncRenderService;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class ConnectViewPresenter implements Initializable {

  @Inject
  SessionContext ctx;

  @Inject
  VncRenderService con;

  @FXML
  private TextField ipField;
  @FXML
  private TextField portField;
  @FXML
  private CheckBox sslCB;
  @FXML
  private CheckBox sharedCB;
  @FXML
  private CheckBox forceRfb33CB;

  @FXML
  private TextField listeningPortField;
  @FXML
  private CheckBox listeningCB;

  @FXML
  private TextField userField;
  @FXML
  private PasswordField pwdField;
  @FXML
  private CheckBox rawCB;
  @FXML
  private CheckBox copyrectCB;
  @FXML
  private CheckBox hextileCB;
  @FXML
  private CheckBox cursorCB;
  @FXML
  private ListView<HistoryEntry> historyList;
  @FXML
  private Button clearBtn;

  @FXML
  ComboBox<SecurityType> securityCombo;

  @FXML
  CheckBox desktopCB;

  @FXML
  CheckBox zlibCB;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    ProtocolConfiguration prop = con.getConfiguration();
    historyList.setItems(ctx.getHistory());

    clearBtn.setOnAction(a -> historyList.getItems().clear());
    securityCombo.getItems().addAll(FXCollections.observableArrayList(SecurityType.NONE, SecurityType.VNC_Auth));
    securityCombo.getSelectionModel().selectedItemProperty().addListener((l, a, b) -> {
      prop.securityProperty().set(b != null ? b : SecurityType.UNKNOWN);
    });

    pwdField.disableProperty().bind(Bindings.equal(SecurityType.NONE, securityCombo.getSelectionModel().selectedItemProperty()));

    prop.hostProperty().bindBidirectional(ipField.textProperty());
    StringConverter<Number> converter = new NumberStringConverter("#");
    Bindings.bindBidirectional(portField.textProperty(), prop.portProperty(), converter);

    prop.passwordProperty().bindBidirectional(pwdField.textProperty());
    prop.sslProperty().bindBidirectional(sslCB.selectedProperty());
    prop.sharedProperty().bindBidirectional(sharedCB.selectedProperty());
    forceRfb33CB.setSelected(prop.versionProperty().get() == ProtocolVersion.RFB_3_3);
    forceRfb33CB.selectedProperty().addListener((l, a, b) -> prop.versionProperty().set(b ? ProtocolVersion.RFB_3_3 : ProtocolVersion.RFB_3_8));
    listeningCB.selectedProperty().bindBidirectional(con.listeningModeProperty());
    Bindings.bindBidirectional(listeningPortField.textProperty(), con.listeningPortProperty(), converter);
    listeningPortField.disableProperty().bind(listeningCB.selectedProperty().not());

    prop.rawEncProperty().bindBidirectional(rawCB.selectedProperty());
    prop.copyRectEncProperty().bindBidirectional(copyrectCB.selectedProperty());
    prop.hextileEncProperty().bindBidirectional(hextileCB.selectedProperty());

    prop.clientCursorProperty().bindBidirectional(cursorCB.selectedProperty());
    prop.desktopSizeProperty().bindBidirectional(desktopCB.selectedProperty());
    prop.zlibEncProperty().bindBidirectional(zlibCB.selectedProperty());

    portField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null && !newValue.isEmpty() && !newValue.matches("[0-9]+")) {
        if (!oldValue.matches("[0-9]+")) {
          ((StringProperty) observable).setValue("");
          return;
        }
        ((StringProperty) observable).setValue(oldValue);
      }
    });

    con.connectingProperty().addListener((l, a, b) -> Platform.runLater(() -> ipField.getParent().setDisable(b)));

    ctx.bind(ipField.textProperty(), "hostField");
    ctx.bind(portField.textProperty(), "portField");
    ctx.bind(userField.textProperty(), "userField");
    ctx.bind(pwdField.textProperty(), "pwdField");
    ctx.bind(securityCombo, "authType");
    ctx.bind(sslCB.selectedProperty(), "useSSL");
    ctx.bind(sharedCB.selectedProperty(), "useSharedView");
    ctx.bind(forceRfb33CB.selectedProperty(), "forceRfb33");
    ctx.bind(listeningCB.selectedProperty(), "listeningMode");
    ctx.bind(listeningPortField.textProperty(), "listeningPortField");

    ctx.bind(rawCB.selectedProperty(), "useRaw");
    ctx.bind(copyrectCB.selectedProperty(), "useCopyRect");
    ctx.bind(hextileCB.selectedProperty(), "useHextile");
    ctx.bind(cursorCB.selectedProperty(), "useCursor");
    ctx.bind(desktopCB.selectedProperty(), "useDesktopSize");
    ctx.bind(zlibCB.selectedProperty(), "useZlib");

    if (securityCombo.getSelectionModel().getSelectedIndex() < 0) {
      securityCombo.getSelectionModel().select(SecurityType.VNC_Auth);
    }

    historyList.getSelectionModel().selectedItemProperty().addListener((l, a, b) -> updateData(b));
    con.connectInfoProperty().addListener((l, a, b) -> Platform.runLater(() -> addToHistory(b)));

  }

  private void updateData(HistoryEntry e) {
    if (e == null) {
      return;
    }
    ipField.setText(e.getHost());
    portField.setText(Integer.toString(e.getPort()));
    pwdField.setText(e.getPassword());
    securityCombo.getSelectionModel().select(SecurityType.valueOf(e.getSecurityType()));

  }

  private void addToHistory(ConnectInfoEvent info) {
    if (info == null) {
      return;
    }
    ProtocolConfiguration prop = con.getConfiguration();
    final HistoryEntry e = new HistoryEntry(prop.hostProperty().get(), prop.portProperty().get());

    Optional<HistoryEntry> opt = historyList.getItems().stream().filter(h -> h.equals(e)).findAny();
    if (!opt.isPresent()) {
      historyList.getItems().add(e);
    }
    opt.orElse(e).setPassword(prop.passwordProperty().get());
    opt.orElse(e).setSecurityType(prop.securityProperty().get().getType());
    opt.orElse(e).setServerName(info.getServerName());
  }

}
