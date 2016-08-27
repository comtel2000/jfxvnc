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

import java.util.Arrays;

import org.jfxvnc.net.rfb.codec.Encoding;
import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.jfxvnc.net.rfb.codec.decoder.ServerDecoderEvent;
import org.jfxvnc.net.rfb.codec.security.SecurityType;

public class ConnectInfoEvent implements ServerDecoderEvent {

  String remoteAddress;

  String serverName;

  ProtocolVersion rfbProtocol;

  int frameHeight;
  int frameWidth;

  Encoding[] supportedEncodings;

  PixelFormat serverPF;

  PixelFormat clientPF;

  SecurityType security;

  String connectionType;

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public ProtocolVersion getRfbProtocol() {
    return rfbProtocol;
  }

  public void setRfbProtocol(ProtocolVersion rfbProtocol) {
    this.rfbProtocol = rfbProtocol;
  }

  public int getFrameHeight() {
    return frameHeight;
  }

  public void setFrameHeight(int frameHeight) {
    this.frameHeight = frameHeight;
  }

  public int getFrameWidth() {
    return frameWidth;
  }

  public void setFrameWidth(int frameWidth) {
    this.frameWidth = frameWidth;
  }

  public Encoding[] getSupportedEncodings() {
    return supportedEncodings;
  }

  public void setSupportedEncodings(Encoding[] supportedEncodings) {
    this.supportedEncodings = supportedEncodings;
  }

  public PixelFormat getServerPF() {
    return serverPF;
  }

  public void setServerPF(PixelFormat serverPF) {
    this.serverPF = serverPF;
  }

  public PixelFormat getClientPF() {
    return clientPF;
  }

  public void setClientPF(PixelFormat clientPF) {
    this.clientPF = clientPF;
  }

  public SecurityType getSecurity() {
    return security;
  }

  public void setSecurity(SecurityType securityType) {
    this.security = securityType;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(String adr) {
    this.remoteAddress = adr;
  }

  public String getConnectionType() {
    return connectionType;
  }

  public void setConnectionType(String connectionType) {
    this.connectionType = connectionType;
  }

  @Override
  public String toString() {
    return "ConnectInfoEvent [" + (remoteAddress != null ? "remoteAddress=" + remoteAddress + ", " : "")
        + (serverName != null ? "serverName=" + serverName + ", " : "") + (rfbProtocol != null ? "rfbProtocol=" + rfbProtocol + ", " : "") + "frameHeight="
        + frameHeight + ", frameWidth=" + frameWidth + ", "
        + (supportedEncodings != null ? "supportedEncodings=" + Arrays.toString(supportedEncodings) + ", " : "")
        + (serverPF != null ? "serverPF=" + serverPF + ", " : "") + (clientPF != null ? "clientPF=" + clientPF + ", " : "") + "security=" + security + ", "
        + (connectionType != null ? "connectionType=" + connectionType : "") + "]";
  }

}
