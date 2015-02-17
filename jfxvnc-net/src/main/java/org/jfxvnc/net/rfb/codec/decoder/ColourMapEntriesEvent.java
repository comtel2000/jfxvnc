package org.jfxvnc.net.rfb.codec.decoder;

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


public class ColourMapEntriesEvent implements ServerEvent {

	private final int firstColor;
	private final int numberOfColor;
	private final int red;
	private final int green;
	private final int blue;
	
	public ColourMapEntriesEvent(int firstColor, int numberOfColor, int red, int green, int blue) {
	    this.firstColor = firstColor;
	    this.numberOfColor = numberOfColor;
	    this.red = red;
	    this.green = green;
	    this.blue = blue;
	}

	public int getFirstColor() {
	    return firstColor;
	}

	public int getNumberOfColor() {
	    return numberOfColor;
	}

	public int getRed() {
	    return red;
	}

	public int getGreen() {
	    return green;
	}

	public int getBlue() {
	    return blue;
	}

	@Override
	public String toString() {
	    return "ColourMapEntries [firstColor=" + firstColor + ", numberOfColor=" + numberOfColor + ", red=" + red + ", green=" + green + ", blue=" + blue + "]";
	}
}
