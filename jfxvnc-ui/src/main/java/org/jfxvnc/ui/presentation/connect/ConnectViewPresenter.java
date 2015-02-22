package org.jfxvnc.ui.presentation.connect;

/*
 * #%L
 * jfxvnc-ui
 * %%
 * Copyright (C) 2015 comtel2000
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.net.URL;
import java.util.ResourceBundle;

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

import javax.inject.Inject;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.net.rfb.codec.security.ISecurityType;
import org.jfxvnc.ui.persist.HistoryEntry;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.service.SecurityType;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

public class ConnectViewPresenter implements Initializable {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConnectViewPresenter.class);

    @Inject
    SessionContext ctx;

    @Inject
    VncRenderService con;

    @Inject
    ProtocolConfiguration prop;

    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private CheckBox sslCB;
    @FXML
    private CheckBox sharedCB;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

	clearBtn.setOnAction((a) -> historyList.getItems().clear());

	securityCombo.getItems().addAll(FXCollections.observableArrayList(SecurityType.values()));
	securityCombo.getSelectionModel().selectedItemProperty().addListener((l, a, b) -> {
	    prop.securityProperty().set(b != null ? b.getType() : 0);
	});
	
	pwdField.disableProperty().bind(Bindings.equal(SecurityType.NONE, securityCombo.getSelectionModel().selectedItemProperty()));
	
	prop.hostProperty().bind(ipField.textProperty());
	StringConverter<Number> converter = new NumberStringConverter();
	Bindings.bindBidirectional(portField.textProperty(), prop.portProperty(), converter);
	prop.passwordProperty().bind(pwdField.textProperty());
	prop.sslProperty().bind(sslCB.selectedProperty());
	prop.sharedProperty().bind(sharedCB.selectedProperty());

	prop.rawEncProperty().bind(rawCB.selectedProperty());
	prop.copyRectEncProperty().bind(copyrectCB.selectedProperty());
	prop.hextileEncProperty().bind(hextileCB.selectedProperty());

	prop.clientCursorProperty().bind(cursorCB.selectedProperty());
	prop.desktopSizeProperty().bind(desktopCB.selectedProperty());

	portField.textProperty().addListener((observable, oldValue, newValue) -> {
	    if (newValue != null && !newValue.isEmpty() && !newValue.matches("[0-9]+")) {
		if (!oldValue.matches("[0-9]+")) {
		    ((StringProperty) observable).setValue("");
		    return;
		}
		((StringProperty) observable).setValue(oldValue);
	    }
	});

	con.connectProperty().addListener((l) -> Platform.runLater(() -> ipField.getParent().setDisable(con.connectProperty().get())));

	ctx.bind(ipField.textProperty(), "hostField");
	ctx.bind(portField.textProperty(), "portField");
	ctx.bind(userField.textProperty(), "userField");
	ctx.bind(pwdField.textProperty(), "pwdField");
	ctx.bind(securityCombo, "authType");
	ctx.bind(sslCB.selectedProperty(), "useSSL");
	ctx.bind(sharedCB.selectedProperty(), "useSharedView");
	ctx.bind(rawCB.selectedProperty(), "useRAW");
	ctx.bind(copyrectCB.selectedProperty(), "useCopyRect");
	ctx.bind(hextileCB.selectedProperty(), "useHextile");
	ctx.bind(cursorCB.selectedProperty(), "useCursor");
	ctx.bind(desktopCB.selectedProperty(), "useDesktopSize");

	if (securityCombo.getSelectionModel().getSelectedIndex() < 0) {
	    securityCombo.getSelectionModel().select(SecurityType.VNC_Auth);
	}

	historyList.getSelectionModel().selectedItemProperty().addListener((l, a, b) -> updateData(b));

	con.detailsProperty().addListener((l) -> Platform.runLater(() -> {
	    if (con.detailsProperty().get() != null) {
		saveHistoryEntry(con.detailsProperty().get().getServerName());
	    }
	}));

    }

    private void updateData(HistoryEntry e) {
	if (e == null) {
	    return;
	}
	ipField.setText(e.getHost());
	portField.setText(Integer.toString(e.getPort()));
	pwdField.setText(e.getPassword());
	securityCombo.getSelectionModel().select(SecurityType.getValueByType(e.getSecurityType()));

    }

    private void saveHistoryEntry(String serverName) {
	HistoryEntry e = new HistoryEntry(prop.hostProperty().get(), prop.portProperty().get());
	if (!historyList.getItems().contains(e)) {
	    historyList.getItems().add(e);
	} else {
	    for (HistoryEntry entry : historyList.getItems()) {
		if (entry.equals(e)) {
		    e = entry;
		    break;
		}
	    }
	}
	e.setPassword(prop.passwordProperty().get());
	e.setSecurityType(prop.securityProperty().get());
	e.setServerName(serverName);
    }

}
