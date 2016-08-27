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
package org.jfxvnc.net.rfb.codec.security;

/**
 * VNC SecurityType types
 * 
 */
public enum SecurityType {

  UNKNOWN(-1), INVALID(0), NONE(1), VNC_Auth(2), RA2(5), RA2ne(6), Tight(16), Ultra(17), TLS(18), VeNCrypt(19), GTK_VNC_SAS(20), MD5(21), Colin_Dean_xvp(22);

  private final int type;

  private SecurityType(int type) {
    this.type = type;
  }

  public static SecurityType valueOf(int type) {
    for (SecurityType e : values()) {
      if (e.type == type) {
        return e;
      }
    }
    return UNKNOWN;
  }

  public int getType() {
    return type;
  }
}
