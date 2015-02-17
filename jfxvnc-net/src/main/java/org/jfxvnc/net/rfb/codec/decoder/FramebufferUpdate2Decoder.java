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
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import javax.xml.ws.ProtocolException;

import org.jfxvnc.net.rfb.codec.IEncodings;
import org.jfxvnc.net.rfb.codec.ServerEventType;
import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;
import org.jfxvnc.net.rfb.render.rect.RawImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FramebufferUpdate2Decoder extends ByteToMessageDecoder  {

    private static Logger logger = LoggerFactory.getLogger(FramebufferUpdate2Decoder.class);

    private PixelFormat pixelFormat;
    private ByteBuf framebuffer;
    private int numberRects, currentRect;
    private int x, y, w, h, enc;
    private boolean init, newRect;

    public FramebufferUpdate2Decoder(PixelFormat pixelFormat) {
	this.pixelFormat = pixelFormat;
	init = true;
    }

    public int[] getSupportedEncodings() {
	return new int[] { IEncodings.RAW, IEncodings.COPY_RECT };
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf m, List<Object> out) throws Exception {

	if (init) {
	    logger.debug("init readable {} bytes", m.readableBytes());
	    if (!m.isReadable()) {
		return;
	    }
	    if (m.getByte(0) != ServerEventType.FRAMEBUFFER_UPDATE) {
		logger.error("no FBS type!!! {}", m.getByte(0));
		ctx.fireChannelReadComplete();
		return;
	    }
	    if (!m.isReadable(4)) {
		return;
	    }
	    m.skipBytes(2); // padding
	    numberRects = m.readUnsignedShort();
	    currentRect = 0;
	    logger.debug("number of rectangles: {}", numberRects);
	    if (numberRects < 1) {
		return;
	    }
	    init = false;
	    newRect = true;
	    if (framebuffer == null) {
		framebuffer = Unpooled.buffer();
	    }
	}

	if (newRect) {
	    if (!readRect(ctx, m)) {
		return;
	    }
	    newRect = false;
	}

	if (framebuffer.isWritable() && m.isReadable()) {
	    logger.debug("write rect: {} with {} bytes remaining: {}", currentRect, m.readableBytes(), framebuffer.writableBytes());
	    framebuffer.writeBytes(m, Math.min(m.readableBytes(), framebuffer.writableBytes()));
	}

	if (!framebuffer.isWritable()) {
	    logger.debug("read FB: {} ({})", framebuffer.readableBytes(), currentRect);
	    sendRect(out);
	    framebuffer.clear();

	    if (currentRect == numberRects) {
		init = true;
		ctx.fireUserEventTriggered(ProtocolState.FBU_REQUEST);
		return;
	    }

	    if (!readRect(ctx, m)) {
		newRect = true;
	    }

	}
    }

    private void sendRect(List<Object> out) {
	switch (enc) {
	case IEncodings.RAW:
	    // TODO: optimize me
	    int[] pixels = new int[framebuffer.capacity() / 4];
	    for (int i = 0; i < pixels.length; i++) {
		pixels[i] = (framebuffer.readByte() & 0xff) << pixelFormat.getRedShift()| (framebuffer.readByte() & 0xff) << pixelFormat.getGreenShift() | (framebuffer.readByte() & 0xff) << pixelFormat.getBlueShift() | 0xff000000;
		framebuffer.skipBytes(1);
	    }

	    out.add(new RawImageRect(x, y, w, h, pixels));
	    break;
	case IEncodings.COPY_RECT:
	    out.add(new CopyImageRect(x, y, w, h, framebuffer.getShort(0), framebuffer.getShort(2)));
	    break;
	default:
	    // TODO: implement alternative encodings
	    break;
	}

    }

    private boolean readRect(ChannelHandlerContext ctx, ByteBuf m) {

	if (!m.isReadable(12)) {
	    return false;
	}
	x = m.readUnsignedShort();
	y = m.readUnsignedShort();
	w = m.readUnsignedShort();
	h = m.readUnsignedShort();
	enc = m.readInt();

	logger.debug("{},{},{},{} enc({})", x, y, w, h, enc);
	currentRect++;

	switch (enc) {
	case IEncodings.RAW:
	    framebuffer.capacity(w * h * pixelFormat.getBytePerPixel());
	    break;
	case IEncodings.COPY_RECT:
	    framebuffer.capacity(4);
	    break;
	default:
	    logger.warn("not supported encoding type: {}", enc);
	    ctx.fireExceptionCaught(new ProtocolException("not supported encoding type: " + enc));
	    break;
	}
	return true;
    }

}
