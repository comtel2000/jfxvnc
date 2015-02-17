package org.jfxvnc.net.rfb.codec.security;

import java.util.Map;


public interface RfbSecurityMessage {

    int getSecurityType();
    
    void setCredentials(Map<String, Object> credentials);
    
    
}
