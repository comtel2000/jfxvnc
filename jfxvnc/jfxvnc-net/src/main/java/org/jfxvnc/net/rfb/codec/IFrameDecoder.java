package org.jfxvnc.net.rfb.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public interface IFrameDecoder {

    boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
}
