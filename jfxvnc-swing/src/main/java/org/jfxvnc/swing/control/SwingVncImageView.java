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
package org.jfxvnc.swing.control;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.jfxvnc.net.rfb.codec.decoder.ColourMapEvent;
import org.jfxvnc.net.rfb.codec.decoder.ServerDecoderEvent;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;
import org.jfxvnc.net.rfb.render.rect.HextileImageRect;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.jfxvnc.swing.PointerEventHandler;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SwingVncImageView extends VncComponent implements BiConsumer<ServerDecoderEvent, ImageRect> {

  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SwingVncImageView.class);

  private static final long serialVersionUID = 1L;

  private final BooleanProperty disabled = new SimpleBooleanProperty(false);

  private IndexColorModel colorModel;

  private PointerEventHandler pointerHandler;

  public SwingVncImageView() {
    this(true, true);
  }

  public SwingVncImageView(boolean useVolatile, boolean resizable) {
    super(useVolatile, resizable);
    addPropertyChangeListener("enabled", (evt) -> {
      boolean ena = (Boolean) evt.getNewValue();
      disabled.set(!ena);
    });

  }

  public BooleanProperty disabledProperty() {
    return disabled;
  }

  @Override
  public void accept(ServerDecoderEvent event, ImageRect rect) {
    if (event instanceof ConnectInfoEvent) {
      setConnectInfoEvent((ConnectInfoEvent) event);
    } else if (event instanceof ColourMapEvent) {
      setPixelFormat((ColourMapEvent) event);
    }
    if (rect != null) {
      render(rect);
    }
  }

  private void setPixelFormat(ColourMapEvent event) {

    int[] colors = new int[event.getNumberOfColor()];
    int r, g, b;
    for (int i = event.getFirstColor(); i < colors.length; i++) {
      r = event.getColors().readUnsignedShort();
      g = event.getColors().readUnsignedShort();
      b = event.getColors().readUnsignedShort();
      colors[i] = (0xff << 24) | ((r >> 8) << 16) | ((g >> 8) << 8) | (b >> 8);
    }

    colorModel = new IndexColorModel(8, colors.length, colors, event.getFirstColor(), false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    currentImage = getBufferedImage(currentImage.getWidth(), currentImage.getHeight(), colorModel);

  }

  private void setConnectInfoEvent(ConnectInfoEvent e) {
    currentImage = getBufferedImage(e.getFrameWidth(), e.getFrameHeight(), BufferedImage.TYPE_3BYTE_BGR);
    setPreferredSize(new Dimension(e.getFrameWidth(), e.getFrameHeight()));
    if (!isResizable()) {
      setMinimumSize(getPreferredSize());
      setMaximumSize(getPreferredSize());
      setSize(getPreferredSize());
    }
  }

  private void render(ImageRect rect) {
    try {
      if (currentImage == null) {
        logger.error("canvas image has not been initialized");
        return;
      }
      switch (rect.getEncoding()) {
        case HEXTILE:
          HextileImageRect hextileRect = (HextileImageRect) rect;
          //PixelWriter writer = vncImage.getPixelWriter();
          for (RawImageRect rawRect : hextileRect.getRects()){
            renderFrame(true, rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), rawRect.getPixels());
          }
          break;
        case RAW:
        case ZLIB:
          RawImageRect rawRect = (RawImageRect) rect;
          renderFrame(true, rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), rawRect.getPixels());
          break;
        case COPY_RECT:
          CopyImageRect copy = (CopyImageRect) rect;
          Graphics2D gc = (Graphics2D) currentImage.getGraphics();
          gc.copyArea(copy.getSrcX(), copy.getSrcY(), copy.getWidth(), copy.getHeight(), copy.getX(), copy.getY());
          gc.dispose();
          // byte[] pixels = new byte[copy.getWidth() * copy.getHeight() *
          // pixelFormat.getBytePerPixel()];
          // final WritableRaster raster = currentImage.getRaster();
          // raster.getDataElements(copy.getSrcX(), copy.getSrcY(), copy.getWidth(),
          // copy.getHeight(), pixels);
          // raster.setDataElements(copy.getX(), copy.getY(), copy.getWidth(), copy.getHeight(),
          // pixels);
          update(copy.getX(), copy.getY(), copy.getWidth(), copy.getHeight());
          break;
        default:
          logger.error("not supported encoding rect: {}", rect);
          break;
      }
    } catch (Exception e) {
      logger.error("rect: " + String.valueOf(rect), e);
    } finally {
      rect.release();
    }
  }

  public void registerInputEventListener(InputEventListener listener) {
    Objects.requireNonNull(listener, "input listener must not be null");
    if (pointerHandler == null) {

      pointerHandler = new PointerEventHandler();
      pointerHandler.register(this);

      // pointerHandler.registerZoomLevel(zoomLevelProperty());
      disabledProperty().addListener(l -> pointerHandler.setEnabled(!disabledProperty().get()));
    }
    pointerHandler.setInputEventListener(listener);

    // if (keyHandler == null) {
    // keyHandler = new KeyButtonEventHandler();
    // keyHandler.register(getScene());
    // keyHandler.enabledProperty().bind(disabledProperty().not());
    // }
    // keyHandler.setInputEventListener(listener);
    //
    // if (cutTextHandler == null) {
    // cutTextHandler = new CutTextEventHandler();
    // cutTextHandler.enabledProperty().bind(disabledProperty().not());
    // }
    // cutTextHandler.setInputEventListener(listener);
  }


  public void unregisterInputEventListener() {
    if (pointerHandler != null) {
      pointerHandler.unregister(this);
      pointerHandler = null;
    }

  }

}
