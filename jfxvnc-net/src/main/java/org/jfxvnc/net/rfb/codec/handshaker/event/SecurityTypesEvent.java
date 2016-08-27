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

import java.util.Arrays;

import org.jfxvnc.net.rfb.codec.security.SecurityType;

public class SecurityTypesEvent implements HandshakeEvent {

  private final boolean response;

  private final SecurityType[] securityTypes;

  public SecurityTypesEvent(boolean response, SecurityType... securityTypes) {
    this.response = response;
    this.securityTypes = securityTypes;
  }

  public SecurityType[] getSecurityTypes() {
    return securityTypes;
  }

  public boolean isResponse() {
    return response;
  }

  @Override
  public String toString() {
    return "SecurityTypesEvent [response=" + response + ", " + (securityTypes != null ? "securityTypes=" + Arrays.toString(securityTypes) : "") + "]";
  }

}
