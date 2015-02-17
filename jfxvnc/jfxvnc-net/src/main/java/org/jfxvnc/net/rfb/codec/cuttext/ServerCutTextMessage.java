package org.jfxvnc.net.rfb.codec.cuttext;

public class ServerCutTextMessage {

    private final String text;

    public ServerCutTextMessage(String text) {
	this.text = text;
    }

    public String getText() {
	return text;
    }

}
