package org.jfxvnc.net.rfb.render;

import org.jfxvnc.net.rfb.codec.RfbPixelFormat;

public class ConnectionDetails {

    String serverName;
    
    String rfbProtocol;
    
    int frameHeight;
    int frameWidth;
    
    int[] supportedEncodings;
    
    RfbPixelFormat serverPF;
    
    RfbPixelFormat clientPF;
    
    int security;
    
}
