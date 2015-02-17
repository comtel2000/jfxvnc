package org.jfxvnc.net.rfb.codec.input;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.jfxvnc.net.rfb.codec.IClientMessageType;

public class PointerEventEncoder extends MessageToByteEncoder<PointerEventMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, PointerEventMessage msg, ByteBuf out) throws Exception {
	ByteBuf buf = ctx.alloc().buffer(6);
	try {
	    buf.writeByte(IClientMessageType.POINTER_EVENT);
	    buf.writeByte(msg.getButtonMask());
	    buf.writeShort(msg.getxPos());
	    buf.writeShort(msg.getyPos());
	    out.writeBytes(buf);
	} finally {
	    buf.release();
	}
    }

}
