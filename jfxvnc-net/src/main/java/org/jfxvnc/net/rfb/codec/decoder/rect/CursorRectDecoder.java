/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.jfxvnc.net.rfb.codec.decoder.rect;

import java.util.List;
import java.util.stream.IntStream;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.CursorImageRect;

import io.netty.buffer.ByteBuf;

public class CursorRectDecoder extends RawRectDecoder {

    private int bitMaskLength;

    public CursorRectDecoder(PixelFormat pixelFormat) {
	super(pixelFormat);
    }

    @Override
    public void setRect(FrameRect rect) {
	if (framebuffer == null || framebuffer.writerIndex() == 0) {
	    this.rect = rect;
	    this.bitMaskLength = Math.floorDiv(rect.getWidth() + 7, 8) * rect.getHeight();
	    this.capacity = (rect.getWidth() * rect.getHeight() * pixelFormat.getBytePerPixel()) + bitMaskLength;
	}
    }

    @Override
    protected void sendRect(List<Object> out) {
	int i = 0;
	ByteBuf pixels = aloc.buffer(capacity - bitMaskLength);
	while (pixels.isWritable(4)) {
	    pixels.writeInt(framebuffer.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift()
		    | framebuffer.getUnsignedByte(i * 4 + 1) << pixelFormat.getGreenShift()
		    | framebuffer.getUnsignedByte(i * 4 + bluePos) << pixelFormat.getBlueShift() | 0xff000000);
	    i++;
	}

	if (bitMaskLength > 0) {
	    ByteBuf bitmask = aloc.buffer(bitMaskLength);
	    framebuffer.getBytes(capacity - bitMaskLength, bitmask);
	    // remove transparent pixels
	    int maskBytesPerRow = Math.floorDiv((rect.getWidth() + 7), 8);
	    IntStream.range(0, rect.getHeight())
		    .forEach(y -> IntStream.range(0, rect.getWidth())
			    .filter(x -> (bitmask.getByte((y * maskBytesPerRow) + Math.floorDiv(x, 8))
				    & (1 << 7 - Math.floorMod(x, 8))) < 1)
			    .forEach(x -> pixels.setInt((y * rect.getWidth() + x) * 4, 0)));
	    bitmask.release();
	}
	out.add(new CursorImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), pixels));
    }
}
