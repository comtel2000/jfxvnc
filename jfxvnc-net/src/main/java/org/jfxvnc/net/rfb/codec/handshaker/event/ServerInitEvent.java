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
package org.jfxvnc.net.rfb.codec.handshaker.event;

import org.jfxvnc.net.rfb.codec.PixelFormat;

public class ServerInitEvent implements HandshakeEvent {

  private int frameBufferWidth;
  private int frameBufferHeight;

  private PixelFormat pixelFormat;

  private String serverName;

  public ServerInitEvent() {}

  public int getFrameBufferWidth() {
    return frameBufferWidth;
  }

  public void setFrameBufferWidth(int frameBufferWidth) {
    this.frameBufferWidth = frameBufferWidth;
  }

  public int getFrameBufferHeight() {
    return frameBufferHeight;
  }

  public void setFrameBufferHeight(int frameBufferHeight) {
    this.frameBufferHeight = frameBufferHeight;
  }

  public PixelFormat getPixelFormat() {
    return pixelFormat;
  }

  public void setPixelFormat(PixelFormat pixelFormat) {
    this.pixelFormat = pixelFormat;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  @Override
  public String toString() {
    return "ServerInitEvent [frameBufferWidth=" + frameBufferWidth + ", frameBufferHeight=" + frameBufferHeight + ", pixelFormat=" + pixelFormat
        + ", serverName=" + serverName + "]";
  }

}
