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

import org.jfxvnc.net.rfb.codec.encoder.ClientCutText;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

public class CutTextEventHandler implements KeyButtonMap {

  private InputEventListener listener;

  private Timeline clipTask;
  private Clipboard clipboard;

  private final StringProperty clipTextProperty = new SimpleStringProperty();
  private final BooleanProperty enabled = new SimpleBooleanProperty(false);

  public CutTextEventHandler() {

    Platform.runLater(() -> {
      clipboard = Clipboard.getSystemClipboard();
      clipTask = new Timeline(new KeyFrame(Duration.millis(500), (event) -> {
        if (clipboard.hasString()) {
          String newString = clipboard.getString();
          if (newString == null) {
            return;
          }
          if (clipTextProperty.get() == null) {
            clipTextProperty.set(newString);
            return;
          }
          if (newString != null && !clipTextProperty.get().equals(newString)) {
            fire(new ClientCutText(newString.replace("\r\n", "\n")));
            clipTextProperty.set(newString);
          }
        }
      }));
      clipTask.setCycleCount(Animation.INDEFINITE);
    });

    enabled.addListener((l, o, n) -> Platform.runLater(() -> {
      if (n) {
        clipTask.play();
      } else {
        clipTask.stop();
        clipTextProperty.set(null);
      }
    }));
  }

  public void setInputEventListener(InputEventListener listener) {
    this.listener = listener;
  }

  public void addClipboardText(String text) {
    Platform.runLater(() -> {
      ClipboardContent content = new ClipboardContent();
      content.putString(text);
      clipboard.setContent(content);
    });
  }

  public BooleanProperty enabledProperty() {
    return enabled;
  }

  private void fire(ClientCutText msg) {
    if (listener != null) {
      listener.sendInputEvent(msg);
    }
  }

}
