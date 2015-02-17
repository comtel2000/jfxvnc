package org.jfxvnc.net.rfb.codec.input;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.jfxvnc.net.rfb.codec.IClientMessageType;

public class KeyEventEncoder extends MessageToByteEncoder<KeyEventMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, KeyEventMessage msg, ByteBuf out) throws Exception {
	ByteBuf buf = ctx.alloc().buffer(8);
	try {
	    buf.writeByte(IClientMessageType.KEY_EVENT);
	    buf.writeBoolean(msg.isDown());
	    buf.writeZero(2);
	    buf.writeInt(msg.getKey());
	    out.writeBytes(buf);
	} finally {
	    buf.release();
	}
    }

}
