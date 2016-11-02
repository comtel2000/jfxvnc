package org.jfxvnc.ui.control;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.jfxvnc.net.rfb.codec.decoder.ColourMapEvent;
import org.jfxvnc.net.rfb.codec.decoder.ServerDecoderEvent;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;
import org.jfxvnc.net.rfb.render.rect.CursorImageRect;
import org.jfxvnc.net.rfb.render.rect.HextileImageRect;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.jfxvnc.ui.CutTextEventHandler;
import org.jfxvnc.ui.KeyButtonEventHandler;
import org.jfxvnc.ui.PointerEventHandler;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class VncImageView extends ImageView implements BiConsumer<ServerDecoderEvent, ImageRect> {

  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncImageView.class);

  private WritableImage vncImage;

  private PointerEventHandler pointerHandler;
  private CutTextEventHandler cutTextHandler;
  private KeyButtonEventHandler keyHandler;

  private ImageCursor remoteCursor;

  private boolean useClientCursor = false;

  private final PixelFormat<ByteBuffer> DEFAULT_PIXELFORMAT = PixelFormat.getByteRgbInstance();

  private AtomicReference<PixelFormat<ByteBuffer>> pixelFormat = new AtomicReference<>(DEFAULT_PIXELFORMAT);

  private SimpleDoubleProperty zoomLevel;

  public VncImageView() {
    setPreserveRatio(true);
    registerListener();
  }

  public void registerListener() {

    setOnMouseEntered(event -> {
      if (!isDisabled()) {
        requestFocus();
        setCursor(remoteCursor != null ? remoteCursor : Cursor.DEFAULT);
      }
    });

    setOnMouseExited(event -> {
      if (!isDisabled()) {
        setCursor(Cursor.DEFAULT);
      }
    });
    
    zoomLevelProperty().addListener(l -> {
      if (getImage() != null) {
        setFitHeight(getImage().getHeight() * zoomLevelProperty().get());
      }
    });
  }

  public void setPixelFormat(ColourMapEvent event) {

    int[] colors = new int[event.getNumberOfColor()];
    int r, g, b;
    for (int i = event.getFirstColor(); i < colors.length; i++) {
      r = event.getColors().readUnsignedShort();
      g = event.getColors().readUnsignedShort();
      b = event.getColors().readUnsignedShort();
      colors[i] = (0xff << 24) | ((r >> 8) << 16) | ((g >> 8) << 8) | (b >> 8);
    }

    pixelFormat.set(PixelFormat.createByteIndexedInstance(colors));
  }

  @Override
  public void accept(ServerDecoderEvent event, ImageRect rect) {
    if (event instanceof ConnectInfoEvent){
      Platform.runLater(() -> setConnectInfoEvent((ConnectInfoEvent) event));
    }else  if (event instanceof ColourMapEvent){
      Platform.runLater(() -> setPixelFormat((ColourMapEvent) event));
    }
    if (rect != null){
      Platform.runLater(() -> render(rect));
    }
  }

  private void render(ImageRect rect) {
    try {
      if (vncImage == null) {
        logger.error("canvas image has not been initialized");
        return;
      }
      switch (rect.getEncoding()) {
        case HEXTILE:
          HextileImageRect hextileRect = (HextileImageRect) rect;
          //PixelWriter writer = vncImage.getPixelWriter();
          for (RawImageRect rawRect : hextileRect.getRects()){
            vncImage.getPixelWriter().setPixels(rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), pixelFormat.get(),
                rawRect.getPixels().nioBuffer(), rawRect.getScanlineStride());
          }
          break;
        case RAW:
        case ZLIB:
          RawImageRect rawRect = (RawImageRect) rect;
          vncImage.getPixelWriter().setPixels(rawRect.getX(), rawRect.getY(), rawRect.getWidth(), rawRect.getHeight(), pixelFormat.get(),
              rawRect.getPixels().nioBuffer(), rawRect.getScanlineStride());
          break;
        case COPY_RECT:
          CopyImageRect copyImageRect = (CopyImageRect) rect;
          PixelReader reader = vncImage.getPixelReader();
          WritableImage copyRect = new WritableImage(copyImageRect.getWidth(), copyImageRect.getHeight());
          copyRect.getPixelWriter().setPixels(0, 0, copyImageRect.getWidth(), copyImageRect.getHeight(), reader, copyImageRect.getSrcX(),
              copyImageRect.getSrcY());
          vncImage.getPixelWriter().setPixels(copyImageRect.getX(), copyImageRect.getY(), copyImageRect.getWidth(), copyImageRect.getHeight(),
              copyRect.getPixelReader(), 0, 0);
          break;
        case CURSOR:
          if (!useClientCursor) {
            logger.warn("ignore cursor encoding");
            return;
          }
          final CursorImageRect cRect = (CursorImageRect) rect;

          if (cRect.getHeight() < 2 && cRect.getWidth() < 2) {
            setCursor(Cursor.NONE);
            return;
          }

          Dimension2D dim = ImageCursor.getBestSize(cRect.getWidth(), cRect.getHeight());
          WritableImage cImage = new WritableImage((int) dim.getWidth(), (int) dim.getHeight());
          cImage.getPixelWriter().setPixels(0, 0, (int) Math.min(dim.getWidth(), cRect.getWidth()), (int) Math.min(dim.getHeight(), cRect.getHeight()),
              PixelFormat.getIntArgbInstance(), cRect.getPixels().nioBuffer().asIntBuffer(), cRect.getWidth());
          remoteCursor = new ImageCursor(cImage, cRect.getHotspotX(), cRect.getHotspotY());
          setCursor(remoteCursor);
          break;
        case DESKTOP_SIZE:
          logger.debug("resize image: {}", rect);
          vncImage = new WritableImage(rect.getWidth(), rect.getHeight());
          setImage(vncImage);
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
      pointerHandler.registerZoomLevel(zoomLevelProperty());
      pointerHandler.enabledProperty().bind(disabledProperty().not());
    }
    pointerHandler.setInputEventListener(listener);

    if (keyHandler == null) {
      keyHandler = new KeyButtonEventHandler();
      keyHandler.register(getScene());
      keyHandler.enabledProperty().bind(disabledProperty().not());
    }
    keyHandler.setInputEventListener(listener);

    if (cutTextHandler == null) {
      cutTextHandler = new CutTextEventHandler();
      cutTextHandler.enabledProperty().bind(disabledProperty().not());
    }
    cutTextHandler.setInputEventListener(listener);
  }

  public void unregisterInputEventListener() {
    if (pointerHandler != null) {
      pointerHandler.unregister(this);
      pointerHandler = null;
    }

    if (keyHandler != null) {
      keyHandler.unregister(getScene());
      keyHandler = null;
    }
    
    if (cutTextHandler != null) {
      cutTextHandler.setInputEventListener(null);
      cutTextHandler = null;
    }
  }
  
  public DoubleProperty zoomLevelProperty() {
    if (zoomLevel == null) {
      zoomLevel = new SimpleDoubleProperty(1.0);
    }
    return zoomLevel;
  }

  public boolean isUseClientCursor() {
    return useClientCursor;
  }

  public void setUseClientCursor(boolean flag) {
    this.useClientCursor = flag;
    if (!useClientCursor) {
      setCursor(Cursor.DEFAULT);
    }
  }

  public boolean addClipboardText(String text) {
    if (cutTextHandler != null) {
      cutTextHandler.addClipboardText(text);
      return true;
    }
    return false;
  }

  public void setConnectInfoEvent(ConnectInfoEvent e) {
    setImage(vncImage = new WritableImage(e.getFrameWidth(), e.getFrameHeight()));
    setFitHeight(getImage().getHeight() * zoomLevelProperty().get());
    pixelFormat.set(DEFAULT_PIXELFORMAT);
  }

}

