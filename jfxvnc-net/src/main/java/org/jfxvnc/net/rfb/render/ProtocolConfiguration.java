package org.jfxvnc.net.rfb.render;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.jfxvnc.net.rfb.codec.security.SecurityType;

/*
 * #%L
 * jfxvnc-net
 * %%
 * Copyright (C) 2015 comtel2000
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public interface ProtocolConfiguration {

    /**
     * VNC server name or IP address
     * 
     * @return host
     */
    public StringProperty hostProperty();

    /**
     * VNC server port (default: 5900)
     * 
     * @return port
     */
    public IntegerProperty portProperty();

    /**
     * VNC authentication password
     * 
     * @return password
     */
    public StringProperty passwordProperty();

    /**
     * Enable SSL/TLS transfer
     * 
     * @return SSL/TLS enabled
     */
    public BooleanProperty sslProperty();

    /**
     * Security Type {@link SecurityType}
     * 
     * @return current {@link SecurityType}
     * @see org.jfxvnc.net.rfb.codec.security.SecurityType
     */
    public ObjectProperty<SecurityType> securityProperty();

    /**
     * VNC connection shared by other clients
     * 
     * @return shared
     */
    public BooleanProperty sharedProperty();

    /**
     * Used Protocol Version {@link ProtocolVersion}
     * 
     * @return current {@link ProtocolVersion}
     */
    public ObjectProperty<ProtocolVersion> versionProperty();

    /**
     * Used PixelFormat {@link PixelFormat}
     * 
     * @return current {@link PixelFormat}
     * @see org.jfxvnc.net.rfb.codec.PixelFormat
     */
    public ObjectProperty<PixelFormat> clientPixelFormatProperty();

    /**
     * Activate RAW encoding
     * 
     * @return raw enabled
     * @see org.jfxvnc.net.rfb.codec.Encoding
     */
    public BooleanProperty rawEncProperty();

    /**
     * Activate COPY RECT encoding
     * 
     * @return raw enabled
     * @see org.jfxvnc.net.rfb.codec.Encoding
     */
    public BooleanProperty copyRectEncProperty();

    /**
     * Activate Hextile encoding
     * 
     * @return Hextile enabled
     * @see org.jfxvnc.net.rfb.codec.Encoding
     */
    public BooleanProperty hextileEncProperty();

    /**
     * Activate Cursor pseudo encoding
     * 
     * @return Cursor enabled
     * @see org.jfxvnc.net.rfb.codec.Encoding
     */
    public BooleanProperty clientCursorProperty();

    /**
     * Activate Desktop Resize pseudo encoding
     * 
     * @return Desktop Resize enabled
     * @see org.jfxvnc.net.rfb.codec.Encoding
     */
    public BooleanProperty desktopSizeProperty();

    /**
     * Activate Zlib pseudo encoding
     * 
     * @return Zlib enabled
     * @see org.jfxvnc.net.rfb.codec.Encoding
     */
    public BooleanProperty zlibEncProperty();

}