package org.jfxvnc.net.rfb.codec.input;

/**
 * BackSpace 0xff08<br>
 * Tab 0xff09<br>
 * Return or Enter 0xff0d<br>
 * Escape 0xff1b<br>
 * Insert 0xff63<br>
 * Delete 0xffff<br>
 * Home 0xff50<br>
 * End 0xff57<br>
 * Page Up 0xff55<br>
 * Page Down 0xff56<br>
 * Left 0xff51<br>
 * Up 0xff52<br>
 * Right 0xff53<br>
 * Down 0xff54<br>
 * F1 0xffbe<br>
 * F2 0xffbf<br>
 * F3 0xffc0<br>
 * F4 0xffc1<br>
 * ...<br>
 * ...<br>
 * F12 0xffc9<br>
 * Shift (left) 0xffe1<br>
 * Shift (right) 0xffe2<br>
 * Control (left) 0xffe3<br>
 * Control (right) 0xffe4<br>
 * Meta (left) 0xffe7<br>
 * Meta (right) 0xffe8<br>
 * Alt (left) 0xffe9<br>
 * Alt (right) 0xffea<br>
 * 
 * @author comtel
 *
 */
public class KeyEventMessage implements InputEventMessage {

    private final boolean isDown;

    private final int key;

    public KeyEventMessage(boolean isDown, int key) {
	this.isDown = isDown;
	this.key = key;
    }

    public boolean isDown() {
	return isDown;
    }

    public int getKey() {
	return key;
    }

    @Override
    public String toString() {
	return "KeyEventMessage [isDown=" + isDown + ", key=" + key + "]";
    }

}
