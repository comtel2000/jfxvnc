package org.jfxvnc.net.rfb.codec;

public class ProtocolException extends Exception {

    private static final long serialVersionUID = 5616560775184943955L;

    public ProtocolException(String message) {
	super(message);
    }
    
    public ProtocolException(String message, Throwable throwable) {
	super(message, throwable);
    }
}
