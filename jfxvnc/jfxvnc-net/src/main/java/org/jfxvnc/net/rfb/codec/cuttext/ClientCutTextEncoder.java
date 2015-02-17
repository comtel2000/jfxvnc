package org.jfxvnc.net.rfb.codec.cuttext;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jfxvnc.net.rfb.codec.IClientMessageType;

public class ClientCutTextEncoder extends MessageToMessageEncoder<ClientCutTextMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ClientCutTextMessage msg, List<Object> out) throws Exception {

	byte[] text = msg.getText().getBytes(StandardCharsets.ISO_8859_1);
	ByteBuf buf = ctx.alloc().buffer(8 + text.length);
	buf.writeByte(IClientMessageType.CLIENT_CUT_TEXT);
	buf.writeZero(3);
	buf.writeInt(text.length);
	buf.writeBytes(text);

	out.add(buf);
    }

}
