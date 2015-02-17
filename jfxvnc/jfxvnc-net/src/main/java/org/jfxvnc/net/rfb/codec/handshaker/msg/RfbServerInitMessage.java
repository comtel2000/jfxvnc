package org.jfxvnc.net.rfb.codec.handshaker.msg;

import org.jfxvnc.net.rfb.codec.RfbPixelFormat;

public class RfbServerInitMessage implements RfbMessage {

    private int frameBufferWidth;
    private int frameBufferHeight;

    private RfbPixelFormat pixelFormat;

    private String serverName;

    public RfbServerInitMessage() {

    }

    public int getFrameBufferWidth() {
	return frameBufferWidth;
    }

    public void setFrameBufferWidth(int frameBufferWidth) {
	this.frameBufferWidth = frameBufferWidth;
    }

    public int getFrameBufferHeight() {
	return frameBufferHeight;
    }

    public void setFrameBufferHeight(int frameBufferHeight) {
	this.frameBufferHeight = frameBufferHeight;
    }

    public RfbPixelFormat getPixelFormat() {
	return pixelFormat;
    }

    public void setPixelFormat(RfbPixelFormat pixelFormat) {
	this.pixelFormat = pixelFormat;
    }

    public String getServerName() {
	return serverName;
    }

    public void setServerName(String serverName) {
	this.serverName = serverName;
    }

    @Override
    public String toString() {
	return "ServerInitMessage [frameBufferWidth=" + frameBufferWidth + ", frameBufferHeight=" + frameBufferHeight + ", pixelFormat=" + pixelFormat + ", serverName="
		+ serverName + "]";
    }

}
