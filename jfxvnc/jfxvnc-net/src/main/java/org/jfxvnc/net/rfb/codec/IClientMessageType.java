package org.jfxvnc.net.rfb.codec;

public interface IClientMessageType {

    int SET_PIXEL_FORMAT = 0;
    int SET_ENCODINGS = 2;
    int FRAMEBUFFER_UPDATE_REQUEST = 3;
    int KEY_EVENT = 4;
    int POINTER_EVENT = 5;
    int CLIENT_CUT_TEXT = 6;

    int AL = 255;
    int VMWare_A = 254;
    int VMWare_B = 127;
    int GII = 253;
    int TIGHT = 252;
    int PO_SET_DESKTOP_SIZE = 251;
    int CD_XVP = 250;
    int OLIVE_CALL_CONTROL = 249;

}
