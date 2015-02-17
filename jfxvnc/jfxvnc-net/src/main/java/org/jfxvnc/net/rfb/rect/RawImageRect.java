package org.jfxvnc.net.rfb.rect;

public class RawImageRect extends ImageRect {

    private final int[] pixels;

    public RawImageRect(int x, int y, int width, int height, int[] pixels) {
	super(x, y, width, height);
	this.pixels = pixels;
    }

    public int[] getPixels() {
	return pixels;
    }

}
