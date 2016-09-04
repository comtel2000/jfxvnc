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
package org.jfxvnc.app.presentation.info;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.jfxvnc.app.persist.SessionContext;
import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.jfxvnc.ui.service.VncRenderService;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * VNC information screen
 * 
 * @author comtel
 *
 */
public class InfoViewPresenter implements Initializable {

  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(InfoViewPresenter.class);

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
  private Label infoName;
  @FXML
  private Label infoHost;
  @FXML
  private Label infoPixelformat;
  @FXML
  private Label infoPixelformatDef;
  @FXML
  private Label infoEncoding;
  @FXML
  private Label infoProtocol;
  @FXML
  private Label infoSecurity;
  @FXML
  private Label infoConnectType;
  @FXML
  private Label infoSize;

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
  @FXML
  private Label total;
  @FXML
  private CheckBox enableCB;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    con.connectInfoProperty().addListener((l, a, b) -> Platform.runLater(() -> updateDetails(b)));
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

  private void updateDetails(ConnectInfoEvent cd) {
    if (cd == null) {
      resetServerData();
      return;
    }
    infoName.setText(cd.getServerName());
    infoSize.setText(String.format("%d x %d", cd.getFrameWidth(), cd.getFrameHeight()));
    infoProtocol.setText(cd.getRfbProtocol().getMajorVersion() + "." + cd.getRfbProtocol().getMinorVersion());
    infoHost.setText(cd.getRemoteAddress());
    infoPixelformat.setText(getPixelFormatReadable(cd.getClientPF()));
    infoPixelformatDef.setText(getPixelFormatReadable(cd.getServerPF()));
    infoEncoding.setText(Arrays.toString(cd.getSupportedEncodings()));
    infoSecurity.setText(String.valueOf(cd.getSecurity()));
    infoConnectType.setText(cd.getConnectionType());
  }

  private void resetServerData() {
    logger.debug("reset fields");
    infoName.setText("-");
    infoHost.setText("-");
    infoPixelformat.setText("-");
    infoPixelformatDef.setText("-");
    infoEncoding.setText("-");
    infoProtocol.setText("-");
    infoSecurity.setText("-");
    infoConnectType.setText("-");
    infoSize.setText("-");
  }

  public static String getPixelFormatReadable(PixelFormat pf) {
    return MessageFormat.format("depth {0} ({1}bpp) {2}-endian shift(r{3},g{4},b{5})", pf.getDepth(), pf.getBitPerPixel(),
        (pf.isBigEndian() ? "big" : "little"), pf.getRedShift(), pf.getGreenShift(), pf.getBlueShift());
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
  public void reset(ActionEvent event) {
    totalCount.set(0);
    rawCount.set(0);
    copyRectCount.set(0);
    hextileCount.set(0);
    zlibCount.set(0);
    cursorCount.set(0);
    desktopCount.set(0);

  }

}
