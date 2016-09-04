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
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonEvent;
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyButtonEventHandler implements KeyButtonMap {

  private InputEventListener listener;

  private final BooleanProperty enabled = new SimpleBooleanProperty(false);
  private final EventHandler<KeyEvent> keyEventHandler;

  private boolean lastCodePointRelease;
  private int lastCodePoint;

  public KeyButtonEventHandler() {
    keyEventHandler = (e) -> {
      if (enabled.get()) {
        sendKeyEvents(e);
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

  public void register(Scene scene) {
    scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
    scene.addEventFilter(KeyEvent.KEY_TYPED, keyEventHandler);
    scene.addEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler);
  }

  public void unregister(Scene scene) {
    scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
    scene.removeEventFilter(KeyEvent.KEY_TYPED, keyEventHandler);
    scene.removeEventFilter(KeyEvent.KEY_RELEASED, keyEventHandler);
  }

  private static boolean isModifierPressed(KeyEvent event) {
    return event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShortcutDown();
  }

  public void sendKeyEvents(KeyEvent event) {
    if (event.isConsumed()) {
      return;
    }

    if (event.getEventType() == KeyEvent.KEY_TYPED) {
      if (!isModifierPressed(event) && event.getCode() == KeyCode.UNDEFINED) {
        int codePoint = event.getCharacter().codePointAt(0);
        // space was triggered twice
        if (!Character.isWhitespace(codePoint) && !Character.isISOControl(codePoint)) {
          lastCodePoint = codePoint;
          lastCodePointRelease = true;
          fire(new KeyButtonEvent(true, codePoint));
        }
      }
      return;
    }

    if (event.getCode().isFunctionKey()) {
      sendFunctionKeyEvents(event, event.getEventType() == KeyEvent.KEY_PRESSED);
      return;
    }
    if (event.getCode().isModifierKey()) {
      sendModifierKeyEvents(event, event.getEventType() == KeyEvent.KEY_PRESSED);
      return;
    }
    if (event.getCode().isNavigationKey()) {
      sendNavigationKeyEvents(event, event.getEventType() == KeyEvent.KEY_PRESSED);
      return;
    }

    if (sendSpecialKeyEvents(event, event.getEventType() == KeyEvent.KEY_PRESSED)) {
      return;
    }

    if (event.isShortcutDown() || event.isControlDown()) {
      int codePoint = event.getText().codePointAt(0);
      fire(new KeyButtonEvent(event.getEventType() == KeyEvent.KEY_PRESSED, codePoint));
      return;
    }

    if (event.getEventType() == KeyEvent.KEY_RELEASED) {
      if (lastCodePointRelease) {
        lastCodePointRelease = false;
        fire(new KeyButtonEvent(false, lastCodePoint));
      } else {
        int codePoint = event.getText().codePointAt(0);
        fire(new KeyButtonEvent(false, codePoint));
      }
      return;
    }
  }

  private boolean sendSpecialKeyEvents(KeyEvent event, boolean isDown) {
    switch (event.getCode()) {
      case PRINTSCREEN:
        fire(new KeyButtonEvent(isDown, RFB_Print));
        return true;
      case INSERT:
        fire(new KeyButtonEvent(isDown, RFB_Insert));
        return true;
      case UNDO:
        fire(new KeyButtonEvent(isDown, RFB_Undo));
        return true;
      case AGAIN:
        fire(new KeyButtonEvent(isDown, RFB_Redo));
        return true;
      case FIND:
        fire(new KeyButtonEvent(isDown, RFB_Find));
        return true;
      case CANCEL:
        fire(new KeyButtonEvent(isDown, RFB_Cancel));
        return true;
      case HELP:
        fire(new KeyButtonEvent(isDown, RFB_Help));
        return true;
      case STOP:
        fire(new KeyButtonEvent(isDown, RFB_Break));
        return true;
      case MODECHANGE:
        fire(new KeyButtonEvent(isDown, RFB_Mode_switch));
        return true;
      case NUM_LOCK:
        fire(new KeyButtonEvent(isDown, RFB_Num_Lock));
        return true;
      case BACK_SPACE:
        fire(new KeyButtonEvent(isDown, RFB_BackSpace));
        return true;
      case TAB:
        fire(new KeyButtonEvent(isDown, RFB_Tab));
        return true;
      case CLEAR:
        fire(new KeyButtonEvent(isDown, RFB_Clear));
        return true;
      case ENTER:
        fire(new KeyButtonEvent(isDown, RFB_Return));
        return true;
      case PAUSE:
        fire(new KeyButtonEvent(isDown, RFB_Pause));
        return true;
      case SCROLL_LOCK:
        fire(new KeyButtonEvent(isDown, RFB_Scroll_Lock));
        return true;
      case ESCAPE:
        fire(new KeyButtonEvent(isDown, RFB_Escape));
        return true;
      case DELETE:
        fire(new KeyButtonEvent(isDown, RFB_Delete));
        return true;
      case SPACE:
        fire(new KeyButtonEvent(isDown, RFB_space));
        return true;
      case CAPS:
        fire(new KeyButtonEvent(isDown, RFB_Caps_Lock));
        return true;
      case CHANNEL_DOWN:
        fire(new KeyButtonEvent(isDown, RFB_N));
        return true;
      case NUMPAD0:
        fire(new KeyButtonEvent(isDown, RFB_KP_0));
        return true;
      case NUMPAD1:
        fire(new KeyButtonEvent(isDown, RFB_KP_1));
        return true;
      case NUMPAD2:
        fire(new KeyButtonEvent(isDown, RFB_KP_2));
        return true;
      case NUMPAD3:
        fire(new KeyButtonEvent(isDown, RFB_KP_3));
        return true;
      case NUMPAD4:
        fire(new KeyButtonEvent(isDown, RFB_KP_4));
        return true;
      case NUMPAD5:
        fire(new KeyButtonEvent(isDown, RFB_KP_5));
        return true;
      case NUMPAD6:
        fire(new KeyButtonEvent(isDown, RFB_KP_6));
        return true;
      case NUMPAD7:
        fire(new KeyButtonEvent(isDown, RFB_KP_7));
        return true;
      case NUMPAD8:
        fire(new KeyButtonEvent(isDown, RFB_KP_8));
        return true;
      case NUMPAD9:
        fire(new KeyButtonEvent(isDown, RFB_KP_9));
        return true;
      default:
        return false;
    }
  }

  private void sendNavigationKeyEvents(KeyEvent event, boolean isDown) {
    switch (event.getCode()) {
      case HOME:
        fire(new KeyButtonEvent(isDown, RFB_Home));
        break;
      case KP_UP:
        fire(new KeyButtonEvent(isDown, RFB_KP_Up));
        break;
      case KP_RIGHT:
        fire(new KeyButtonEvent(isDown, RFB_KP_Right));
        break;
      case KP_DOWN:
        fire(new KeyButtonEvent(isDown, RFB_KP_Down));
        break;
      case KP_LEFT:
        fire(new KeyButtonEvent(isDown, RFB_KP_Left));
        break;
      case UP:
        fire(new KeyButtonEvent(isDown, RFB_Up));
        break;
      case RIGHT:
        fire(new KeyButtonEvent(isDown, RFB_Right));
        break;
      case DOWN:
        fire(new KeyButtonEvent(isDown, RFB_Down));
        break;
      case LEFT:
        fire(new KeyButtonEvent(isDown, RFB_Left));
        break;
      case TRACK_PREV:
        fire(new KeyButtonEvent(isDown, RFB_PreviousCandidate));
        break;
      case PAGE_UP:
        fire(new KeyButtonEvent(isDown, RFB_Page_Up));
        break;
      case TRACK_NEXT:
        fire(new KeyButtonEvent(isDown, RFB_Next));
        break;
      case PAGE_DOWN:
        fire(new KeyButtonEvent(isDown, RFB_Page_Down));
        break;
      case END:
        fire(new KeyButtonEvent(isDown, RFB_End));
        break;
      case BEGIN:
        fire(new KeyButtonEvent(isDown, RFB_Begin));
        break;
      default:
        break;
    }
  }

  private void sendFunctionKeyEvents(KeyEvent event, boolean isDown) {
    switch (event.getCode()) {
      case F1:
        fire(new KeyButtonEvent(isDown, RFB_F1));
        break;
      case F2:
        fire(new KeyButtonEvent(isDown, RFB_F2));
        break;
      case F3:
        fire(new KeyButtonEvent(isDown, RFB_F3));
        break;
      case F4:
        fire(new KeyButtonEvent(isDown, RFB_F4));
        break;
      case F5:
        fire(new KeyButtonEvent(isDown, RFB_F5));
        break;
      case F6:
        fire(new KeyButtonEvent(isDown, RFB_F6));
        break;
      case F7:
        fire(new KeyButtonEvent(isDown, RFB_F7));
        break;
      case F8:
        fire(new KeyButtonEvent(isDown, RFB_F8));
        break;
      case F9:
        fire(new KeyButtonEvent(isDown, RFB_F9));
        break;
      case F10:
        fire(new KeyButtonEvent(isDown, RFB_F10));
        break;
      case F11:
        fire(new KeyButtonEvent(isDown, RFB_F11));
        break;
      case F12:
        fire(new KeyButtonEvent(isDown, RFB_F12));
        break;
      default:
        break;
    }
  }

  private void sendModifierKeyEvents(KeyEvent event, boolean isDown) {
    switch (event.getCode()) {
      case SHIFT:
        fire(new KeyButtonEvent(isDown, RFB_Shift_L));
        break;
      case CONTROL:
        fire(new KeyButtonEvent(isDown, RFB_Control_L));
        break;
      case META:
        fire(new KeyButtonEvent(isDown, RFB_Meta_L));
        break;
      case ALT:
        fire(new KeyButtonEvent(isDown, RFB_Alt_L));
        break;
      case ALT_GRAPH:
        fire(new KeyButtonEvent(isDown, RFB_Alt_R));
        break;
      default:
        break;
    }
  }

  private synchronized void fire(KeyButtonEvent msg) {
    if (listener != null) {
      listener.sendInputEvent(msg);
    }
  }

}
