package org.jfxvnc.net.rfb.codec.decoder.rect;

/*
 * #%L
 * jfxvnc-net
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


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.ZlibImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZlibRectDecoder extends RawRectDecoder {

    private static Logger logger = LoggerFactory.getLogger(ZlibRectDecoder.class);
    
    private final Inflater inflater;
    private boolean initialized;

    public ZlibRectDecoder(PixelFormat pixelFormat) {
	super(pixelFormat);
	initialized = false;
	inflater = new Inflater();
    }

    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!initialized) {
	    if (!in.isReadable(4)) {
		return false;
	    }
	    capacity = (int) in.readUnsignedInt();
	    initialized = true;
	}
	return super.decode(ctx, in, out);
    }

    @Override
    public void setRect(FrameRect rect) {
	this.rect = rect;
    }
    
    @Override
    protected void sendRect(List<Object> out) {
	initialized = false;
	if (framebuffer.hasArray()) {
	    inflater.setInput(framebuffer.array(), 0, framebuffer.capacity());
	} else {
	    byte[] array = new byte[framebuffer.readableBytes()];
	    framebuffer.getBytes(framebuffer.readerIndex(), array);
	    inflater.setInput(array);
	}

	byte[] result = new byte[rect.getWidth() * rect.getHeight() * pixelFormat.getBytePerPixel()];
	try {
	    int resultLength = inflater.inflate(result);
	    if (resultLength != result.length) {
		logger.error("incorrect zlib ({}/{})", resultLength, result.length);
		return;
	    }
	} catch (DataFormatException e) {
	    logger.error(e.getMessage(), e);
	    return;
	}
	int[] pixels = new int[result.length / 4];
	if (pixels.length > 5000) {
	    Arrays.parallelSetAll(pixels, (i) -> (result[i * 4 + 2] & 0xFF) << 16 | (result[i * 4 + 1] & 0xFF) << 8 | (result[i * 4] & 0xFF) | 0xff000000);
	} else {
	    Arrays.setAll(pixels, (i) -> (result[i * 4 + 2] & 0xFF) << 16 | (result[i * 4 + 1] & 0xFF) << 8 | (result[i * 4] & 0xFF) | 0xff000000);
	}
	out.add(new ZlibImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), pixels));
    }
}
