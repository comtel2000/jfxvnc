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
package org.jfxvnc.net.rfb.codec.decoder.rect;

import org.jfxvnc.net.rfb.codec.Encoding;

public class FrameRect {

  private final int x, y, width, height;
  private final Encoding encoding;

  public FrameRect(int x, int y, int width, int height, Encoding encoding) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.encoding = encoding;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getX2() {
    return x + width;
  }

  public int getY2() {
    return y + height;
  }
  
  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Encoding getEncoding() {
    return encoding;
  }

  @Override
  public String toString() {
    return "FrameRect [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", " + (encoding != null ? "encoding=" + encoding : "") + "]";
  }

}
