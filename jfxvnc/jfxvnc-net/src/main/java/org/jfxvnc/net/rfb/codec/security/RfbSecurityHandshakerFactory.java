package org.jfxvnc.net.rfb.codec.security;

import org.jfxvnc.net.rfb.codec.security.vncauth.VncAuthHandshaker;


public class RfbSecurityHandshakerFactory {

    public RfbSecurityHandshakerFactory() {
    }

    public RfbSecurityHandshaker newRfbSecurityHandshaker(int securityType) {
	
	if (securityType == ISecurityType.VNC_Auth){
	    return new VncAuthHandshaker(securityType);
	}
	
	return null;

    }

}
