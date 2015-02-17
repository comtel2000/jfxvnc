package org.jfxvnc.net.rfb.codec.bell;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.jfxvnc.net.rfb.codec.IFrameDecoder;

public class BellDecoder implements IFrameDecoder {

    public BellDecoder() {
    }

    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	out.add(new Bell());
	return true;
    }

}
