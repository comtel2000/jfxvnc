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

/**
 * Encoding types
 * 
 * <code>
 * 0 Raw<br>
 * 1 CopyRect<br>
 * 2 RRE<br>
 * 5 Hextile<br>
 * 16 ZRLE<br>
 * -239 Cursor pseudo-encoding<br>
 * -223 DesktopSize pseudo-encoding<br>
 * 4 CoRRE<br>
 * 6 zlib<br>
 * 7 tight<br>
 * 8 zlibhex<br>
 * 15 TRLE<br>
 * 17 Hitachi ZYWRLE<br>
 * 18 Adam Walling XZ<br>
 * 19 Adam Walling XZYW<br>
 * -1 to -222<br>
 * -224 to -238<br>
 * -240 to -256 tight options<br>
 * -257 to -272 Anthony Liguori<br>
 * -273 to -304 VMWare<br>
 * -305 gii<br>
 * -306 popa<br>
 * -307 Peter Astrand DesktopName<br>
 * -308 Pierre Ossman ExtendedDesktopSize<br>
 * -309 Colin Dean xvp<br>
 * -310 OLIVE Call Control<br>
 * -311 CursorWithAlpha<br>
 * -412 to -512 TurboVNC fine-grained quality level<br>
 * -763 to -768 TurboVNC subsampling level<br>
 * 0x574d5600 to 0x574d56ff VMWare<br>
 * </code>
 *
 */
public enum Encoding {

  UNKNOWN(Integer.MIN_VALUE),

  RAW(0),

  COPY_RECT(1),

  RRE(2),

  CO_RRE(4),

  HEXTILE(5),

  ZLIB(6),

  TIGHT(7),

  ZLIB_HEX(8),

  TRLE(15),

  ZRLE(16),

  H_ZYWRLE(17),

  AW_XZ(18),

  AW_XZYW(19),

  DESKTOP_SIZE(-223),

  CURSOR(-239);

  private final int type;

  private Encoding(int type) {
    this.type = type;
  }

  public static Encoding valueOf(int type) {
    for (Encoding e : values()) {
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
