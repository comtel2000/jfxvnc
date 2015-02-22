package org.jfxvnc.ui.presentation;

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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;

import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.StatusBar;
import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.presentation.detail.DetailView;
import org.jfxvnc.ui.presentation.vnc.VncView;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

public class MainViewPresenter implements Initializable {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MainViewPresenter.class);

    @Inject
    SessionContext ctx;

    @Inject
    VncRenderService con;

    @Inject
    ProtocolConfiguration config;

    @FXML
    BorderPane mainPane;

    private MasterDetailPane mdPane;

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
	statusBar.setId("status-bar");
	
	mainPane.setCenter(mdPane);
	mainPane.setBottom(statusBar);

	statusBar.textProperty().bind(statusProperty);

	ToggleButton gearButton = new ToggleButton("", new Pane());
	gearButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	gearButton.setId("menu-settings");
	gearButton.selectedProperty().bindBidirectional(mdPane.showDetailNodeProperty());

	Button connectBtn = new Button(rb.getString("connect.button"));
	connectBtn.setPrefWidth(100);
	connectBtn.setOnAction((a) -> con.restart());

	Button disconnectBtn = new Button(rb.getString("disconnect.button"));
	disconnectBtn.setPrefWidth(100);
	disconnectBtn.disableProperty().bind(connectBtn.disabledProperty().not());
	disconnectBtn.setOnAction((a) -> con.disconnect());

	ProgressIndicator progressIndicator = new ProgressIndicator(-1);
	progressIndicator.visibleProperty().bind(con.runningProperty());
	progressIndicator.setPrefSize(16, 16);


	
	
	PlusMinusSlider zoomSlider = new PlusMinusSlider();
	zoomSlider.setStyle("-fx-translate-y: 5;");
	zoomSlider.setOnValueChanged((e)->{
	    double zoom = e.getValue() + 1;
	    if (zoom >= con.getMinZoomLevel()) {
		statusProperty.set("zoom: " + (int) Math.floor(zoom * 100) + "%");
		con.zoomLevelProperty().set(zoom);
	    }
	});
	
	
	statusBar.getRightItems().addAll(progressIndicator, createSpace(10, 20), zoomSlider, createSpace(10, 20), connectBtn, disconnectBtn, createSpace(10, 20), gearButton);


	con.protocolStateProperty().addListener((l, o, event) -> Platform.runLater(() -> {
	    switch (event) {
	    case CLOSED:
		statusProperty.set("connection closed");
		break;
	    case HANDSHAKE_STARTED:
		statusProperty.set(String.format("try to connect to %s:%s", config.hostProperty().get(), config.portProperty().get()));
		break;
	    case HANDSHAKE_COMPLETE:
		statusProperty.set("online");
		gearButton.setSelected(false);
		break;
	    case SECURITY_FAILED:
		statusProperty.set("authentication failed");
		break;
	    case SECURITY_COMPLETE:
		statusProperty.set("authentication completed");
		break;
	    default:
		break;
	    }

	}));
	con.connectProperty().addListener((l, o, n) -> Platform.runLater(() -> connectBtn.setDisable(n)));

	con.exceptionCaughtProperty().addListener((l, o, n) -> {
	    Platform.runLater(() -> {
		Notifications.create().owner(mainPane).position(Pos.TOP_CENTER).text(n.getMessage()).showError();
		statusProperty.set(n.getMessage());
		// ExceptionDialog dlg = new ExceptionDialog(n);
		// dlg.setHeaderText("Connection Error");
		// dlg.show();

	    });
	});
    }

    private Pane createSpace(double w, double h) {
	Pane space = new Pane();
	space.setPrefSize(w, h);
	return space;
    }
}
