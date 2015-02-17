package org.jfxvnc.net.rfb.codec;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RfbVersion implements Comparable<RfbVersion> {

    public final static RfbVersion RFB_3_3 = new RfbVersion(3, 3);
    public final static RfbVersion RFB_3_7 = new RfbVersion(3, 7);
    public final static RfbVersion RFB_3_8 = new RfbVersion(3, 8);

    private final Pattern VERSION_PAT = Pattern.compile("RFB ([0-9]{3}).([0-9]{3})");

    private int majorVersion;

    private int minorVersion;

    public RfbVersion(String v) {
	if (v == null) {
	    throw new IllegalArgumentException("null can not parsed to version");
	}
	Matcher versionMatcher = VERSION_PAT.matcher(v);
	if (versionMatcher.find()) {
	    majorVersion = Integer.parseInt(versionMatcher.group(1));
	    minorVersion = Integer.parseInt(versionMatcher.group(2));
	} else {
	    throw new IllegalArgumentException("version: " + v + " not supported");
	}
    }

    public RfbVersion(int major, int minor) {
	majorVersion = major;
	minorVersion = minor;
    }

    public int getMajorVersion() {
	return majorVersion;
    }

    public int getMinorVersion() {
	return minorVersion;
    }

    public boolean isGreaterThan(RfbVersion o) {
	return compareTo(o) > 0;
    }

    public boolean isGreaterThan(String v) {
	return compareTo(new RfbVersion(v)) > 0;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null || !(obj instanceof RfbVersion)) {
	    return false;
	}
	return compareTo((RfbVersion) obj) == 0;
    }

    @Override
    public int compareTo(RfbVersion v) {
	if (majorVersion == v.getMajorVersion() && minorVersion == v.getMinorVersion()) {
	    return 0;
	}
	if (majorVersion > v.getMajorVersion() || (majorVersion == v.getMajorVersion() && minorVersion > v.getMinorVersion())) {
	    return 1;
	}
	return -1;
    }

    public byte[] getBytes() {
	return toString().getBytes(StandardCharsets.US_ASCII);
    }
    
    
    @Override
    public String toString() {
	return String.format("RFB %03d.%03d\n", majorVersion, minorVersion);
    }
}
