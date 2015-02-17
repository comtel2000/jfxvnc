package org.jfxvnc.net.rfb.rect;

import org.jfxvnc.net.rfb.codec.RfbPixelFormat;

public class CanvasImageRect extends ImageRect {

    private final RfbPixelFormat pixelFormat;
    private final String serverName;

    public CanvasImageRect(int width, int height, String serverName, RfbPixelFormat pixelFormat) {
	super(0, 0, width, height);
	this.serverName = serverName;
	this.pixelFormat = pixelFormat;
    }

    public String getServerName() {
	return serverName;
    }

    public RfbPixelFormat getPixelFormat() {
	return pixelFormat;
    }


}
