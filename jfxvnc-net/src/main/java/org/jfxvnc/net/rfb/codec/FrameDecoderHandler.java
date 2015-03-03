package org.jfxvnc.net.rfb.codec;

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
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.EnumMap;
import java.util.List;

import org.jfxvnc.net.rfb.codec.decoder.BellDecoder;
import org.jfxvnc.net.rfb.codec.decoder.ColourMapEntriesDecoder;
import org.jfxvnc.net.rfb.codec.decoder.FrameDecoder;
import org.jfxvnc.net.rfb.codec.decoder.FramebufferUpdateRectDecoder;
import org.jfxvnc.net.rfb.codec.decoder.ServerCutTextDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameDecoderHandler extends ByteToMessageDecoder {
    private static Logger logger = LoggerFactory.getLogger(FrameDecoderHandler.class);

    private final EnumMap<ServerEvent, FrameDecoder> frameDecoder = new EnumMap<>(ServerEvent.class);

    enum State {
	NEXT, FRAME
    }

    private State state = State.NEXT;

    private ServerEvent serverEvent;

    public FrameDecoderHandler(PixelFormat pixelFormat) {

	frameDecoder.put(ServerEvent.SET_COLOR_MAP_ENTRIES, new ColourMapEntriesDecoder());
	frameDecoder.put(ServerEvent.BELL, new BellDecoder());
	frameDecoder.put(ServerEvent.SERVER_CUT_TEXT, new ServerCutTextDecoder());
	frameDecoder.put(ServerEvent.FRAMEBUFFER_UPDATE, new FramebufferUpdateRectDecoder(pixelFormat));
    }

    public Encoding[] getSupportedEncodings() {
	return ((FramebufferUpdateRectDecoder) frameDecoder.get(ServerEvent.FRAMEBUFFER_UPDATE)).getSupportedEncodings();
    }

    public boolean isPixelFormatSupported() {
	return ((FramebufferUpdateRectDecoder) frameDecoder.get(ServerEvent.FRAMEBUFFER_UPDATE)).isPixelFormatSupported();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable()){
	    return;
	}
	
	FrameDecoder decoder;
	switch (state) {
	case NEXT:
	    serverEvent = ServerEvent.valueOf(in.getUnsignedByte(0));
	    decoder = frameDecoder.get(serverEvent);

	    if (decoder == null) {
		logger.error("not handled server message type: {} ({})", serverEvent, in.getUnsignedByte(0));
		in.skipBytes(in.readableBytes());
		return;
	    }
	    if (!decoder.decode(ctx, in, out)) {
		state = State.FRAME;
	    }
	case FRAME:
	    decoder = frameDecoder.get(serverEvent);

	    if (decoder == null) {
		logger.error("not handled server message type: {} ({})", serverEvent, in.getUnsignedByte(0));
		in.skipBytes(in.readableBytes());
		return;
	    }
	    if (decoder.decode(ctx, in, out)) {
		state = State.NEXT;
	    }
	    break;
	default:
	    logger.warn("unknown state: {}", state);
	    break;
	}
    }

}
