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
package org.jfxvnc.ui;

import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonMap;
import org.jfxvnc.net.rfb.codec.encoder.PointerEvent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class PointerEventHandler implements KeyButtonMap {

  private double zoomLevel = 1.0;
  private InputEventListener listener;

  private final BooleanProperty enabled = new SimpleBooleanProperty(false);
  private final EventHandler<MouseEvent> mouseEventHandler;
  private final EventHandler<ScrollEvent> scrollEventHandler;

  private final ReadOnlyIntegerWrapper xPosProperty = new ReadOnlyIntegerWrapper(0);
  private final ReadOnlyIntegerWrapper yPosProperty = new ReadOnlyIntegerWrapper(0);

  public PointerEventHandler() {
    mouseEventHandler = (e) -> {
      if (enabled.get()) {
        sendMouseEvents(e);
        e.consume();
      }
    };
    scrollEventHandler = (e) -> {
      if (enabled.get()) {
        sendScrollEvents(e);
        e.consume();
      }
    };
  }

  public void setInputEventListener(InputEventListener listener) {
    this.listener = listener;
  }

  public BooleanProperty enabledProperty() {
    return enabled;
  }

  public void registerZoomLevel(DoubleProperty zoom) {
    zoom.addListener(l -> zoomLevel = zoom.get());
  }

  public void register(Node node) {

    node.addEventFilter(ScrollEvent.SCROLL, scrollEventHandler);

    node.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
    node.addEventFilter(MouseEvent.MOUSE_MOVED, mouseEventHandler);
    node.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
    node.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

  }

  public void unregister(Node node) {

    node.removeEventFilter(ScrollEvent.SCROLL, scrollEventHandler);

    node.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
    node.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseEventHandler);
    node.removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
    node.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseEventHandler);

  }

  private void sendMouseEvents(MouseEvent event) {

    xPosProperty.set((int) Math.floor(event.getX() / zoomLevel));
    yPosProperty.set((int) Math.floor(event.getY() / zoomLevel));

    byte buttonMask = 0;
    if (event.getEventType() == MouseEvent.MOUSE_PRESSED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
      if (event.isMiddleButtonDown()) {
        buttonMask = 2;
      } else if (event.isSecondaryButtonDown()) {
        buttonMask = 4;
      } else {
        buttonMask = 1;
      }
      fire(new PointerEvent(buttonMask, xPosProperty.get(), yPosProperty.get()));
    } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED || event.getEventType() == MouseEvent.MOUSE_MOVED) {
      buttonMask = 0;
    }

    fire(new PointerEvent(buttonMask, xPosProperty.get(), yPosProperty.get()));

  }

  private void sendScrollEvents(ScrollEvent event) {
    fire(
        new PointerEvent(event.getDeltaY() > 0 ? (byte) 8 : (byte) 16, (int) Math.floor(event.getX() / zoomLevel), (int) Math.floor(event.getY() / zoomLevel)));
  }

  private synchronized void fire(PointerEvent msg) {
    if (listener != null) {
      listener.sendInputEvent(msg);
    }
  }

  public ReadOnlyIntegerProperty xPosProperty() {
    return xPosProperty.getReadOnlyProperty();
  }

  public ReadOnlyIntegerProperty yPosProperty() {
    return yPosProperty.getReadOnlyProperty();
  }

}
