package org.jfxvnc.ui.presentation.vnc;

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
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.inject.Inject;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.render.rect.CanvasImageRect;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;
import org.jfxvnc.net.rfb.render.rect.CursorImageRect;
import org.jfxvnc.net.rfb.render.rect.DesktopSizeRect;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.service.CutTextEventHandler;
import org.jfxvnc.ui.service.KeyButtonEventHandler;
import org.jfxvnc.ui.service.PointerEventHandler;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;



public class VncViewPresenter implements Initializable {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncViewPresenter.class);

    @Inject
    SessionContext ctx;

    @Inject
    VncRenderService con;

    @Inject
    ProtocolConfiguration prop;

    @FXML
    private ImageView vncView;
    @FXML
    private Canvas cursorView;

    private WritableImage vncImage;
    private WritableImage cursor;

    private PointerEventHandler pointerHandler;

    private KeyButtonEventHandler keyHandler;
    private CutTextEventHandler cutTextHandler;

    private final Effect blurEffect = new BoxBlur();

    @FXML StackPane vncPane;

    @FXML ScrollPane vncScrollPane;

    private Rectangle mouseRect;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	con.imageProperty().addListener((l, old, image) -> Platform.runLater(() -> {
	    render(image);
	}));
	con.connectProperty().addListener((l, dis, con) -> Platform.runLater(() -> {
	    vncView.setEffect(con ? null : blurEffect);
	}));
	con.serverCutTextProperty().addListener((l, o, text) -> {
	    if (cutTextHandler != null) {
		cutTextHandler.addClipboardText(text);
	    }
	});
	con.inputProperty().addListener((l) -> registerInputEventListener(con.inputProperty().get()));
    }

    /** in FX render thread */
    private void render(ImageRect rect) {
	try {
	    if (rect instanceof CanvasImageRect) {
		vncImage = new WritableImage(rect.getWidth(), rect.getHeight());
		vncView.setEffect(null);
		vncView.setImage(vncImage);

		return;
	    }
	    if (rect instanceof DesktopSizeRect) {
		WritableImage copyRect = new WritableImage(rect.getWidth(), rect.getHeight());
		copyRect.getPixelWriter().setPixels(0, 0, Math.min(rect.getWidth(), (int)vncImage.getWidth()), Math.min(rect.getHeight(), (int)vncImage.getHeight()), vncImage.getPixelReader(), 0, 0);
		
		vncImage = copyRect;
		vncView.setImage(vncImage);
		
		return;
	    }
	    if (vncImage == null) {
		logger.error("canvas not initialized");
		return;
	    }

	    if (rect instanceof RawImageRect) {
		RawImageRect rawRect = (RawImageRect) rect;
		vncImage.getPixelWriter().setPixels(rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), PixelFormat.getIntArgbInstance(), rawRect.getPixels(),
			0, rawRect.getWidth());
		return;
	    }
	    if (rect instanceof CopyImageRect) {
		CopyImageRect rawRect = (CopyImageRect) rect;

		PixelReader reader = vncImage.getPixelReader();
		WritableImage copyRect = new WritableImage(rawRect.getWidth(), rawRect.getHeight());
		copyRect.getPixelWriter().setPixels(0, 0, rawRect.getWidth(), rawRect.getHeight(), reader, rawRect.getSrcX(), rawRect.getSrcY());
		vncImage.getPixelWriter().setPixels(rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), copyRect.getPixelReader(), 0, 0);
		return;
	    }
	    if (prop.clientCursorProperty().get() && rect instanceof CursorImageRect) {
		CursorImageRect rawRect = (CursorImageRect) rect;
		cursor = new WritableImage(rawRect.getWidth(), rawRect.getHeight());
		cursor.getPixelWriter().setPixels(0, 0, rawRect.getWidth(), rawRect.getHeight(), PixelFormat.getIntArgbInstance(), rawRect.getPixels(), 0, rawRect.getWidth());
		// TODO optimize it!!
		cursorView.getGraphicsContext2D().clearRect(0, 0, cursorView.getWidth(), cursorView.getHeight());

		mouseRect = new Rectangle(rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight());
	        mouseRect.setFill(Color.BLUE);
	        
		cursorView.setWidth(cursor.getWidth());
		cursorView.setHeight(cursor.getHeight());
		cursorView.getGraphicsContext2D().drawImage(cursor, 0, 0);


	    }
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	}
    }

    public void registerInputEventListener(InputEventListener listener) {

	if (listener == null) {
	    logger.error("input listener must not be null");
	    return;
	}
	vncView.setOnMouseEntered((event) -> {
	    vncView.requestFocus();
	});


	
	if (pointerHandler == null) {
	    pointerHandler = new PointerEventHandler();
	    pointerHandler.register(vncView);
	    pointerHandler.enabledProperty().bind(con.connectProperty());
	    
	}
	pointerHandler.setInputEventListener(listener);

	if (keyHandler == null) {
	    keyHandler = new KeyButtonEventHandler();
	    keyHandler.register(vncView.getScene());
	    keyHandler.enabledProperty().bind(con.connectProperty());
	}
	keyHandler.setInputEventListener(listener);

	if (cutTextHandler == null) {
	    cutTextHandler = new CutTextEventHandler();
	    cutTextHandler.enabledProperty().bind(con.connectProperty());
	}
	cutTextHandler.setInputEventListener(listener);
	
	Platform.runLater(() -> {
	    if (prop.clientCursorProperty().get()) {
		vncView.setCursor(Cursor.NONE);
		cursorView.translateXProperty().bind(pointerHandler.xPosProperty().subtract(cursorView.widthProperty().divide(2)).subtract(cursorView.layoutXProperty()));
		cursorView.translateYProperty().bind(pointerHandler.yPosProperty().subtract(cursorView.heightProperty().divide(2)).subtract(cursorView.layoutYProperty()));
	    } else {
		vncView.setCursor(Cursor.DEFAULT);
		cursorView.translateXProperty().unbind();
		cursorView.translateYProperty().unbind();
	    }
	});
	prop.clientCursorProperty().addListener((l, a, b) -> {
	    if (b) {
		vncView.setCursor(Cursor.NONE);
		cursorView.translateXProperty().bind(pointerHandler.xPosProperty().subtract(cursorView.widthProperty().divide(2)).subtract(cursorView.layoutXProperty()));
		cursorView.translateYProperty().bind(pointerHandler.yPosProperty().subtract(cursorView.heightProperty().divide(2)).subtract(cursorView.layoutYProperty()));
	    } else {
		vncView.setCursor(Cursor.DEFAULT);
		cursorView.translateXProperty().unbind();
		cursorView.translateYProperty().unbind();
	    }
	});
    }

}
