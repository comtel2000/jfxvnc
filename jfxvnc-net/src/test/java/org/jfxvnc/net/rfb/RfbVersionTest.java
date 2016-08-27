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
package org.jfxvnc.net.rfb;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.junit.Test;

public class RfbVersionTest {

  @Test
  public void RfbVersionCompare() {

    ProtocolVersion v1 = new ProtocolVersion("RFB 003.003\n");
    assertNotNull(v1);
    assertEquals("RFB 003.003", v1.toString());

    ProtocolVersion v2 = new ProtocolVersion("RFB 003.003");
    assertNotNull(v2);

    assertArrayEquals(new byte[] { 82, 70, 66, 32, 48, 48, 51, 46, 48, 48, 51, 10 }, v2.getBytes());
    assertEquals(v1, v2);
    assertFalse(v1.isGreaterThan(v2));
    assertFalse(v2.isGreaterThan(v1));

    ProtocolVersion v3 = new ProtocolVersion("RFB 003.008\n");
    assertNotNull(v3);

    assertTrue(v3.isGreaterThan(v2));
    assertFalse(v2.isGreaterThan(v3));

    assertEquals(3, v3.getMajorVersion());
    assertEquals(8, v3.getMinorVersion());
    assertEquals(ProtocolVersion.RFB_3_8, v3);

    ProtocolVersion v4 = new ProtocolVersion("RFB 004.002\n");
    assertNotNull(v4);

    assertTrue(v4.isGreaterThan(v2));
    assertFalse(v2.isGreaterThan(v4));

    assertEquals(4, v4.getMajorVersion());
    assertEquals(2, v4.getMinorVersion());

    ProtocolVersion v5 = new ProtocolVersion("RFB 003.889\n");
    assertNotNull(v5);
    assertEquals("RFB 003.889", v5.toString());
    assertTrue(v5.isGreaterThan(ProtocolVersion.RFB_3_8));
  }
}
