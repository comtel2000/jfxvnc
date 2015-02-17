package org.jfxvnc.net.rfb.codec.security.vncauth;

import org.jfxvnc.net.rfb.codec.security.RfbSecurityDecoder;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityEncoder;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityHandshaker;


public class VncAuthHandshaker extends RfbSecurityHandshaker {

    public VncAuthHandshaker(int securityType) {
	super(securityType);
    }

    @Override
    public RfbSecurityDecoder newSecurityDecoder() {
	return new VncAuthDecoder();
    }

    @Override
    public RfbSecurityEncoder newSecurityEncoder() {
	return new VncAuthEncoder();
    }

}
