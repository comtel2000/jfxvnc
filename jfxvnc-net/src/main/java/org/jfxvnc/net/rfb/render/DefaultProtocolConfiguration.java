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
package org.jfxvnc.net.rfb.render;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.jfxvnc.net.rfb.codec.security.SecurityType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DefaultProtocolConfiguration implements ProtocolConfiguration {

  private final ObjectProperty<ProtocolVersion> versionProperty = new SimpleObjectProperty<>(ProtocolVersion.RFB_3_8);
  private final ObjectProperty<PixelFormat> clientPixelFormatProperty = new SimpleObjectProperty<>(PixelFormat.RGB_888);
  private final ObjectProperty<SecurityType> securityProperty = new SimpleObjectProperty<>(SecurityType.VNC_Auth);

  private final StringProperty host = new SimpleStringProperty("127.0.0.1");
  private final IntegerProperty port = new SimpleIntegerProperty(DEFAULT_PORT);
  private final IntegerProperty listeningPort = new SimpleIntegerProperty(DEFAULT_LISTENING_PORT);
  private final StringProperty password = new SimpleStringProperty();
  private final BooleanProperty shared = new SimpleBooleanProperty(true);
  private final BooleanProperty ssl = new SimpleBooleanProperty(false);

  private final BooleanProperty rawEnc = new SimpleBooleanProperty(true);
  private final BooleanProperty copyRectEnc = new SimpleBooleanProperty(true);
  private final BooleanProperty hextileEnc = new SimpleBooleanProperty(false);
  private final BooleanProperty zlibEnc = new SimpleBooleanProperty(false);
  private final BooleanProperty clientCursor = new SimpleBooleanProperty(false);
  private final BooleanProperty desktopSize = new SimpleBooleanProperty(true);

  @Override
  public StringProperty hostProperty() {
    return host;
  }

  @Override
  public IntegerProperty portProperty() {
    return port;
  }

  @Override
  public IntegerProperty listeningPortProperty() {
    return listeningPort;
  }
  
  @Override
  public StringProperty passwordProperty() {
    return password;
  }

  @Override
  public BooleanProperty sslProperty() {
    return ssl;
  }

  @Override
  public ObjectProperty<SecurityType> securityProperty() {
    return securityProperty;
  }

  @Override
  public BooleanProperty sharedProperty() {
    return shared;
  }

  @Override
  public ObjectProperty<ProtocolVersion> versionProperty() {
    return versionProperty;
  }

  @Override
  public ObjectProperty<PixelFormat> clientPixelFormatProperty() {
    return clientPixelFormatProperty;
  }

  @Override
  public BooleanProperty rawEncProperty() {
    return rawEnc;
  }

  @Override
  public BooleanProperty copyRectEncProperty() {
    return copyRectEnc;
  }

  @Override
  public BooleanProperty hextileEncProperty() {
    return hextileEnc;
  }

  @Override
  public BooleanProperty clientCursorProperty() {
    return clientCursor;
  }

  @Override
  public BooleanProperty desktopSizeProperty() {
    return desktopSize;
  }

  @Override
  public BooleanProperty zlibEncProperty() {
    return zlibEnc;
  }


}
