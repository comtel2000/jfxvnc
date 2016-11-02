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
package org.jfxvnc.net.rfb.codec.decoder;

import java.util.EnumMap;
import java.util.List;

import org.jfxvnc.net.rfb.codec.Encoding;
import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.net.rfb.codec.ServerEvent;
import org.jfxvnc.net.rfb.codec.decoder.rect.CopyRectDecoder;
import org.jfxvnc.net.rfb.codec.decoder.rect.CursorRectDecoder;
import org.jfxvnc.net.rfb.codec.decoder.rect.DesktopSizeRectDecoder;
import org.jfxvnc.net.rfb.codec.decoder.rect.FrameRect;
import org.jfxvnc.net.rfb.codec.decoder.rect.FrameRectDecoder;
import org.jfxvnc.net.rfb.codec.decoder.rect.HextileDecoder;
import org.jfxvnc.net.rfb.codec.decoder.rect.RawRectDecoder;
import org.jfxvnc.net.rfb.codec.decoder.rect.ZlibRectDecoder;
import org.jfxvnc.net.rfb.exception.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

class FramebufferUpdateRectDecoder implements FrameDecoder {

  private static Logger logger = LoggerFactory.getLogger(FramebufferUpdateRectDecoder.class);

  private PixelFormat pixelFormat;

  private int numberRects, currentRect;

  private final EnumMap<Encoding, FrameRectDecoder> frameRectDecoder = new EnumMap<>(Encoding.class);

  private State state;

  private FrameRect rect;

  enum State {
    INIT, NEW_RECT, READ_RECT
  }

  public FramebufferUpdateRectDecoder(PixelFormat pixelFormat) {
    this.pixelFormat = pixelFormat;
    state = State.INIT;
    registerFrameRectDecoder();
  }

  private void registerFrameRectDecoder() {

    frameRectDecoder.put(Encoding.ZLIB, new ZlibRectDecoder(pixelFormat));
    frameRectDecoder.put(Encoding.COPY_RECT, new CopyRectDecoder(pixelFormat));
    frameRectDecoder.put(Encoding.HEXTILE, new HextileDecoder(pixelFormat));
    frameRectDecoder.put(Encoding.RAW, new RawRectDecoder(pixelFormat));
    frameRectDecoder.put(Encoding.CURSOR, new CursorRectDecoder(pixelFormat));
    frameRectDecoder.put(Encoding.DESKTOP_SIZE, new DesktopSizeRectDecoder(pixelFormat));
  }

  public Encoding[] getSupportedEncodings() {
    return new Encoding[] {Encoding.ZLIB, Encoding.COPY_RECT, Encoding.HEXTILE, Encoding.RAW, Encoding.CURSOR, Encoding.DESKTOP_SIZE};
  }

  public boolean isPixelFormatSupported() {
    logger.debug("is pixelformat supported: {}", pixelFormat);
    return true;// PixelFormat.RGB_888.equals(pixelFormat);
  }

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf m, List<Object> out) throws Exception {

    if (state == State.INIT) {
      logger.trace("init readable {} bytes", m.readableBytes());
      if (!m.isReadable()) {
        return false;
      }
      if (m.getByte(0) != ServerEvent.FRAMEBUFFER_UPDATE.getType()) {
        logger.error("no FBU type!!! {}", m.getByte(0));
        ctx.fireChannelReadComplete();
        return false;
      }
      if (!m.isReadable(4)) {
        return false;
      }
      m.skipBytes(2); // padding
      numberRects = m.readUnsignedShort();
      currentRect = 0;
      logger.trace("number of rectangles: {}", numberRects);
      if (numberRects < 1) {
        return true;
      }
      state = State.NEW_RECT;
    }

    if (state == State.NEW_RECT) {
      if (!readRect(ctx, m, out)) {
        return false;
      }
      state = State.READ_RECT;
    }

    FrameRectDecoder dec = frameRectDecoder.get(rect.getEncoding());
    if (dec == null) {
      throw new ProtocolException("Encoding not supported: " + rect.getEncoding());
    }
    dec.setRect(rect);
    if (!dec.decode(ctx, m, out)) {
      return false;
    }

    if (currentRect == numberRects) {
      state = State.INIT;
      ctx.fireUserEventTriggered(ProtocolState.FBU_REQUEST);
      return true;
    }

    if (!readRect(ctx, m, out)) {
      state = State.NEW_RECT;
    }
    return false;
  }

  
  private boolean readRect(ChannelHandlerContext ctx, ByteBuf m, List<Object> out) {
    if (!m.isReadable(12)) {
      return false;
    }
    int x = m.readUnsignedShort();
    int y = m.readUnsignedShort();
    int w = m.readUnsignedShort();
    int h = m.readUnsignedShort();
    int enc = m.readInt();

    rect = new FrameRect(x, y, w, h, Encoding.valueOf(enc));
    currentRect++;
    logger.trace("{}of{} - ({}) {}", currentRect, numberRects, rect, enc);

    if (w == 0 || h == 0) {
      if (currentRect == numberRects) {
        state = State.INIT;
        ctx.fireUserEventTriggered(ProtocolState.FBU_REQUEST);
        return true;
      }
      return false;
    }
    return true;
  }

}
