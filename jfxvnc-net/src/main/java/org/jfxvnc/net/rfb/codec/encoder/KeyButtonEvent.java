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
package org.jfxvnc.net.rfb.codec.encoder;

/**
 * BackSpace 0xff08<br>
 * Tab 0xff09<br>
 * Return or Enter 0xff0d<br>
 * Escape 0xff1b<br>
 * Insert 0xff63<br>
 * Delete 0xffff<br>
 * Home 0xff50<br>
 * End 0xff57<br>
 * Page Up 0xff55<br>
 * Page Down 0xff56<br>
 * Left 0xff51<br>
 * Up 0xff52<br>
 * Right 0xff53<br>
 * Down 0xff54<br>
 * F1 0xffbe<br>
 * F2 0xffbf<br>
 * F3 0xffc0<br>
 * F4 0xffc1<br>
 * ...<br>
 * ...<br>
 * F12 0xffc9<br>
 * Shift (left) 0xffe1<br>
 * Shift (right) 0xffe2<br>
 * Control (left) 0xffe3<br>
 * Control (right) 0xffe4<br>
 * Meta (left) 0xffe7<br>
 * Meta (right) 0xffe8<br>
 * Alt (left) 0xffe9<br>
 * Alt (right) 0xffea<br>
 * 
 * @author comtel
 *
 */
public class KeyButtonEvent implements InputEvent {

  private final boolean isDown;

  private final int key;

  public KeyButtonEvent(boolean isDown, int key) {
    this.isDown = isDown;
    this.key = key;
  }

  public boolean isDown() {
    return isDown;
  }

  public int getKey() {
    return key;
  }

  @Override
  public String toString() {
    return "KeyEvent [isDown=" + isDown + ", key=" + key + "]";
  }

}
