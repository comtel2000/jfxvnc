package org.jfxvnc.net.rfb.codec.input;

public interface InputEventListener {

    void fireInputEvent(InputEventMessage event);
}
