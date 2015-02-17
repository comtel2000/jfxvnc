package org.jfxvnc.net.rfb.rect;

public abstract class ImageRect {

    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;

    public ImageRect(int x, int y, int width, int height) {
	super();
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    @Override
    public String toString() {
	return "ImageRect [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

}
