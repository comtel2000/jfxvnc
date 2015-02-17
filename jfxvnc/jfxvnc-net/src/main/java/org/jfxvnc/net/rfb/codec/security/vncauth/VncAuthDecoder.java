package org.jfxvnc.net.rfb.codec.security.vncauth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.jfxvnc.net.rfb.codec.security.RfbSecurityDecoder;

public class VncAuthDecoder extends ByteToMessageDecoder implements RfbSecurityDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable(16)) {
	    return;
	}
	byte[] challenge = new byte[16];
	in.readBytes(challenge);
	out.add(new VncAuthSecurityMessage(challenge));
    }

}
