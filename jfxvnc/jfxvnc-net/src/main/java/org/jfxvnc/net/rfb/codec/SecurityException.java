package org.jfxvnc.net.rfb.codec;

public class SecurityException extends Exception {

    private static final long serialVersionUID = -2832675482799477488L;

    public SecurityException(String message) {
	super(message);
    }
    
    public SecurityException(String message, Throwable throwable) {
	super(message, throwable);
    }
}
