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
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import javax.inject.Inject;

import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;
import org.jfxvnc.net.rfb.render.rect.CursorImageRect;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.jfxvnc.ui.persist.SessionContext;
import org.jfxvnc.ui.service.CutTextEventHandler;
import org.jfxvnc.ui.service.KeyButtonEventHandler;
import org.jfxvnc.ui.service.PointerEventHandler;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

import com.airhacks.afterburner.injection.Injector;

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

    private WritableImage vncImage;

    private PointerEventHandler pointerHandler;
    private CutTextEventHandler cutTextHandler;

    private KeyButtonEventHandler keyHandler;

    private final Effect blurEffect = new BoxBlur();

    private ImageCursor remoteCursor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

	vncView.setOnMouseEntered(event -> {
	    if (con.connectProperty().get()) {
		vncView.requestFocus();
		vncView.setCursor(remoteCursor != null ? remoteCursor : Cursor.DEFAULT);
	    }
	});

	vncView.setOnMouseExited(event -> {
	    if (con.connectProperty().get()) {
		vncView.setCursor(Cursor.DEFAULT);
	    }
	});

	vncView.setPreserveRatio(true);

	con.imageProperty().addListener((l, old, image) -> Platform.runLater(() -> render(image)));

	con.connectProperty().addListener((l, dis, con) -> Platform.runLater(() -> {
	    if (!con) {
		remoteCursor = null;
		vncView.setEffect(blurEffect);
	    }
	}));
	con.serverCutTextProperty().addListener((l, o, text) -> {
	    if (cutTextHandler != null) {
		cutTextHandler.addClipboardText(text);
	    }
	});
	con.inputProperty().addListener(l -> registerInputEventListener(con.inputProperty().get()));
	prop.clientCursorProperty().addListener((l, a, b) -> {
	    if (!b) {
		vncView.setCursor(Cursor.DEFAULT);
	    }
	});

	vncView.setOnZoom(e -> con.zoomLevelProperty().set(e.getTotalZoomFactor()));

	con.zoomLevelProperty().addListener(l -> {
	    if (vncView.getImage() != null) {
		vncView.setFitHeight(vncView.getImage().getHeight() * con.zoomLevelProperty().get());
	    }
	    // vncView.getTransforms().clear();
	    // vncView.getTransforms().add(new Scale(con.scaleProperty().get(),
	    // con.scaleProperty().get()));
	    });

	con.connectInfoProperty().addListener((l, a, rect) -> Platform.runLater(() -> {
	    vncImage = new WritableImage(rect.getFrameWidth(), rect.getFrameHeight());
	    vncView.setEffect(null);
	    vncView.setImage(vncImage);
	    vncView.setFitHeight(vncView.getImage().getHeight() * con.zoomLevelProperty().get());
	}));

    }

    /** in FX render thread */
    private void render(ImageRect rect) {

	if (vncImage == null) {
	    logger.error("canvas image has not been initialized");
	    return;
	}

	try {
	    switch (rect.getEncoding()) {
	    case RAW:
	    case ZLIB:
		RawImageRect rawRect = (RawImageRect) rect;
		vncImage.getPixelWriter().setPixels(rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), PixelFormat.getIntArgbInstance(), rawRect.getPixels(),
			0, rawRect.getWidth());
		break;
	    case COPY_RECT:
		CopyImageRect copyImageRect = (CopyImageRect) rect;

		PixelReader reader = vncImage.getPixelReader();
		WritableImage copyRect = new WritableImage(copyImageRect.getWidth(), copyImageRect.getHeight());
		copyRect.getPixelWriter().setPixels(0, 0, copyImageRect.getWidth(), copyImageRect.getHeight(), reader, copyImageRect.getSrcX(), copyImageRect.getSrcY());
		vncImage.getPixelWriter().setPixels(copyImageRect.getX(), copyImageRect.getY(), copyImageRect.getWidth(), copyImageRect.getHeight(), copyRect.getPixelReader(), 0,
			0);
		break;
	    case CURSOR:
		if (!prop.clientCursorProperty().get()) {
		    logger.warn("ignore cursor encoding");
		    return;
		}
		final CursorImageRect cRect = (CursorImageRect) rect;

		if (cRect.getHeight() < 2 && cRect.getWidth() < 2) {
		    vncView.setCursor(Cursor.NONE);
		    return;
		}

		if (cRect.getBitmask() != null && cRect.getBitmask().length > 0) {
		    // remove transparent pixels
		    int maskBytesPerRow = Math.floorDiv((cRect.getWidth() + 7), 8);
		    IntStream.range(0, cRect.getHeight()).forEach(
			    y -> IntStream.range(0, cRect.getWidth())
				    .filter(x -> (cRect.getBitmask()[(y * maskBytesPerRow) + Math.floorDiv(x, 8)] & (1 << 7 - Math.floorMod(x, 8))) < 1)
				    .forEach(x -> cRect.getPixels()[y * cRect.getWidth() + x] = 0));
		}

		Dimension2D dim = ImageCursor.getBestSize(cRect.getWidth(), cRect.getHeight());
		WritableImage cImage = new WritableImage((int) dim.getWidth(), (int) dim.getHeight());
		cImage.getPixelWriter().setPixels(0, 0, (int) Math.min(dim.getWidth(), cRect.getWidth()), (int) Math.min(dim.getHeight(), cRect.getHeight()),
			PixelFormat.getIntArgbInstance(), cRect.getPixels(), 0, cRect.getWidth());
		remoteCursor = new ImageCursor(cImage, cRect.getHotspotX(), cRect.getHotspotY());
		vncView.setCursor(remoteCursor);
		break;
	    case DESKTOP_SIZE:
		logger.debug("resize image: {}", rect);
		vncImage = new WritableImage(rect.getWidth(), rect.getHeight());
		vncView.setImage(vncImage);
		break;
	    default:
		logger.error("not supported encoding rect: {}", rect);
		break;
	    }
	} catch (Exception e) {
	    logger.error("rect: " + String.valueOf(rect), e);
	}
    }

    public void registerInputEventListener(InputEventListener listener) {
	if (listener == null) {
	    throw new IllegalArgumentException("input listener must not be null");
	}
	if (pointerHandler == null) {

	    pointerHandler = (PointerEventHandler) Injector.instantiateModelOrService(PointerEventHandler.class);
	    pointerHandler.register(vncView);
	    pointerHandler.enabledProperty().bind(con.connectProperty());
	}
	pointerHandler.setInputEventListener(listener);

	if (keyHandler == null) {
	    keyHandler = (KeyButtonEventHandler) Injector.instantiateModelOrService(KeyButtonEventHandler.class);
	    keyHandler.register(vncView.getScene());
	    keyHandler.enabledProperty().bind(con.connectProperty());
	}
	keyHandler.setInputEventListener(listener);

	if (cutTextHandler == null) {
	    cutTextHandler = (CutTextEventHandler) Injector.instantiateModelOrService(CutTextEventHandler.class);
	    cutTextHandler.enabledProperty().bind(con.connectProperty());
	}
	cutTextHandler.setInputEventListener(listener);

    }

}
