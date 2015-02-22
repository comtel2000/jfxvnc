package org.jfxvnc.ui.presentation.info;

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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;

import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.StringUtils;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

public class InfoViewPresenter implements Initializable {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InfoViewPresenter.class);

    @Inject
    SessionContext ctx;
    @Inject
    VncRenderService con;

    @FXML
    private Label infoName;
    @FXML
    private Label infoHost;
    @FXML
    private Label infoPixelformat;
    @FXML
    private Label infoPixelformatDef;
    @FXML
    private Label infoEncoding;
    @FXML
    private Label infoProtocol;
    @FXML
    private Label infoSecurity;
    @FXML
    private Label infoConnectType;
    @FXML
    private Label infoSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

	con.detailsProperty().addListener((l) -> Platform.runLater(() -> updateDetails(con.detailsProperty().get())));
	con.connectProperty().addListener((l) -> Platform.runLater(() -> infoName.getParent().setDisable(!con.connectProperty().get())));
    }

    private void updateDetails(ConnectInfoEvent cd) {
	if (cd == null) {
	    reset();
	    return;
	}
	infoName.setText(cd.getServerName());
	infoSize.setText(String.format("%s x %s", cd.getFrameWidth(), cd.getFrameHeight()));
	infoProtocol.setText(cd.getRfbProtocol());
	infoHost.setText(cd.getRemoteAddress());
	infoPixelformat.setText(StringUtils.getPixelFormatReadable(cd.getServerPF()));
	infoPixelformatDef.setText(StringUtils.getPixelFormatReadable(cd.getClientPF()));
	infoEncoding.setText(StringUtils.getEncodingNames(cd.getSupportedEncodings()));
	infoSecurity.setText(StringUtils.getSecurityName(cd.getSecurity()));
	infoConnectType.setText(cd.getConnectionType());
    }

    private void reset() {
	logger.debug("reset fields");
	infoName.setText("-");
	infoHost.setText("-");
	infoPixelformat.setText("-");
	infoPixelformatDef.setText("-");
	infoEncoding.setText("-");
	infoProtocol.setText("-");
	infoSecurity.setText("-");
	infoConnectType.setText("-");
	infoSize.setText("-");
    }

}
