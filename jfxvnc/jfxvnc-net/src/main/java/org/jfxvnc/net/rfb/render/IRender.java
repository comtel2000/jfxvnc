package org.jfxvnc.net.rfb.render;

import org.jfxvnc.net.rfb.codec.RfbProtocolEvent;
import org.jfxvnc.net.rfb.codec.input.InputEventListener;
import org.jfxvnc.net.rfb.rect.ImageRect;

public interface IRender {

    // void setSize(int width, int height);
    //
    // void setPixel(int x, int y, int color);
    //
    // void setPixels(int x, int y, int width, int height, int[] pixels);
    //
    // void setPixels(int x, int y, int width, int height, byte[] pixels);
    //
    // void copyRect(int x, int y, int width, int height, int srcX, int srcY);
    //
    // void drawRect(int x, int y, int width, int height, int color);

    void render(ImageRect rect, RenderCallback callback);

    void showInformation(ConnectionDetails details);
    
    void exceptionCaught(String msg, Throwable t);

    void stateChanged(RfbProtocolEvent state);

    void registerInputEventListener(InputEventListener listener);
}
