package org.jfxvnc.ui;

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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.presentation.MainView;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

import com.airhacks.afterburner.injection.Injector;

public class VncClientApp extends Application {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncClientApp.class);

    private final StringProperty headerProperty = new SimpleStringProperty(System.getProperty("javafx.runtime.version"));
    private final StringExpression headerExpr = Bindings.format("JavaFX VNC Viewer (%s)", headerProperty);

    private final DoubleProperty sceneWidthProperty = new SimpleDoubleProperty(1024);
    private final DoubleProperty sceneHeightProperty = new SimpleDoubleProperty(768);

    private Image offlineImg;
    private Image onlineImg;

    @Override
    public void start(Stage stage) throws Exception {

	stage.titleProperty().bind(headerExpr);

	stage.setResizable(true);
	offlineImg = new Image(VncClientApp.class.getResourceAsStream("icon.png"));
	onlineImg = new Image(VncClientApp.class.getResourceAsStream("icon_green.png"));

	Injector.setLogger((t) -> logger.trace(t));

	// Injector.setModelOrService(Stage.class, stage);

	Injector.instantiateModelOrService(ProtocolConfiguration.class);
	VncRenderService vncService = (VncRenderService) Injector.instantiateModelOrService(VncRenderService.class);

	vncService.detailsProperty().addListener((l, a, b) -> Platform.runLater(() -> headerProperty.set(b.getServerName())));

	vncService.onlineProperty().addListener((l, a, b) -> Platform.runLater(() -> {
	    stage.getIcons().add(b ? onlineImg : offlineImg);
	    stage.getIcons().remove(!b ? onlineImg : offlineImg);
	}));

	SessionContext session = (SessionContext) Injector.instantiateModelOrService(SessionContext.class);
	session.setSession("jfxvnc.app");
	session.loadSession();

	session.bind(sceneWidthProperty, "scene.width");
	session.bind(sceneHeightProperty, "scene.height");

	MainView main = new MainView();

	final Scene scene = new Scene(main.getView(), sceneWidthProperty.get(), sceneHeightProperty.get());
	stage.setOnCloseRequest((e) -> {
	    sceneWidthProperty.set(scene.getWidth());
	    sceneHeightProperty.set(scene.getHeight());
	    Injector.forgetAll();
	    System.exit(0);
	});
	stage.setScene(scene);
	stage.getIcons().add(offlineImg);
	stage.show();

    }

    public static void main(String[] args) {
	launch(args);
    }
}
