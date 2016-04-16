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

import java.util.Arrays;
import java.util.List;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RawRectDecoder implements FrameRectDecoder {

    private static Logger logger = LoggerFactory.getLogger(RawRectDecoder.class);

    protected int capacity;
    protected FrameRect rect;
    protected PixelFormat pixelFormat;

    protected ByteBuf framebuffer;

    protected final int redPos;
    protected final int bluePos;

    public RawRectDecoder(PixelFormat pixelFormat) {
	this.pixelFormat = pixelFormat;
	this.redPos = pixelFormat.isBigEndian() ? 0 : 2;
	this.bluePos = pixelFormat.isBigEndian() ? 2 : 0;
    }

    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

	if (framebuffer == null) {
	    framebuffer = Unpooled.buffer(capacity);
	} else if (framebuffer.capacity() != capacity) {
	    framebuffer.capacity(capacity);
	}

	if (framebuffer.isWritable() && in.isReadable()) {
	    logger.debug("readable/writable {}/{}", in.readableBytes(), framebuffer.writableBytes());
	    framebuffer.writeBytes(in, Math.min(in.readableBytes(), framebuffer.writableBytes()));
	}
	if (!framebuffer.isWritable()) {
	    logger.debug("read {} raw bytes completed", framebuffer.readableBytes());
	    sendRect(out);
	    framebuffer.clear();
	    return true;
	}
	return false;
    }

    @Override
    public void setRect(FrameRect rect) {
	if (framebuffer == null || framebuffer.writerIndex() == 0) {
	    this.rect = rect;
	    this.capacity = rect.getWidth() * rect.getHeight() * pixelFormat.getBytePerPixel();
	}

    }

    protected void sendRect(List<Object> out) {
	int[] pixels = new int[capacity / 4];
	if (pixels.length > 5000) {
	    Arrays.parallelSetAll(pixels,
		    (i) -> framebuffer.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift()
			    | framebuffer.getUnsignedByte(i * 4 + 1) << pixelFormat.getGreenShift()
			    | framebuffer.getUnsignedByte(i * 4 + bluePos) << pixelFormat.getBlueShift() | 0xff000000);
	} else {
	    Arrays.setAll(pixels,
		    (i) -> framebuffer.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift()
			    | framebuffer.getUnsignedByte(i * 4 + 1) << pixelFormat.getGreenShift()
			    | framebuffer.getUnsignedByte(i * 4 + bluePos) << pixelFormat.getBlueShift() | 0xff000000);
	}
	out.add(new RawImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), pixels));
    }
}
