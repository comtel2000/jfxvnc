package org.jfxvnc.ui.persist;

public class HistoryEntry {

    private final String host;
    private final int port;
    private int securityType;
    private String password;
    private String serverName;

    public HistoryEntry(String host, int port) {
	super();
	this.host = host;
	this.port = port;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((host == null) ? 0 : host.hashCode());
	result = prime * result + port;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	HistoryEntry other = (HistoryEntry) obj;
	if (host == null) {
	    if (other.host != null)
		return false;
	} else if (!host.equals(other.host))
	    return false;
	if (port != other.port)
	    return false;
	return true;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getServerName() {
	return serverName;
    }

    public void setServerName(String serverName) {
	this.serverName = serverName;
    }

    public String getHost() {
	return host;
    }

    public int getPort() {
	return port;
    }

    public int getSecurityType() {
	return securityType;
    }

    public void setSecurityType(int type) {
	this.securityType = type;
    }

    @Override
    public String toString() {
	return host + ":" + port + (serverName != null ? " (" + serverName + ")" : "");
    }

}
