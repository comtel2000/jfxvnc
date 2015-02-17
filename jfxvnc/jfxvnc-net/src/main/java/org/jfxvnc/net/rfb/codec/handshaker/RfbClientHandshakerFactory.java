package org.jfxvnc.net.rfb.codec.handshaker;

import org.jfxvnc.net.rfb.codec.RfbVersion;

public class RfbClientHandshakerFactory {

    public RfbClientHandshakerFactory() {
    }

    public RfbClientHandshaker newRfbClientHandshaker(RfbVersion version) {
	if (version.isGreaterThan(RfbVersion.RFB_3_7)) {
	    return new RfbClient38Handshaker(version);
	}

	return new RfbClient33Handshaker(version);

    }

}
