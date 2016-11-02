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
import org.jfxvnc.net.rfb.render.rect.HextileImageRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class HextileDecoder extends RawRectDecoder {

  private static Logger logger = LoggerFactory.getLogger(HextileDecoder.class);

  enum State {
    INIT, INIT_PART, ADD_TILE, NEXT_TILE, RAW, BG_FILL, FG_FILL, INIT_SUBRECT, ANY_SUBRECT;
  }

  private State state = State.INIT;

  public static final int RAW = 1;
  public static final int BG_FILL = 2;
  public static final int FG_FILL = 4;
  public static final int ANY_SUBRECT = 8;
  public static final int SUBRECT_COLORED = 16;

  private int bytesPerPixel;

  private int yPos = 0;
  private int xPos = 0;


  private HextileRect partRect;
  private HextileImageRect imageRect;

  private int tileType;

  private ByteBuf frame;
  private int frameStartIndex;

  private final byte[] bg = new byte[3];
  private final byte[] fg = new byte[3];


  private int subrectCount;
  private int subrectPos;
  private boolean subRectColored;

  public HextileDecoder(PixelFormat pixelFormat) {
    super(pixelFormat);
    bytesPerPixel = pixelFormat.getBytePerPixel();
  }


  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    if (state == State.INIT) {
      imageRect = new HextileImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
      xPos = rect.getX();
      yPos = rect.getY();
      state = State.INIT_PART;
    }

    if (state == State.INIT_PART) {
      if (!in.isReadable()) {
        return false;
      }
      partRect = new HextileRect(xPos, yPos, Math.min(xPos + 16, rect.getX() + rect.getWidth()), Math.min(yPos + 16, rect.getY() + rect.getHeight()));
      tileType = in.readUnsignedByte();
      state = State.RAW;
    }

    if (state == State.RAW) {
      if ((tileType & RAW) != 0) {
        if (!in.isReadable()) {
          return false;
        }

        int pixels = partRect.getPixelCount(bytesPerPixel);
        if (!in.isReadable(pixels)) {
          return false;
        }
        if (bytesPerPixel == 1) {
          frame = in.readSlice(pixels).retain();
        } else {
          // reduce 4 byte to 3 byte
          int size = (pixels * 3) / 4;
          frame = ctx.alloc().buffer(size);
          byte[] buffer = new byte[3];
          while (frame.isWritable()) {
            buffer[redPos] = in.readByte();
            buffer[1] = in.readByte();
            buffer[bluePos] = in.readByte();
            frame.writeBytes(buffer);
            in.skipBytes(1);
          }
        }
        logger.trace("hextile (raw): {}x{} {}", partRect.getWidth(), partRect.getHeight(), frame.readableBytes());
        imageRect.getRects().add(new RawImageRect(partRect.getX(), partRect.getY(), partRect.getWidth(), partRect.getHeight(), frame.copy(),
            partRect.getWidth() * Math.min(3, bytesPerPixel)));
        state = State.NEXT_TILE;
      } else {
        int pixels = partRect.getPixelCount(bytesPerPixel);
        if (bytesPerPixel == 1) {
          frame = ctx.alloc().buffer(pixels);
        } else {
          int size = (pixels * 3) / 4;
          frame = ctx.alloc().buffer(size);
        }
        frameStartIndex = frame.readerIndex();
        state = State.BG_FILL;
      }
    }

    if (state == State.BG_FILL) {
      if ((tileType & BG_FILL) != 0) {
        if (!in.isReadable(bytesPerPixel)) {
          return false;
        }
        readColor(in, bg, bytesPerPixel, redPos, bluePos);
      }
      if (bytesPerPixel == 1) {
        for (int i = frameStartIndex; i < frame.capacity(); i++) {
          frame.setByte(frameStartIndex + i, bg[0]);
        }
      } else {
        for (int i = frameStartIndex; i < frame.capacity() / 3; i++) {
          frame.setBytes(frameStartIndex + (i * 3), bg);
        }
      }
      state = State.FG_FILL;
    }

    if (state == State.FG_FILL) {
      if ((tileType & FG_FILL) != 0) {
        if (!in.isReadable(bytesPerPixel)) {
          return false;
        }
        readColor(in, fg, bytesPerPixel, redPos, bluePos);
      }
      state = State.INIT_SUBRECT;
    }

    if (state == State.INIT_SUBRECT) {
      subrectPos = 0;
      subrectCount = 0;
      if ((tileType & ANY_SUBRECT) != 0) {
        if (!in.isReadable()) {
          return false;
        }
        subrectCount = in.readUnsignedByte();
        subrectPos = 0;
        subRectColored = (tileType & SUBRECT_COLORED) != 0;
        logger.trace("hextile (anySubrects): {} {}", subrectCount, subRectColored);
        state = State.ANY_SUBRECT;
      } else {
        state = State.ADD_TILE;
      }
    }

    if (state == State.ANY_SUBRECT) {
      while (subrectPos < subrectCount && in.isReadable(subRectColored ? 2 + bytesPerPixel : 2)) {
        if (subRectColored) {
          readColor(in, fg, bytesPerPixel, redPos, bluePos);
        }
        int xy = in.readUnsignedByte();
        int wh = in.readUnsignedByte();

        int x = ((xy >> 4) & 15);
        int y = (xy & 15);
        int w = ((wh >> 4) & 15) + 1;
        int h = (wh & 15) + 1;
        int index = y * partRect.getWidth() + x;
        int rowAdd = partRect.getWidth() - w;
        for (int row = 0; row < h; row++) {
          for (int col = 0; col < w; col++) {
            if (bytesPerPixel == 1) {
              frame.setByte(frameStartIndex + index++, fg[0]);
            } else {
              frame.setBytes(frameStartIndex + (3 * index++), fg);
            }
          }
          index += rowAdd;
        }
        subrectPos++;
      }
      if (subrectPos >= subrectCount) {
        state = State.ADD_TILE;
      }
    }
    if (state == State.ADD_TILE) {
      frame.writerIndex(frameStartIndex + frame.capacity());
      imageRect.getRects().add(new RawImageRect(partRect.getX(), partRect.getY(), partRect.getWidth(), partRect.getHeight(), frame.copy(),
          partRect.getWidth() * Math.min(3, bytesPerPixel)));
      // frame.release();
      state = State.NEXT_TILE;
    }

    if (state == State.NEXT_TILE) {
      if (partRect.getX2() == rect.getX2() && partRect.getY2() == rect.getY2()) {
        logger.trace("final rects: {}", imageRect);
        out.add(imageRect);
        state = State.INIT;
        return true;
      }
      if (partRect.getX2() == rect.getX2()) {
        // new row
        yPos += 16;
        xPos = rect.getX();
      } else {
        // new column
        xPos += 16;
      }
      state = State.INIT_PART;
    }
    return false;
  }


  private static void readColor(ByteBuf in, byte[] buffer, int bytesPerPixel, int redPos, int bluePos) {
    if (bytesPerPixel == 1) {
      buffer[0] = in.readByte();
    } else {
      buffer[redPos] = in.readByte();
      buffer[1] = in.readByte();
      buffer[bluePos] = in.readByte();
      in.skipBytes(1);
    }

  }

  @Override
  public void setRect(FrameRect rect) {
    this.rect = rect;
    // this.capacity = rect.getWidth() * rect.getHeight() * bpp;
    this.capacity = 16 * 16 * 4 * bytesPerPixel;
  }


  static class HextileRect {

    private final int x, y, x2, y2;

    public HextileRect(int x, int y, int x2, int y2) {
      this.x = x;
      this.y = y;
      this.x2 = x2;
      this.y2 = y2;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    public int getX2() {
      return x2;
    }

    public int getY2() {
      return y2;
    }

    public int getWidth() {
      return x2 - x;
    }

    public int getHeight() {
      return y2 - y;
    }

    public int getPixelCount(int bytePerPixel) {
      return getWidth() * getHeight() * bytePerPixel;
    }

    @Override
    public String toString() {
      return "HextileRect [x=" + x + ", y=" + y + ", x2=" + x2 + ", y2=" + y2 + ", getWidth()=" + getWidth() + ", getHeight()=" + getHeight() + "]";
    }

  }
}
