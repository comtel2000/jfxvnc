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


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.List;

import javax.xml.ws.ProtocolException;

import org.jfxvnc.net.rfb.codec.IEncodings;
import org.jfxvnc.net.rfb.codec.ServerEventType;
import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;
import org.jfxvnc.net.rfb.render.rect.CursorImageRect;
import org.jfxvnc.net.rfb.render.rect.DesktopSizeRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FramebufferUpdateDecoder implements FrameDecoder {

    private static Logger logger = LoggerFactory.getLogger(FramebufferUpdateDecoder.class);

    private PixelFormat pixelFormat;
    private ByteBuf framebuffer;
    private int numberRects, currentRect;
    private int x, y, w, h, enc;

    private State state;
    
    enum State {
	INIT, NEW_RECT, READ_RECT
    }
    public FramebufferUpdateDecoder(PixelFormat pixelFormat) {
	this.pixelFormat = pixelFormat;
	state = State.INIT;
    }

    public int[] getSupportedEncodings() {
	return new int[] {IEncodings.COPY_RECT, IEncodings.RAW, IEncodings.CURSOR, IEncodings.DESKTOP_SIZE};
    }

    public boolean isPixelFormatSupported() {
	return PixelFormat.RGB_888.equals(pixelFormat);
    }
    
    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf m, List<Object> out) throws Exception {

	if (state == State.INIT) {
	    logger.debug("init readable {} bytes", m.readableBytes());
	    if (!m.isReadable()) {
		return false;
	    }
	    if (m.getByte(0) != ServerEventType.FRAMEBUFFER_UPDATE) {
		logger.error("no FBU type!!! {}", m.getByte(0));
		ctx.fireChannelReadComplete();
		return false;
	    }
	    if (!m.isReadable(4)) {
		return false;
	    }
	    m.skipBytes(2); // padding
	    numberRects = m.readUnsignedShort();
	    currentRect = 0;
	    logger.debug("number of rectangles: {}", numberRects);
	    if (numberRects < 1) {
		return true;
	    }
	    state = State.NEW_RECT;
	    if (framebuffer == null) {
		framebuffer = Unpooled.buffer();
	    }
	}

	if (state == State.NEW_RECT) {
	    if (!readRect(ctx, m, out)) {
		return false;
	    }
	    state = State.READ_RECT;
	}

	if (framebuffer.isWritable() && m.isReadable()) {
	    logger.debug("write rect: {} with {} bytes remaining: {}", currentRect, m.readableBytes(), framebuffer.writableBytes());
	    framebuffer.writeBytes(m, Math.min(m.readableBytes(), framebuffer.writableBytes()));
	}

	if (!framebuffer.isWritable()) {
	    logger.debug("read FB: {} ({})", framebuffer.readableBytes(), currentRect);
	    sendRect(out);
	    
	    if (currentRect == numberRects) {
		state = State.INIT;
		ctx.fireUserEventTriggered(ProtocolState.FBU_REQUEST);
		return true;
	    }
	    state = State.READ_RECT;
	    
	    if (!readRect(ctx, m, out)) {
		state = State.NEW_RECT;
	    }
	}
	
	return false;
    }

    private void sendRect(List<Object> out) {
	int[] pixels;
	int redPos = pixelFormat.isBigEndian() ? 0 : 2;
	int bluePos = pixelFormat.isBigEndian() ? 2 : 0;

	switch (enc) {
	case IEncodings.RAW:
	    // TODO: optimize me
	    pixels = new int[framebuffer.capacity() / 4];
	    if (pixels.length > 1000) {
		Arrays.parallelSetAll(pixels, (i) -> framebuffer.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift()
			| framebuffer.getUnsignedByte((i * 4) + 1) << pixelFormat.getGreenShift() | framebuffer.getUnsignedByte((i * 4) + bluePos) << pixelFormat.getBlueShift()
			| 0xff000000);
	    } else {
		Arrays.setAll(pixels,
			(i) -> framebuffer.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift() | framebuffer.getUnsignedByte((i * 4) + 1) << pixelFormat.getGreenShift()
				| framebuffer.getUnsignedByte((i * 4) + bluePos) << pixelFormat.getBlueShift() | 0xff000000);
	    }
	    out.add(new RawImageRect(x, y, w, h, pixels));
	    break;
	case IEncodings.COPY_RECT:
	    out.add(new CopyImageRect(x, y, w, h, framebuffer.getShort(0), framebuffer.getShort(2)));
	    break;
	case IEncodings.CURSOR:
	    pixels = new int[(w * h * pixelFormat.getBytePerPixel()) / 4];
	    Arrays.setAll(pixels,
		    (i) -> framebuffer.getUnsignedByte(i * 4 + redPos) << pixelFormat.getRedShift() | framebuffer.getUnsignedByte((i * 4) + 1) << pixelFormat.getGreenShift()
			    | framebuffer.getUnsignedByte((i * 4) + bluePos) << pixelFormat.getBlueShift() | 0xff000000);
	    byte[] bitmask = new byte[framebuffer.capacity() - (pixels.length * 4)];
	    framebuffer.getBytes(framebuffer.capacity() - bitmask.length, bitmask);

	    out.add(new CursorImageRect(x, y, w, h, pixels, bitmask));

	    break;
	case IEncodings.DESKTOP_SIZE:
	    out.add(new DesktopSizeRect(x, y, w, h));
	    break;
	default:
	    // TODO: implement alternative encodings
	    break;
	}
	framebuffer.clear();
    }

    private boolean readRect(ChannelHandlerContext ctx, ByteBuf m, List<Object> out) {

	if (!m.isReadable(12)) {
	    return false;
	}
	x = m.readUnsignedShort();
	y = m.readUnsignedShort();
	w = m.readUnsignedShort();
	h = m.readUnsignedShort();
	enc = m.readInt();
	currentRect++;
	
	logger.debug("{}of{} - ({}) [{},{},{},{}]", currentRect, numberRects, enc, x, y, w, h);

	if (w == 0 || h == 0){
	    if (currentRect == numberRects) {
		state = State.INIT;
		ctx.fireUserEventTriggered(ProtocolState.FBU_REQUEST);
		return true;
	    }
	    return false;
	}
	
	switch (enc) {
	case IEncodings.RAW:
	    framebuffer.capacity(w * h * pixelFormat.getBytePerPixel());
	    break;
	case IEncodings.COPY_RECT:
	    framebuffer.capacity(4);
	    break;
	case IEncodings.CURSOR:
	    int bitMaskLength = Math.floorDiv(w + 7, 8) * h;
	    framebuffer.capacity((w * h * pixelFormat.getBytePerPixel()) + bitMaskLength);
	    break;
	case IEncodings.DESKTOP_SIZE:
	    sendRect(out);
	    if (currentRect == numberRects) {
		state = State.INIT;
		ctx.fireUserEventTriggered(ProtocolState.FBU_REQUEST);
		return false;
	    }
	    break;
	default:
	    logger.warn("not supported encoding type: {}", enc);
	    ctx.fireExceptionCaught(new ProtocolException("not supported encoding type: " + enc));
	    break;
	}
	return true;
    }

}
