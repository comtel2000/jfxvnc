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
package org.jfxvnc.net.rfb.codec;

public enum ServerEvent {

  UNKNOWN(Integer.MIN_VALUE),

  FRAMEBUFFER_UPDATE(0),

  SET_COLOR_MAP_ENTRIES(1),

  BELL(2),

  SERVER_CUT_TEXT(3),

  AL(255),

  VMWare_A(254),

  VMWare_B(127),

  GII(253),

  TIGHT(252),

  PO_SET_DESKTOP_SIZE(251),

  CD_XVP(250),

  OLIVE_CALL_CONTROL(249);

  private final int type;

  private ServerEvent(int type) {
    this.type = type;
  }

  public static ServerEvent valueOf(int type) {
    for (ServerEvent e : values()) {
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
