package org.jfxvnc.net.rfb.rect;

public class CopyImageRect extends ImageRect {

    protected final int srcX;
    protected final int srcY;

    public CopyImageRect(int x, int y, int width, int height, int srcx, int srcy) {
	super(x, y, width, height);
	this.srcX = srcx;
	this.srcY = srcy;
    }

    public int getSrcX() {
	return srcX;
    }

    public int getSrcY() {
	return srcY;
    }

    @Override
    public String toString() {
	return "CopyImageRect [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", srcX=" + srcX + ", srcY=" + srcY + "]";
    }
}
