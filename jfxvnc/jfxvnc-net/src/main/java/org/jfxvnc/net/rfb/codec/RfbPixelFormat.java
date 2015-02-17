package org.jfxvnc.net.rfb.codec;

public class RfbPixelFormat {

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
	return "ServerPixelFormat [bitPerPixel=" + bitPerPixel + ", depth=" + depth + ", bigEndian=" + bigEndian + ", trueColor=" + trueColor + ", redMax=" + redMax
		+ ", greenMax=" + greenMax + ", blueMax=" + blueMax + ", redShift=" + redShift + ", greenShift=" + greenShift + ", blueShift=" + blueShift + "]";
    }
}
