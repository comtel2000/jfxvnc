package org.jfxvnc.net.rfb.codec.handshaker.msg;

public class RfbSecurityResultMessage implements RfbMessage {

    private final boolean passed;
    private Throwable throwable;
    
    public RfbSecurityResultMessage(boolean passed) {
	this.passed = passed;
    }

    public RfbSecurityResultMessage(boolean passed, Throwable t) {
	this.passed = passed;
	this.setThrowable(t);
    }
    
    public boolean isPassed() {
	return passed;
    }

    public Throwable getThrowable() {
	return throwable;
    }

    public void setThrowable(Throwable throwable) {
	this.throwable = throwable;
    }
}
