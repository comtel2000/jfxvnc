package org.jfxvnc.net.rfb.codec.handshaker.msg;

public class RfbSharedMessage implements RfbMessage {

    private final boolean shared;

    public RfbSharedMessage(boolean shared) {
	this.shared = shared;
    }

    public boolean isShared() {
	return shared;
    }

}
