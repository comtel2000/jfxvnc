package org.jfxvnc.net.rfb.codec.handshaker;

import org.jfxvnc.net.rfb.codec.RfbVersion;

class RfbClient33Handshaker extends RfbClientHandshaker {


    public RfbClient33Handshaker(RfbVersion version) {
	super(version);
    }

    @Override
    public RfbClientDecoder newRfbClientDecoder() {
	return new RfbClient33Decoder();
    }

    @Override
    public RfbClientEncoder newRfbClientEncoder() {
	return new RfbClient33Encoder();
    }

}
