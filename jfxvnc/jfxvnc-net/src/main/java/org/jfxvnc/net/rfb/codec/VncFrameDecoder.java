package org.jfxvnc.net.rfb.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.jfxvnc.net.rfb.codec.bell.BellDecoder;
import org.jfxvnc.net.rfb.codec.colormapentries.ColourMapEntriesDecoder;
import org.jfxvnc.net.rfb.codec.cuttext.ServerCutTextDecoder;
import org.jfxvnc.net.rfb.codec.fbu.FramebufferUpdateDecoder;

public class VncFrameDecoder extends ByteToMessageDecoder {

    enum Decoder {
	NEXT, BELL, CUT_TEXT, COLORMAP, FBU
    }

    private Decoder state = Decoder.NEXT;

    private IFrameDecoder serverCutTextDecoder;
    private IFrameDecoder colorMapDecoder;
    private IFrameDecoder bellDecoder;
    private IFrameDecoder framebufferDecoder;

    public VncFrameDecoder(RfbPixelFormat pixelFormat) {
	colorMapDecoder = new ColourMapEntriesDecoder();
	bellDecoder = new BellDecoder();
	serverCutTextDecoder = new ServerCutTextDecoder();
	framebufferDecoder = new FramebufferUpdateDecoder(pixelFormat);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable()) {
	    return;
	}

	switch (state) {
	case NEXT:
	    int msg = in.getUnsignedByte(0);
	    switch (msg) {
	    case IServerMessageType.FRAMEBUFFER_UPDATE:
		state = Decoder.FBU;
		if (framebufferDecoder.decode(ctx, in, out)) {
		    state = Decoder.NEXT;
		}
		break;
	    case IServerMessageType.SET_COLOR_MAP_ENTRIES:
		state = Decoder.COLORMAP;
		if (colorMapDecoder.decode(ctx, in, out)) {
		    state = Decoder.NEXT;
		}
		break;
	    case IServerMessageType.BELL:
		state = Decoder.BELL;
		if (bellDecoder.decode(ctx, in, out)) {
		    state = Decoder.NEXT;
		}
		break;
	    case IServerMessageType.SERVER_CUT_TEXT:
		state = Decoder.CUT_TEXT;
		if (serverCutTextDecoder.decode(ctx, in, out)) {
		    state = Decoder.NEXT;
		}
		break;
	    default:
		break;
	    }
	    break;

	case FBU:
	    if (framebufferDecoder.decode(ctx, in, out)) {
		state = Decoder.NEXT;
	    }
	    break;
	case CUT_TEXT:
	    if (serverCutTextDecoder.decode(ctx, in, out)) {
		state = Decoder.NEXT;
	    }
	    break;
	case BELL:
	    if (bellDecoder.decode(ctx, in, out)) {
		state = Decoder.NEXT;
	    }
	    break;
	case COLORMAP:
	    if (colorMapDecoder.decode(ctx, in, out)) {
		state = Decoder.NEXT;
	    }
	    break;
	default:
	    break;
	}
    }

}
