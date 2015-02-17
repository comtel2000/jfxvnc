package org.jfxvnc.net.rfb;

import static org.junit.Assert.*;

import org.jfxvnc.net.rfb.codec.RfbVersion;
import org.junit.Test;

public class RfbVersionTest {

    @Test
    public void RfbVersionCompare() {
	
	RfbVersion v1 = new RfbVersion("RFB 003.003\n");
	assertNotNull(v1);
	assertEquals("RFB 003.003\n", v1.toString());
	
	RfbVersion v2 = new RfbVersion("RFB 003.003\n");
	assertNotNull(v2);

	assertEquals(v1, v2);
	assertFalse(v1.isGreaterThan(v2));
	assertFalse(v2.isGreaterThan(v1));

	RfbVersion v3 = new RfbVersion("RFB 003.008\n");
	assertNotNull(v3);

	assertTrue(v3.isGreaterThan(v2));
	assertFalse(v2.isGreaterThan(v3));
	
	assertEquals(3, v3.getMajorVersion());
	assertEquals(8, v3.getMinorVersion());

	RfbVersion v4 = new RfbVersion("RFB 004.002\n");
	assertNotNull(v4);

	assertTrue(v4.isGreaterThan(v2));
	assertFalse(v2.isGreaterThan(v4));
	
	assertEquals(4, v4.getMajorVersion());
	assertEquals(2, v4.getMinorVersion());
	
    }
}
