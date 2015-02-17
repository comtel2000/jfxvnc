package org.jfxvnc.net.rfb.codec.security;

public interface ISecurityType {

    int INVALID = 0;
    int NONE = 1;
    int VNC_Auth = 2;
    int RA2 = 5;
    int RA2ne = 6;
    int Tight = 16;
    int Ultra = 17;
    int TLS = 18;
    int VeNCrypt = 19;
    int GTK_VNC_SAS = 20;
    int MD5 = 21;
    int Colin_Dean_xvp = 22;

}
