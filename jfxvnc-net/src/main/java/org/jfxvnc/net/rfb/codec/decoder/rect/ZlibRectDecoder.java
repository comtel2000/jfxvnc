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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.ZlibImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ZlibRectDecoder extends RawRectDecoder {

  private static Logger logger = LoggerFactory.getLogger(ZlibRectDecoder.class);

  private final Inflater inflater;
  private boolean initialized;

  public ZlibRectDecoder(PixelFormat pixelFormat) {
    super(pixelFormat);
    initialized = false;
    inflater = new Inflater();
  }

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (!initialized) {
      if (!in.isReadable(4)) {
        return false;
      }
      capacity = (int) in.readUnsignedInt();
      initialized = true;
    }
    return super.decode(ctx, in, out);
  }

  @Override
  public void setRect(FrameRect rect) {
    this.rect = rect;
  }

  @Override
  protected void sendRect(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) {
    initialized = false;
    if (frame.hasArray()) {
      inflater.setInput(frame.array(), 0, frame.capacity());
    } else {
      byte[] array = new byte[frame.readableBytes()];
      frame.getBytes(frame.readerIndex(), array);
      inflater.setInput(array);
    }
    byte[] result = new byte[rect.getWidth() * rect.getHeight() * bpp];
    try {
      int resultLength = inflater.inflate(result);
      if (resultLength != result.length) {
        logger.error("incorrect zlib ({}/{})", resultLength, result.length);
        return;
      }

      if (bpp == 1) {
        out.add(new ZlibImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), frame.copy(), rect.getWidth()));
        return;
      }

      int i = 0;
      ByteBuf pixels = ctx.alloc().buffer(result.length - (result.length / 4));
      while (pixels.isWritable()) {
        pixels.writeByte(result[i + redPos] & 0xFF);
        pixels.writeByte(result[i + 1] & 0xFF);
        pixels.writeByte(result[i + bluePos] & 0xFF);
        i += 4;
      }
      out.add(new ZlibImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), pixels, rect.getWidth() * 3));

    } catch (DataFormatException e) {
      logger.error(e.getMessage(), e);
      return;
    }
  }
}
