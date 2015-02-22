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

import java.util.List;

import org.jfxvnc.net.rfb.codec.decoder.BellDecoder;
import org.jfxvnc.net.rfb.codec.decoder.ColourMapEntriesDecoder;
import org.jfxvnc.net.rfb.codec.decoder.FrameDecoder;
import org.jfxvnc.net.rfb.codec.decoder.FramebufferUpdateDecoder;
import org.jfxvnc.net.rfb.codec.decoder.ServerCutTextDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameDecoderHandler extends ByteToMessageDecoder {
    private static Logger logger = LoggerFactory.getLogger(FrameDecoderHandler.class);

    enum State {
	NEXT, BELL, CUT_TEXT, COLORMAP, FBU
    }

    private State state = State.NEXT;

    private FrameDecoder serverCutTextDecoder;
    private FrameDecoder colorMapDecoder;
    private FrameDecoder bellDecoder;
    private FrameDecoder framebufferDecoder;

    public FrameDecoderHandler(PixelFormat pixelFormat) {
	colorMapDecoder = new ColourMapEntriesDecoder();
	bellDecoder = new BellDecoder();
	serverCutTextDecoder = new ServerCutTextDecoder();
	framebufferDecoder = new FramebufferUpdateDecoder(pixelFormat);
    }

    public int[] getSupportedEncodings() {
	return ((FramebufferUpdateDecoder) framebufferDecoder).getSupportedEncodings();
    }

    public boolean isPixelFormatSupported() {
	return ((FramebufferUpdateDecoder) framebufferDecoder).isPixelFormatSupported();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable()) {
	    return;
	}

	switch (state) {
	case NEXT:
	    int msg = in.getUnsignedByte(0);
	    if (msg != 0) {
		logger.info("server type message type: {} ({})", msg, in.readableBytes());
	    }
	    switch (msg) {
	    case ServerEventType.FRAMEBUFFER_UPDATE:
		state = State.FBU;
		if (framebufferDecoder.decode(ctx, in, out)) {
		    state = State.NEXT;
		}
		break;
	    case ServerEventType.SET_COLOR_MAP_ENTRIES:
		state = State.COLORMAP;
		if (colorMapDecoder.decode(ctx, in, out)) {
		    state = State.NEXT;
		}
		break;
	    case ServerEventType.BELL:
		state = State.BELL;
		if (bellDecoder.decode(ctx, in, out)) {
		    state = State.NEXT;
		}
		break;
	    case ServerEventType.SERVER_CUT_TEXT:
		state = State.CUT_TEXT;
		if (serverCutTextDecoder.decode(ctx, in, out)) {
		    state = State.NEXT;
		}
		break;
	    default:
		logger.warn("not handled server type message type: {} ({})", msg, in.readableBytes());
		in.skipBytes(in.readableBytes());
		break;
	    }
	    break;

	case FBU:
	    if (framebufferDecoder.decode(ctx, in, out)) {
		state = State.NEXT;
	    }
	    break;
	case CUT_TEXT:
	    if (serverCutTextDecoder.decode(ctx, in, out)) {
		state = State.NEXT;
	    }
	    break;
	case BELL:
	    if (bellDecoder.decode(ctx, in, out)) {
		state = State.NEXT;
	    }
	    break;
	case COLORMAP:
	    if (colorMapDecoder.decode(ctx, in, out)) {
		state = State.NEXT;
	    }
	    break;
	default:
	    logger.warn("unknown state: {}", state);
	    break;
	}
    }

}
