package org.jfxvnc.net.rfb.codec.handshaker;

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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.codec.handshaker.event.SecurityTypesEvent;
import org.jfxvnc.net.rfb.codec.handshaker.event.ServerInitEvent;
import org.jfxvnc.net.rfb.exception.ProtocolException;
import org.jfxvnc.net.rfb.render.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RfbClient33Decoder extends ReplayingDecoder<RfbClient33Decoder.State> implements RfbClientDecoder {

    private static Logger logger = LoggerFactory.getLogger(RfbClient33Decoder.class);

    protected final Charset ASCII = StandardCharsets.US_ASCII;

    public RfbClient33Decoder() {
	super(State.SEC_TYPES);
    }

    enum State {
	SEC_TYPES, SERVER_INIT
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	switch (state()) {
	case SEC_TYPES:
	    int numberOfSecurtiyTypes = in.readUnsignedByte();
	    if (numberOfSecurtiyTypes == 0) {
		logger.error("no security types available");
		decodeErrorMessage(ctx, in);
		return;
	    }
	    int[] serverSecTypes = new int[numberOfSecurtiyTypes];
	    for (int i = 0; i < numberOfSecurtiyTypes; i++) {
		serverSecTypes[i] = in.readUnsignedByte();
	    }
	    logger.info("supported security types: {}", StringUtils.getSecurityNames(serverSecTypes));
	    checkpoint(State.SERVER_INIT);
	    out.add(new SecurityTypesEvent(serverSecTypes));
	    break;
	case SERVER_INIT:
	    ServerInitEvent initMsg = new ServerInitEvent();

	    initMsg.setFrameBufferWidth(in.readUnsignedShort());
	    initMsg.setFrameBufferHeight(in.readUnsignedShort());

	    initMsg.setPixelFormat(parsePixelFormat(in));

	    byte[] name = new byte[in.readInt()];
	    in.readBytes(name);

	    initMsg.setServerName(new String(name, ASCII));
	    out.add(initMsg);
	    break;
	default:
	    break;
	}
    }

    protected void decodeErrorMessage(ChannelHandlerContext ctx, ByteBuf in) {
	if (!in.isReadable()) {
	    ctx.fireExceptionCaught(new ProtocolException("decode error message failed"));
	    return;
	}

	byte[] reason = new byte[actualReadableBytes()];
	in.readBytes(reason);
	String error = new String(reason, ASCII);
	ctx.fireExceptionCaught(new ProtocolException(error.trim()));
    }

    protected PixelFormat parsePixelFormat(ByteBuf m) {

	PixelFormat pf = new PixelFormat();

	pf.setBitPerPixel(m.readUnsignedByte());
	pf.setDepth(m.readUnsignedByte());
	pf.setBigEndian(m.readUnsignedByte() == 1);
	pf.setTrueColor(m.readUnsignedByte() == 1);
	pf.setRedMax(m.readUnsignedShort());
	pf.setGreenMax(m.readUnsignedShort());
	pf.setBlueMax(m.readUnsignedShort());

	pf.setRedShift(m.readUnsignedByte());
	pf.setGreenShift(m.readUnsignedByte());
	pf.setBlueShift(m.readUnsignedByte());
	m.skipBytes(3);

	return pf;
    }

}
