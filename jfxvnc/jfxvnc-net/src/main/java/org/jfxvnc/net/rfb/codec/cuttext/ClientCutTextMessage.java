package org.jfxvnc.net.rfb.codec.cuttext;

public class ClientCutTextMessage {

    private final String text;

    public ClientCutTextMessage(String text) {
	this.text = text;
    }

    public String getText() {
	return text;
    }

}
