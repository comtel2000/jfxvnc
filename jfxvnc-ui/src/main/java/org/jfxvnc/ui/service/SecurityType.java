package org.jfxvnc.ui.service;

public enum SecurityType {

    NONE(1), VNC_Auth(2);

    final int type;

    SecurityType(int type) {
	this.type = type;
    }

    public int getType() {
	return type;
    }

    public static SecurityType getValueByType(int type) {
	for (SecurityType t : values()) {
	    if (type == t.getType()) {
		return t;
	    }
	}
	return null;
    }
}
