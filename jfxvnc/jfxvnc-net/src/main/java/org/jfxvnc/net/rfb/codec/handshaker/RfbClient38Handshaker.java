package org.jfxvnc.net.rfb.codec.handshaker;

import org.jfxvnc.net.rfb.codec.RfbVersion;

class RfbClient38Handshaker extends RfbClientHandshaker {

    public RfbClient38Handshaker(RfbVersion version) {
	super(version);
    }

    @Override
    public RfbClientDecoder newRfbClientDecoder() {
	return new RfbClient38Decoder();
    }

    @Override
    public RfbClientEncoder newRfbClientEncoder() {
	return new RfbClient38Encoder();
    }

}
