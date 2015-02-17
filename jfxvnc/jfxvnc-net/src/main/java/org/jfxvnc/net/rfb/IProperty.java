package org.jfxvnc.net.rfb;

import org.jfxvnc.net.rfb.codec.security.ISecurityType;

public interface IProperty {

    /**
     * key client version (value: {@link RfbVersion})<br>
     * Do not change the default version unless you know what you are doing
     * 
     */
    String CLIENT_VERSION = "clientVersion";

    /**
     * key of security (value: {@link Integer}) value {@link ISecurityType}
     */
    String SECURITY_TYPE = "securityType";

    /**
     * Server host address (value: {@link String})
     */
    String HOST = "host";

    /**
     * Server port (value: {@link Integer})<br>
     * default: 5900
     */
    String PORT = "port";

    /**
     * authentication password (value: {@link String})
     */
    String PASSWORD = "password";

    /**
     * shared connection flag (value: {@link Boolean})
     */
    String SHARED_FLAG = "sharedFlag";
}
