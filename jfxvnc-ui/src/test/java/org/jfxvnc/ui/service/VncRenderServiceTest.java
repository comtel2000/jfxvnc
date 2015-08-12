package org.jfxvnc.ui.service;

/*
 * #%L
 * jfxvnc-ui
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class VncRenderServiceTest {

    @Test
    public void zoomLevelMinMax() {
	VncRenderService service = new VncRenderService();

	assertEquals(1.0, service.zoomLevelProperty().get(), 0.01);
	service.zoomLevelProperty().set(0.0);
	assertNotEquals(0.0, service.zoomLevelProperty().get(), 0.01);
	service.zoomLevelProperty().set(Double.MAX_VALUE);
	assertNotEquals(Double.MAX_VALUE, service.zoomLevelProperty().get(), 0.01);
	service.zoomLevelProperty().set(1.1);
	assertEquals(1.1, service.zoomLevelProperty().get(), 0.01);
    }

}
