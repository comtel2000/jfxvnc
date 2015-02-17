package org.jfxvnc.net.rfb.codec.handshaker;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbMessage;

public class RfbClient38Encoder extends RfbClient33Encoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, RfbMessage msg, ByteBuf out) throws Exception {
	super.encode(ctx, msg, out);
    }

}
