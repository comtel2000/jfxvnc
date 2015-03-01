package org.jfxvnc.net.rfb.render;

/*
 * #%L
 * RFB protocol
 * %%
 * Copyright (C) 2015 comtel2000
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.text.MessageFormat;

import org.jfxvnc.net.rfb.codec.EncodingType;
import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.security.ISecurityType;

public class StringUtils {

    private StringUtils() {

    }

    public static String getEncodingName(int enc) {

	switch (enc) {
	case EncodingType.RAW:
	    return "Raw";
	case EncodingType.COPY_RECT:
	    return "CopyRect";
	case EncodingType.RRE:
	    return "RRE";
	case EncodingType.HEXTILE:
	    return "Hextile";
	case EncodingType.CO_RRE:
	    return "CoRRE";
	case EncodingType.ZLIB:
	    return "zLib";
	case EncodingType.TIGHT:
	    return "Tight";
	case EncodingType.CURSOR:
	    return "Cursor";
	case EncodingType.DESKTOP_SIZE:
	    return "DesktopSize";
	default:
	    return "(" + enc + ")";
	}
    }

    public static String getEncodingNames(int[] enc) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < enc.length; i++) {
	    if (sb.length() > 1) {
		sb.append(", ");
	    }
	    sb.append(getEncodingName(enc[i]));
	}
	return sb.toString();
    }

    public static boolean isPseudoEncoding(int enc) {
	return enc < 0;
    }

    public static String getSecurityName(int sec) {

	switch (sec) {
	case ISecurityType.INVALID:
	    return "invalid";
	case ISecurityType.NONE:
	    return "none";
	case ISecurityType.VNC_Auth:
	    return "VNC Auth";
	case ISecurityType.RA2:
	    return "RA2";
	case ISecurityType.RA2ne:
	    return "RA2ne";
	default:
	    return "(" + sec + ")";
	}
    }

    public static String getSecurityNames(int[] enc) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < enc.length; i++) {
	    if (sb.length() > 1) {
		sb.append(", ");
	    }
	    sb.append(getSecurityName(enc[i]));
	}
	return sb.toString();
    }

    /** depth 24 (32bpp) little-endian shift(r16,r8,b0) */
    public static String getPixelFormatReadable(PixelFormat pf) {
	return MessageFormat.format("depth {0} ({1}bpp) {2}-endian shift(r{3},g{4},b{5})", pf.getDepth(), pf.getBitPerPixel(), (pf.isBigEndian() ? "big" : "little"),
		pf.getRedShift(), pf.getGreenShift(), pf.getBlueShift());
    }

}
