package org.jfxvnc.net.rfb.codec.handshaker;

import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbMessage;
import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbSharedMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RfbClient33Encoder extends MessageToByteEncoder<RfbMessage> implements RfbClientEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, RfbMessage msg, ByteBuf out) throws Exception {
	if (msg instanceof RfbSharedMessage) {
	    out.writeBoolean(((RfbSharedMessage) msg).isShared());
	}
    }

}
