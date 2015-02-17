package org.jfxvnc.net.rfb.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RfbVersionDecoder extends ByteToMessageDecoder {

    protected final Charset ASCII = StandardCharsets.US_ASCII;

    private final int length = 12;

    public RfbVersionDecoder() {
	setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable(length)) {
	    return;
	}
	byte[] rfb = new byte[length];
	in.readBytes(rfb);
	String rfbVersion = new String(rfb, ASCII);
	out.add(new RfbVersion(rfbVersion));
    }

}
