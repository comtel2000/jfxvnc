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

import java.util.List;
import java.util.stream.IntStream;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.CursorImageRect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CursorRectDecoder extends RawRectDecoder {

  private int bitMaskLength;

  public CursorRectDecoder(PixelFormat pixelFormat) {
    super(pixelFormat);
  }

  @Override
  public void setRect(FrameRect rect) {
      this.rect = rect;
      this.bitMaskLength = Math.floorDiv(rect.getWidth() + 7, 8) * rect.getHeight();
      this.capacity = (rect.getWidth() * rect.getHeight() * bpp) + bitMaskLength;

  }

  @Override
  protected void sendRect(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) {
    int i = 0;
    ByteBuf pixels = ctx.alloc().buffer(capacity - bitMaskLength);
    while (pixels.isWritable(4)) {
      pixels.writeInt(
          frame.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift() | frame.getUnsignedByte(i * 4 + 1) << pixelFormat.getGreenShift()
              | frame.getUnsignedByte(i * 4 + bluePos) << pixelFormat.getBlueShift() | 0xff000000);
      i++;
    }

    if (bitMaskLength > 0) {
      ByteBuf bitmask = ctx.alloc().buffer(bitMaskLength);
      frame.getBytes(capacity - bitMaskLength, bitmask);
      // remove transparent pixels
      int maskBytesPerRow = Math.floorDiv((rect.getWidth() + 7), 8);
      IntStream.range(0, rect.getHeight())
          .forEach(y -> IntStream.range(0, rect.getWidth())
              .filter(x -> (bitmask.getByte((y * maskBytesPerRow) + Math.floorDiv(x, 8)) & (1 << 7 - Math.floorMod(x, 8))) < 1)
              .forEach(x -> pixels.setInt((y * rect.getWidth() + x) * 4, 0)));
      bitmask.release();
    }
    out.add(new CursorImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), pixels));
  }
}
