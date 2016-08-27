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
package org.jfxvnc.net.rfb.codec.security.vncauth;

import java.util.Arrays;

import org.jfxvnc.net.rfb.codec.security.RfbSecurityMessage;
import org.jfxvnc.net.rfb.codec.security.SecurityType;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;

public class VncAuthSecurityMessage implements RfbSecurityMessage {

  private final byte[] challenge;
  private byte[] password;
  private ProtocolConfiguration config;

  public VncAuthSecurityMessage(byte[] challenge) {
    this.challenge = challenge;
  }

  public byte[] getChallenge() {
    return challenge;
  }

  public String getPassword() {
    return config.passwordProperty().get();
  }

  @Override
  public SecurityType getSecurityType() {
    return SecurityType.VNC_Auth;
  }

  @Override
  public String toString() {
    return "VncAuthSecurityMessage [challenge=" + Arrays.toString(challenge) + ", password=" + Arrays.toString(password) + "]";
  }

  @Override
  public void setCredentials(ProtocolConfiguration config) {
    this.config = config;

  }

}
