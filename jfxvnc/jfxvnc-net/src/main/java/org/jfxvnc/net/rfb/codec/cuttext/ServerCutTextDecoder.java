package org.jfxvnc.net.rfb.codec.cuttext;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jfxvnc.net.rfb.codec.IFrameDecoder;

public class ServerCutTextDecoder implements IFrameDecoder {

    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

	if (!in.isReadable(4)) {
	    return false;
	}

	in.markReaderIndex();
	int length = in.readInt();

	if (!in.isReadable(length)) {
	    in.resetReaderIndex();
	    return false;
	}

	byte[] text = new byte[length];
	in.readBytes(text);

	out.add(new ServerCutTextMessage(new String(text, StandardCharsets.ISO_8859_1)));
	return true;
    }

}
