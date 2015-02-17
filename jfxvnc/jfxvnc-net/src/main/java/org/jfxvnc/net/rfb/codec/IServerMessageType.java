package org.jfxvnc.net.rfb.codec;

public interface IServerMessageType {

    int FRAMEBUFFER_UPDATE = 0;
    int SET_COLOR_MAP_ENTRIES = 1;
    int BELL = 2;
    int SERVER_CUT_TEXT = 3;

    int AL = 255;
    int VMWare_A = 254;
    int VMWare_B = 127;
    int GII = 253;
    int TIGHT = 252;
    int PO_SET_DESKTOP_SIZE = 251;
    int CD_XVP = 250;
    int OLIVE_CALL_CONTROL = 249;

}
