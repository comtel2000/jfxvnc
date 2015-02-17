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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.controlsfx.control.HiddenSidesPane;

public class DemoApp extends Application {

    private Stage mainStage;

    private final SimpleDoubleProperty sceneWidthProperty = new SimpleDoubleProperty(1024);
    private final SimpleDoubleProperty sceneHeightProperty = new SimpleDoubleProperty(768);

    @Override
    public void start(Stage stage) throws Exception {
	mainStage = stage;
	stage.setTitle("Demo (" + System.getProperty("javafx.runtime.version") + ")");

	HiddenSidesPane pane = new HiddenSidesPane();
	pane.setContent(new TableView());
	pane.setRight(new ListView());
	
	Scene scene = new Scene(pane, sceneWidthProperty.get(), sceneHeightProperty.get());

	stage.setScene(scene);
	stage.show();

    }

    public static void main(String[] args) {
	launch(args);
    }

}
