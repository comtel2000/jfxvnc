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
package org.jfxvnc.net.rfb.render.rect;

import org.jfxvnc.net.rfb.codec.Encoding;

import io.netty.buffer.ByteBuf;

public class RawImageRect extends ImageRect {

  protected final ByteBuf pixels;
  protected final int scanlineStride;

  public RawImageRect(int x, int y, int width, int height, ByteBuf pixels, int scanlineStride) {
    super(x, y, width, height);
    this.pixels = pixels;
    this.scanlineStride = scanlineStride;
  }

  /**
   * Returns a byte buffer of a pixel data
   * 
   * @return pixels
   */
  public ByteBuf getPixels() {
    return pixels;
  }

  /**
   * Returns the distance between the pixel data for the start of one row of data in the buffer to
   * the start of the next row of data.
   * 
   * @return scanlineStride
   */
  public int getScanlineStride() {
    return scanlineStride;
  }

  @Override
  public Encoding getEncoding() {
    return Encoding.RAW;
  }

  @Override
  public boolean release() {
    return pixels != null ? pixels.release() : true;
  }

  @Override
  public String toString() {
    return "RawImageRect [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", pixels.capacity="
        + (pixels != null ? pixels.capacity() : "null") + ", scanlineStride=" + scanlineStride + "]";
  }
}
