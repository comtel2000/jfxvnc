package org.jfxvnc.net.rfb.codec.input;

public class PointerEventMessage implements InputEventMessage {

    private final byte buttonMask;

    private final int xPos;
    private final int yPos;

    public PointerEventMessage(byte buttonMask, int xPos, int yPos) {
	super();
	this.buttonMask = buttonMask;
	this.xPos = xPos;
	this.yPos = yPos;
    }

    public byte getButtonMask() {
	return buttonMask;
    }

    public int getxPos() {
	return xPos;
    }

    public int getyPos() {
	return yPos;
    }

    @Override
    public String toString() {
	return "PointerEventMessage [buttonMask=" + buttonMask + ", xPos=" + xPos + ", yPos=" + yPos + "]";
    }

}
