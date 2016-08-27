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

public interface ClientEventType {

  int SET_PIXEL_FORMAT = 0;
  int SET_ENCODINGS = 2;
  int FRAMEBUFFER_UPDATE_REQUEST = 3;
  int KEY_EVENT = 4;
  int POINTER_EVENT = 5;
  int CLIENT_CUT_TEXT = 6;

  int AL = 255;
  int VMWare_A = 254;
  int VMWare_B = 127;
  int GII = 253;
  int TIGHT = 252;
  int PO_SET_DESKTOP_SIZE = 251;
  int CD_XVP = 250;
  int OLIVE_CALL_CONTROL = 249;

}
