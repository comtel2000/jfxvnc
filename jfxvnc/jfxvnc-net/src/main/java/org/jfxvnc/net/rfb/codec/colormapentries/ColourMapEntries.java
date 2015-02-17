package org.jfxvnc.net.rfb.codec.colormapentries;

public class ColourMapEntries {

	private final int firstColor;
	private final int numberOfColor;
	private final int red;
	private final int green;
	private final int blue;
	
	public ColourMapEntries(int firstColor, int numberOfColor, int red, int green, int blue) {
	    this.firstColor = firstColor;
	    this.numberOfColor = numberOfColor;
	    this.red = red;
	    this.green = green;
	    this.blue = blue;
	}

	public int getFirstColor() {
	    return firstColor;
	}

	public int getNumberOfColor() {
	    return numberOfColor;
	}

	public int getRed() {
	    return red;
	}

	public int getGreen() {
	    return green;
	}

	public int getBlue() {
	    return blue;
	}
}
