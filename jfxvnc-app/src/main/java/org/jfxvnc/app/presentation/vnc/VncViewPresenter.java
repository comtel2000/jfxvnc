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
package org.jfxvnc.app.presentation.vnc;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.jfxvnc.app.persist.SessionContext;
import org.jfxvnc.ui.control.VncImageView;
import org.jfxvnc.ui.service.VncRenderService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;

public class VncViewPresenter implements Initializable {

  @Inject
  SessionContext ctx;

  @Inject
  VncRenderService con;

  @FXML
  private ScrollPane scrollPane;

  private final Effect blurEffect = new BoxBlur();

  private VncImageView vncView;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    vncView = new VncImageView();
    scrollPane.setContent(vncView);
    con.setEventConsumer(vncView);
    con.serverCutTextProperty().addListener((l, old, text) -> vncView.addClipboardText(text));
    
    con.onlineProperty().addListener((l, old, online) -> Platform.runLater(() -> {
      vncView.setDisable(!online);
      vncView.setEffect(online ? null : blurEffect);
    }));

    con.inputEventListenerProperty().addListener(l -> vncView.registerInputEventListener(con.inputEventListenerProperty().get()));
    con.getConfiguration().clientCursorProperty().addListener((l, a, b) -> vncView.setUseClientCursor(b));

    vncView.setOnZoom(e -> con.zoomLevelProperty().set(e.getTotalZoomFactor()));

    con.zoomLevelProperty().addListener((l, old, zoom) -> vncView.zoomLevelProperty().set(zoom.doubleValue()));

  }

}
