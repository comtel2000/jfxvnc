package org.jfxvnc.net.rfb.codec.security.vncauth;

import java.util.Arrays;
import java.util.Map;

import org.jfxvnc.net.rfb.IProperty;
import org.jfxvnc.net.rfb.codec.security.ISecurityType;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityMessage;

public class VncAuthSecurityMessage implements RfbSecurityMessage {

    private final byte[] challenge;
    private byte[] password;
    private Map<String, Object> credentials;

    public VncAuthSecurityMessage(byte[] challenge) {
	this.challenge = challenge;
    }

    public byte[] getChallenge() {
	return challenge;
    }

    public Object getPassword() {
	return credentials.get(IProperty.PASSWORD);
    }

    @Override
    public int getSecurityType() {
	return ISecurityType.VNC_Auth;
    }

    @Override
    public String toString() {
	return "VncAuthSecurityMessage [challenge=" + Arrays.toString(challenge) + ", password=" + Arrays.toString(password) + "]";
    }

    @Override
    public void setCredentials(Map<String, Object> credentials) {
	this.credentials = credentials;

    }

}
