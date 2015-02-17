package org.jfxvnc.net.rfb;

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


import static org.junit.Assert.*;

import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.junit.Test;

public class RfbVersionTest {

    @Test
    public void RfbVersionCompare() {
	
	ProtocolVersion v1 = new ProtocolVersion("RFB 003.003\n");
	assertNotNull(v1);
	assertEquals("RFB 003.003\n", v1.toString());
	
	ProtocolVersion v2 = new ProtocolVersion("RFB 003.003\n");
	assertNotNull(v2);

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
	assertEquals("RFB 003.889\n", v5.toString());
	assertTrue(v5.isGreaterThan(ProtocolVersion.RFB_3_8));
    }
}
