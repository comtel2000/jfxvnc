package org.jfxvnc.ui.presentation.statistics;

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
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import javax.inject.Inject;

import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

/**
 * VNC information screen
 * 
 * @author comtel
 *
 */
public class StatisticsViewPresenter implements Initializable {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(StatisticsViewPresenter.class);

    @Inject
    SessionContext ctx;
    @Inject
    VncRenderService con;

    private final StatisticsImageListener imgListener = new StatisticsImageListener();

    private final LongProperty totalCount = new SimpleLongProperty(0);
    private final LongProperty rawCount = new SimpleLongProperty(0);
    private final LongProperty copyRectCount = new SimpleLongProperty(0);
    private final LongProperty hextileCount = new SimpleLongProperty(0);
    private final LongProperty zlibCount = new SimpleLongProperty(0);
    private final LongProperty cursorCount = new SimpleLongProperty(0);
    private final LongProperty desktopCount = new SimpleLongProperty(0);

    @FXML
    private CheckBox enableCB;

    @FXML
    private Label total;
    @FXML
    private Label rawrect;
    @FXML
    private Label copyrect;
    @FXML
    private Label hextilerect;
    @FXML
    private Label zlibrect;
    @FXML
    private Label cursor;
    @FXML
    private Label desktop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

	enableCB.selectedProperty().addListener((l, a, ena) -> {
	    con.imageProperty().removeListener(imgListener);
	    if (ena) {
		con.imageProperty().addListener(imgListener);
	    }

	});

	total.textProperty().bind(totalCount.asString());
	rawrect.textProperty().bind(rawCount.asString());
	zlibrect.textProperty().bind(zlibCount.asString());
	copyrect.textProperty().bind(copyRectCount.asString());
	total.textProperty().bind(totalCount.asString());
	hextilerect.textProperty().bind(hextileCount.asString());
	cursor.textProperty().bind(cursorCount.asString());
	desktop.textProperty().bind(desktopCount.asString());

    }

    class StatisticsImageListener implements ChangeListener<ImageRect> {

	@Override
	public void changed(ObservableValue<? extends ImageRect> observable, ImageRect oldValue, ImageRect newValue) {
	    if (newValue == null) {
		return;
	    }

	    Platform.runLater(() -> {
		totalCount.set(totalCount.get() + 1);

		switch (newValue.getEncoding()) {
		case RAW:
		    rawCount.set(rawCount.get() + 1);
		    break;
		case ZLIB:
		    zlibCount.set(zlibCount.get() + 1);
		    break;
		case HEXTILE:
		    hextileCount.set(hextileCount.get() + 1);
		    break;
		case COPY_RECT:
		    copyRectCount.set(copyRectCount.get() + 1);
		    break;
		case CURSOR:
		    cursorCount.set(cursorCount.get() + 1);
		    break;
		case DESKTOP_SIZE:
		    desktopCount.set(desktopCount.get() + 1);
		    break;
		default:
		    break;
		}
	    });
	}

    }

    @FXML
    private void reset() {
	totalCount.set(0);
	rawCount.set(0);
	copyRectCount.set(0);
	hextileCount.set(0);
	zlibCount.set(0);
	cursorCount.set(0);
	desktopCount.set(0);

    }

}
