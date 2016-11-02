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
package org.jfxvnc.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonMap;
import org.jfxvnc.net.rfb.codec.encoder.PointerEvent;

import javafx.beans.property.DoubleProperty;

public class PointerEventHandler implements KeyButtonMap {

  private double zoomLevel = 1.0;
  private InputEventListener listener;

  private volatile boolean enabled = true;
  private final MouseMotionListener mouseMotionEventHandler;
  private final MouseListener mouseEventHandler;

  private final MouseWheelListener mouseWheelEventHandler;

  private int xPos;
  private int yPos;

  public PointerEventHandler() {

    mouseMotionEventHandler = new MouseMotionListener() {

      @Override
      public void mouseMoved(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }
    };

    mouseEventHandler = new MouseListener() {

      @Override
      public void mouseReleased(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (enabled) {
          sendMouseEvents(e);
          e.consume();
        }
      }
    };

    mouseWheelEventHandler = (e) -> {
      if (enabled) {
        sendScrollEvents(e);
        e.consume();
      }
    };
  }

  public void setInputEventListener(InputEventListener listener) {
    this.listener = listener;
  }

  public void setEnabled(boolean ena) {
    enabled = ena;
  }

  public void registerZoomLevel(DoubleProperty zoom) {
    zoom.addListener(l -> zoomLevel = zoom.get());
  }

  public void register(JComponent node) {
    node.addMouseMotionListener(mouseMotionEventHandler);
    node.addMouseListener(mouseEventHandler);
    node.addMouseWheelListener(mouseWheelEventHandler);
  }

  public void unregister(JComponent node) {
    node.removeMouseMotionListener(mouseMotionEventHandler);
    node.removeMouseListener(mouseEventHandler);
    node.removeMouseWheelListener(mouseWheelEventHandler);
  }

  private void sendMouseEvents(MouseEvent event) {

    xPos = (int) Math.floor(event.getX() / zoomLevel);
    yPos = (int) Math.floor(event.getY() / zoomLevel);

    byte buttonMask = 0;
    if (event.getID() == MouseEvent.MOUSE_PRESSED || event.getID() == MouseEvent.MOUSE_DRAGGED) {
      if (event.getButton() == MouseEvent.BUTTON2) {
        buttonMask = 2;
      } else if (event.getButton() == MouseEvent.BUTTON3) {
        buttonMask = 4;
      } else {
        buttonMask = 1;
      }
      fire(new PointerEvent(buttonMask, xPos, yPos));
    } else if (event.getID() == MouseEvent.MOUSE_RELEASED || event.getID() == MouseEvent.MOUSE_MOVED) {
      buttonMask = 0;
    }

    fire(new PointerEvent(buttonMask, xPos, yPos));
  }

  private void sendScrollEvents(MouseWheelEvent event) {
    fire(new PointerEvent(event.getWheelRotation() > 0 ? (byte) 8 : (byte) 16, (int) Math.floor(event.getX() / zoomLevel),
        (int) Math.floor(event.getY() / zoomLevel)));
  }

  private synchronized void fire(PointerEvent msg) {
    if (listener != null) {
      listener.sendInputEvent(msg);
    }
  }

}
