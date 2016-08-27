/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package org.jfxvnc.net.rfb.codec;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProtocolVersion implements Comparable<ProtocolVersion> {

  public final static ProtocolVersion RFB_3_3 = new ProtocolVersion(3, 3);
  public final static ProtocolVersion RFB_3_7 = new ProtocolVersion(3, 7);
  public final static ProtocolVersion RFB_3_8 = new ProtocolVersion(3, 8);

  private final Pattern VERSION_PAT = Pattern.compile("RFB ([0-9]{3}).([0-9]{3})");

  private int majorVersion;

  private int minorVersion;

  /**
   * RFB protocol parser (RFB ([0-9]{3}).([0-9]{3}))
   * 
   * @param version String
   */
  public ProtocolVersion(String version) {
    if (version == null) {
      throw new IllegalArgumentException("null can not parsed to version");
    }
    Matcher versionMatcher = VERSION_PAT.matcher(version);
    if (versionMatcher.find()) {
      majorVersion = Integer.parseInt(versionMatcher.group(1));
      minorVersion = Integer.parseInt(versionMatcher.group(2));
    } else {
      throw new IllegalArgumentException("version: " + version + " not supported");
    }
  }

  public ProtocolVersion(int major, int minor) {
    majorVersion = major;
    minorVersion = minor;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinorVersion() {
    return minorVersion;
  }

  public boolean isGreaterThan(ProtocolVersion o) {
    return compareTo(o) > 0;
  }

  public boolean isGreaterThan(String v) {
    return compareTo(new ProtocolVersion(v)) > 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ProtocolVersion)) {
      return false;
    }
    return compareTo((ProtocolVersion) obj) == 0;
  }

  @Override
  public int compareTo(ProtocolVersion v) {
    if (majorVersion == v.getMajorVersion() && minorVersion == v.getMinorVersion()) {
      return 0;
    }
    if (majorVersion > v.getMajorVersion() || (majorVersion == v.getMajorVersion() && minorVersion > v.getMinorVersion())) {
      return 1;
    }
    return -1;
  }

  /**
   * encoded ASCII bytes include LF
   * 
   * @return expected RFB version bytes
   */
  public byte[] getBytes() {
    return String.format("RFB %03d.%03d\n", majorVersion, minorVersion).getBytes(StandardCharsets.US_ASCII);
  }

  @Override
  public String toString() {
    return String.format("RFB %03d.%03d", majorVersion, minorVersion);
  }
}
