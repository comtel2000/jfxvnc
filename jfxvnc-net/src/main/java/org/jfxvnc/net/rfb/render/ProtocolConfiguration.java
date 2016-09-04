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
import javafx.beans.property.StringProperty;

public interface ProtocolConfiguration {

  final int DEFAULT_PORT = 5900;
  final int DEFAULT_LISTENING_PORT = 5500;

  /**
   * VNC server name or IP address
   * 
   * @return host
   */
  StringProperty hostProperty();

  /**
   * VNC server port (default: 5900)
   * 
   * @return port
   */
  IntegerProperty portProperty();

  /**
   * listening mode to accept incoming connection requests (default: 5500)
   * 
   * @return listening port
   */
  IntegerProperty listeningPortProperty();

  /**
   * VNC authentication password
   * 
   * @return password
   */
  StringProperty passwordProperty();

  /**
   * Enable SSL/TLS transfer
   * 
   * @return SSL/TLS enabled
   */
  BooleanProperty sslProperty();

  /**
   * Security Type {@link SecurityType}
   * 
   * @return current {@link SecurityType}
   * @see org.jfxvnc.net.rfb.codec.security.SecurityType
   */
  ObjectProperty<SecurityType> securityProperty();

  /**
   * VNC connection shared by other clients
   * 
   * @return shared
   */
  BooleanProperty sharedProperty();

  /**
   * Used Protocol Version {@link ProtocolVersion}
   * 
   * @return current {@link ProtocolVersion}
   */
  ObjectProperty<ProtocolVersion> versionProperty();

  /**
   * Used PixelFormat {@link PixelFormat}
   * 
   * @return current {@link PixelFormat}
   * @see org.jfxvnc.net.rfb.codec.PixelFormat
   */
  ObjectProperty<PixelFormat> clientPixelFormatProperty();

  /**
   * Activate RAW encoding
   * 
   * @return raw enabled
   * @see org.jfxvnc.net.rfb.codec.Encoding
   */
  BooleanProperty rawEncProperty();

  /**
   * Activate COPY RECT encoding
   * 
   * @return raw enabled
   * @see org.jfxvnc.net.rfb.codec.Encoding
   */
  BooleanProperty copyRectEncProperty();

  /**
   * Activate Hextile encoding
   * 
   * @return Hextile enabled
   * @see org.jfxvnc.net.rfb.codec.Encoding
   */
  BooleanProperty hextileEncProperty();

  /**
   * Activate Cursor pseudo encoding
   * 
   * @return Cursor enabled
   * @see org.jfxvnc.net.rfb.codec.Encoding
   */
  BooleanProperty clientCursorProperty();

  /**
   * Activate Desktop Resize pseudo encoding
   * 
   * @return Desktop Resize enabled
   * @see org.jfxvnc.net.rfb.codec.Encoding
   */
  BooleanProperty desktopSizeProperty();

  /**
   * Activate Zlib pseudo encoding
   * 
   * @return Zlib enabled
   * @see org.jfxvnc.net.rfb.codec.Encoding
   */
  BooleanProperty zlibEncProperty();

}
