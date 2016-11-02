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

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class RawRectDecoder implements FrameRectDecoder {

  protected final boolean bigEndian;
  protected final int bpp;
  protected int capacity;
  protected FrameRect rect;
  protected PixelFormat pixelFormat;

  protected final int redPos;
  protected final int bluePos;

  public RawRectDecoder(PixelFormat pixelFormat) {
    this.pixelFormat = pixelFormat;
    this.bigEndian = pixelFormat.isBigEndian();
    this.redPos = bigEndian ? 0 : 2;
    this.bluePos = bigEndian ? 2 : 0;
    this.bpp = pixelFormat.getBytePerPixel();
  }

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (!in.isReadable(capacity)) {
      return false;
    }

    sendRect(ctx, in.readSlice(capacity).retain(), out);
    return true;
  }

  @Override
  public void setRect(FrameRect rect) {
    this.rect = rect;
    this.capacity = rect.getWidth() * rect.getHeight() * bpp;
  }

  protected void sendRect(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) {
    if (bpp == 1) {
      out.add(new RawImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), frame.copy(), rect.getWidth()));
      return;
    }

    // reduce 4 byte to 3 byte
    int size = (capacity * 3) / 4;
    ByteBuf pixels = ctx.alloc().buffer(size);
    byte[] buffer = new byte[3];
    while (pixels.isWritable()) {
      buffer[redPos] = frame.readByte();
      buffer[1] = frame.readByte();
      buffer[bluePos] = frame.readByte();
      pixels.writeBytes(buffer);
      frame.skipBytes(1);
    }
    frame.release();
    out.add(new RawImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), pixels, rect.getWidth() * 3));

  }
}
