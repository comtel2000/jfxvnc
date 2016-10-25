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
    INIT, ADD_TILE, NEXT_TILE, RAW, BG_FILL, FG_FILL, INIT_SUBRECT, ANY_SUBRECT;
  }

  private State state = State.INIT;

  public static final int RAW = (1 << 0);
  public static final int BG_FILL = (1 << 1);
  public static final int FG_FILL = (1 << 2);
  public static final int ANY_SUBRECT = (1 << 3);
  public static final int SUBRECT_COLORED = (1 << 4);

  private int bytesPerPixel;

  private int yPos = 0;
  private int xPos = 0;


  private HextileRect partRect;
  private HextileImageRect imageRect;

  private int tileType;

  private ByteBuf frame;

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
      if (!in.isReadable()) {
        return false;
      }
      imageRect = new HextileImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
      System.err.println("init rect");
      xPos = rect.getX();
      yPos = rect.getY();
      partRect = new HextileRect(xPos, yPos, Math.min(xPos + 16, rect.getX() + rect.getWidth()), Math.min(yPos + 16, rect.getY() + rect.getHeight()));
      tileType = in.readUnsignedByte();
      System.err.println(String.format("tileType: %s (%s,%s)", tileType, xPos, yPos));
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
        frame = in.readRetainedSlice(pixels);
        logger.info("hextile (raw): {}x{} {}", partRect.getWidth(), partRect.getHeight(), frame.readableBytes());
        imageRect.getRects().add(new RawImageRect(partRect.getX(), partRect.getY(), partRect.getWidth(), partRect.getHeight(), frame.copy(),
            partRect.getWidth() * Math.min(3, bytesPerPixel)));
        // frame.release();
        state = State.NEXT_TILE;
      } else {
        int pixels = partRect.getPixelCount(bytesPerPixel);
        frame = ctx.alloc().buffer(pixels);
        state = State.BG_FILL;
      }
    }

    if (state == State.BG_FILL) {
      if ((tileType & BG_FILL) != 0) {
        if (!in.isReadable(bytesPerPixel)) {
          return false;
        }
        readColor(in, bg, bytesPerPixel, redPos, bluePos);
        if (bytesPerPixel == 1) {
          for (int i = 0; i < frame.capacity(); i++) {
            frame.setByte(i, bg[0]);
          }
        } else {
          while (frame.isWritable(3)) {
            frame.writeBytes(bg);
          }
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
        logger.info("fgSpecified: {}", fg);
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
        logger.info("hextile (anySubrects): {} {}", subrectCount, subRectColored);
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
              frame.setByte(index++, fg[0]);
            } else {
              frame.setBytes(index++, fg);
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
      frame.writerIndex(frame.capacity());
      imageRect.getRects().add(new RawImageRect(partRect.getX(), partRect.getY(), partRect.getWidth(), partRect.getHeight(), frame.copy(),
          partRect.getWidth() * Math.min(3, bytesPerPixel)));
      // frame.release();
      state = State.NEXT_TILE;
    }
    
    if (state == State.NEXT_TILE) {
      if (partRect.getX2() == rect.getX2() && partRect.getY2() == rect.getY2()) {
        logger.info("final rects: {}", imageRect);
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
      partRect = new HextileRect(xPos, yPos, Math.min(xPos + 16, rect.getX() + rect.getWidth()), Math.min(yPos + 16, rect.getY() + rect.getHeight()));
      tileType = in.readUnsignedByte();
      System.err.println(String.format("tileType: %s (%s,%s)", tileType, xPos, yPos));
      state = State.RAW;

      if (tileType == 0 && partRect.getX2() == rect.getX2() && partRect.getY2() == rect.getY2()) {
        logger.info("final rects: {}", imageRect);
        out.add(imageRect);
        state = State.INIT;
        return true;
      }
    }
    return false;
  }


  private static void readColor(ByteBuf in, byte[] buffer, int bytesPerPixel, int redPos, int bluePos){
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

  @Override
  protected void sendRect(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    logger.info("readable: {}", in.readableBytes());
    ByteBuf buf = ctx.alloc().buffer(capacity);
    // int[] buf = new int[16 * 16 * 4];

    HextileImageRect imageRect = new HextileImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    Rect fullRect = new Rect();
    fullRect.setXYWH(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    Rect partRect = new Rect();
    int bg = 0;
    int fg = 0;
    int tileType;

    logger.info("start hextile: {} bytes per pixel {}", rect, bytesPerPixel);

    // vertical/row
    for (partRect.tl.y = fullRect.tl.y; partRect.tl.y < fullRect.br.y; partRect.tl.y += 16) {

      partRect.br.y = Math.min(fullRect.br.y, partRect.tl.y + 16);
      // horizontal/column
      for (partRect.tl.x = fullRect.tl.x; partRect.tl.x < fullRect.br.x; partRect.tl.x += 16) {

        partRect.br.x = Math.min(fullRect.br.x, partRect.tl.x + 16);

        tileType = in.readUnsignedByte();

        logger.info("tileType: {}", tileType);
        if ((tileType & RAW) != 0) {
          in.readBytes(buf, partRect.area() * bytesPerPixel);
          // is.readPixels(buf, t.area(), bytesPerPixel, bigEndian);
          logger.info("hextile (raw): {}x{} {}", partRect.height(), partRect.width(), buf.readableBytes());
          imageRect.getRects().add(
              new RawImageRect(partRect.tl.x, partRect.tl.y, partRect.width(), partRect.height(), buf.copy(), partRect.width() * Math.min(3, bytesPerPixel)));
          buf.clear();
          // handler.imageRect(t, buf);
          continue;
        }

        if ((tileType & BG_FILL) != 0) {
          bg = bytesPerPixel == 1 ? in.readUnsignedByte() << 16 : in.readInt();
          logger.info("bgSpecified: {}", bg);
          // bg = is.readPixel(bytesPerPixel, bigEndian);
        }

        int ptr, len;
        // while (len-- > 0) buf[ptr++] = bg;
        for (int i = 0; i < partRect.area(); i++) {
          if (bytesPerPixel == 1) {
            buf.writeByte(bg);
          } else {
            buf.writeInt(bg);
          }
        }


        if ((tileType & FG_FILL) != 0) {
          fg = bytesPerPixel == 1 ? in.readUnsignedByte() << 16 : in.readInt();
          logger.info("fgSpecified: {}", fg);
          // fg = is.readPixel(bytesPerPixel, bigEndian);
        }

        if ((tileType & ANY_SUBRECT) != 0) {
          int nSubrects = in.readUnsignedByte();
          logger.info("hextile (anySubrects): {}", nSubrects);
          for (int i = 0; i < nSubrects; i++) {

            if ((tileType & SUBRECT_COLORED) != 0) {
              fg = bytesPerPixel == 1 ? in.readUnsignedByte() : in.readInt();
              // fg = is.readPixel(bytesPerPixel, bigEndian);
            }

            int xy = in.readUnsignedByte();
            int wh = in.readUnsignedByte();


            int x = ((xy >> 4) & 15);
            int y = (xy & 15);
            int w = ((wh >> 4) & 15) + 1;
            int h = (wh & 15) + 1;
            ptr = y * partRect.width() + x;
            int rowAdd = partRect.width() - w;
            while (h-- > 0) {
              len = w;
              // while (len-- > 0) buf[ptr++] = fg;
              while (len-- > 0) {
                if (bytesPerPixel == 1) {
                  buf.setByte(ptr++, fg);
                } else {
                  buf.setInt(ptr++, fg);
                }
              }
              ptr += rowAdd;
            }
          }
        }
        logger.info("hextile: {}x{} {}", partRect.height(), partRect.width(), buf.readableBytes());
        imageRect.getRects().add(
            new RawImageRect(partRect.tl.x, partRect.tl.y, partRect.width(), partRect.height(), buf.copy(), partRect.width() * Math.min(3, bytesPerPixel)));
        buf.clear();
        // handler.imageRect(t, buf);
      }
    }
    logger.info("rects: {}", imageRect);
    out.add(imageRect);


  }


  class Rect {

    // Rect
    //
    // Represents a rectangular region defined by its top-left (tl)
    // and bottom-right (br) Points.
    // Rects may be compared for equality, checked to determine whether
    // or not they are empty, cleared (made empty), or intersected with
    // one another. The bounding rectangle of two existing Rects
    // may be calculated, as may the area of a Rect.
    // Rects may also be translated, in the same way as Points, by
    // an offset specified in a Point structure.

    public Rect() {
      tl = new Point(0, 0);
      br = new Point(0, 0);
    }

    public Rect(Point tl_, Point br_) {
      tl = new Point(tl_.x, tl_.y);
      br = new Point(br_.x, br_.y);
    }

    public Rect(int x1, int y1, int x2, int y2) {
      tl = new Point(x1, y1);
      br = new Point(x2, y2);
    }

    public final void setXYWH(int x, int y, int w, int h) {
      tl.x = x;
      tl.y = y;
      br.x = x + w;
      br.y = y + h;
    }

    public final Rect intersect(Rect r) {
      Rect result = new Rect();
      result.tl.x = Math.max(tl.x, r.tl.x);
      result.tl.y = Math.max(tl.y, r.tl.y);
      result.br.x = Math.max(Math.min(br.x, r.br.x), result.tl.x);
      result.br.y = Math.max(Math.min(br.y, r.br.y), result.tl.y);
      return result;
    }

    public final Rect union_boundary(Rect r) {
      if (r.is_empty())
        return this;
      if (is_empty())
        return r;
      Rect result = new Rect();
      result.tl.x = Math.min(tl.x, r.tl.x);
      result.tl.y = Math.min(tl.y, r.tl.y);
      result.br.x = Math.max(br.x, r.br.x);
      result.br.y = Math.max(br.y, r.br.y);
      return result;
    }

    public final Rect translate(Point p) {
      return new Rect(tl.translate(p), br.translate(p));
    }

    public final boolean equals(Rect r) {
      return r.tl.equals(tl) && r.br.equals(br);
    }

    public final boolean is_empty() {
      return (tl.x >= br.x) || (tl.y >= br.y);
    }

    public final void clear() {
      tl = new Point();
      br = new Point();
    }

    public final boolean enclosed_by(Rect r) {
      return (tl.x >= r.tl.x) && (tl.y >= r.tl.y) && (br.x <= r.br.x) && (br.y <= r.br.y);
    }

    public final boolean overlaps(Rect r) {
      return tl.x < r.br.x && tl.y < r.br.y && br.x > r.tl.x && br.y > r.tl.y;
    }

    public final int area() {
      int area = (br.x - tl.x) * (br.y - tl.y);
      if (area > 0)
        return area;
      return 0;
    }

    public final Point dimensions() {
      return new Point(width(), height());
    }

    public final int width() {
      return br.x - tl.x;
    }

    public final int height() {
      return br.y - tl.y;
    }

    public final boolean contains(Point p) {
      return (tl.x <= p.x) && (tl.y <= p.y) && (br.x > p.x) && (br.y > p.y);
    }

    public Point tl;
    public Point br;

  }

  class Point {

    // Point
    //
    // Represents a point in 2D space, by X and Y coordinates.
    // Can also be used to represent a delta, or offset, between
    // two Points.
    // Functions are provided to allow Points to be compared for
    // equality and translated by a supplied offset.
    // Functions are also provided to negate offset Points.

    public Point() {
      x = 0;
      y = 0;
    }

    public Point(int x_, int y_) {
      x = x_;
      y = y_;
    }

    public final Point negate() {
      return new Point(-x, -y);
    }

    public final boolean equals(Point p) {
      return (x == p.x && y == p.y);
    }

    public final Point translate(Point p) {
      return new Point(x + p.x, y + p.y);
    }

    public final Point subtract(Point p) {
      return new Point(x - p.x, y - p.y);
    }

    public int x, y;

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
