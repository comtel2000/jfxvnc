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

public class PixelFormat {
  /**
   * depth 24 (32bpp) little-endian rgb888
   */
  public final static PixelFormat RGB_888 = new PixelFormat();

  /**
   * depth 16 (16bpp) little-endian rgb555
   */
  public final static PixelFormat RGB_555 = new PixelFormat();

  static {
    RGB_888.setBitPerPixel(32);
    RGB_888.setDepth(24);
    RGB_888.setBigEndian(false);
    RGB_888.setTrueColor(true);
    RGB_888.setRedMax(255);
    RGB_888.setGreenMax(255);
    RGB_888.setBlueMax(255);
    RGB_888.setRedShift(16);
    RGB_888.setGreenShift(8);
    RGB_888.setBlueShift(0);

    RGB_555.setBitPerPixel(16);
    RGB_555.setDepth(16);
    RGB_555.setBigEndian(false);
    RGB_555.setTrueColor(true);
    RGB_555.setRedMax(255);
    RGB_555.setGreenMax(255);
    RGB_555.setBlueMax(255);
    RGB_555.setRedShift(16);
    RGB_555.setGreenShift(8);
    RGB_555.setBlueShift(0);
  }

  private int bitPerPixel;
  private int depth;
  private boolean bigEndian;
  private boolean trueColor;

  private int redMax;
  private int greenMax;
  private int blueMax;

  private int redShift;
  private int greenShift;
  private int blueShift;

  public int getBitPerPixel() {
    return bitPerPixel;
  }

  public void setBitPerPixel(int bitPerPixel) {
    this.bitPerPixel = bitPerPixel;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public boolean isBigEndian() {
    return bigEndian;
  }

  public void setBigEndian(boolean bigEndian) {
    this.bigEndian = bigEndian;
  }

  public boolean isTrueColor() {
    return trueColor;
  }

  public void setTrueColor(boolean trueColor) {
    this.trueColor = trueColor;
  }

  public int getRedMax() {
    return redMax;
  }

  public void setRedMax(int redMax) {
    this.redMax = redMax;
  }

  public int getGreenMax() {
    return greenMax;
  }

  public void setGreenMax(int greenMax) {
    this.greenMax = greenMax;
  }

  public int getBlueMax() {
    return blueMax;
  }

  public void setBlueMax(int blueMax) {
    this.blueMax = blueMax;
  }

  public int getRedShift() {
    return redShift;
  }

  public void setRedShift(int redShift) {
    this.redShift = redShift;
  }

  public int getGreenShift() {
    return greenShift;
  }

  public void setGreenShift(int greenShift) {
    this.greenShift = greenShift;
  }

  public int getBlueShift() {
    return blueShift;
  }

  public void setBlueShift(int blueShift) {
    this.blueShift = blueShift;
  }

  public int getBytePerPixel() {
    return bitPerPixel < 9 ? 1 : bitPerPixel / 8;
  }

  @Override
  public String toString() {
    return "PixelFormat [bitPerPixel=" + bitPerPixel + ", depth=" + depth + ", bigEndian=" + bigEndian + ", trueColor=" + trueColor + ", redMax=" + redMax
        + ", greenMax=" + greenMax + ", blueMax=" + blueMax + ", redShift=" + redShift + ", greenShift=" + greenShift + ", blueShift=" + blueShift + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (bigEndian ? 1231 : 1237);
    result = prime * result + bitPerPixel;
    result = prime * result + blueMax;
    result = prime * result + blueShift;
    result = prime * result + depth;
    result = prime * result + greenMax;
    result = prime * result + greenShift;
    result = prime * result + redMax;
    result = prime * result + redShift;
    result = prime * result + (trueColor ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PixelFormat other = (PixelFormat) obj;
    if (bigEndian != other.bigEndian)
      return false;
    if (bitPerPixel != other.bitPerPixel)
      return false;
    if (blueMax != other.blueMax)
      return false;
    if (blueShift != other.blueShift)
      return false;
    if (depth != other.depth)
      return false;
    if (greenMax != other.greenMax)
      return false;
    if (greenShift != other.greenShift)
      return false;
    if (redMax != other.redMax)
      return false;
    if (redShift != other.redShift)
      return false;
    if (trueColor != other.trueColor)
      return false;
    return true;
  }
}
