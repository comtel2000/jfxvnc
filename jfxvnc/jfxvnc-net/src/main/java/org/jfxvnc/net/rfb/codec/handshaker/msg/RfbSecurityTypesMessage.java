package org.jfxvnc.net.rfb.codec.handshaker.msg;

import java.util.Arrays;

public class RfbSecurityTypesMessage implements RfbMessage {

    private final int[] securityTypes;

    public RfbSecurityTypesMessage(int[] securityTypes) {
	this.securityTypes = securityTypes;
    }

    public int[] getSecurityTypes() {
	return securityTypes;
    }

    @Override
    public String toString() {
	return "RfbSecurityTypesMessage [securityTypes=" + Arrays.toString(securityTypes) + "]";
    }

}
