package org.jfxvnc.net.rfb.codec;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.IRender;

public class ProtocolInitializer extends ChannelInitializer<SocketChannel> {
    private IRender render;
    private ProtocolConfiguration config;

    public ProtocolInitializer(IRender render, ProtocolConfiguration config) {
	super();
	this.render = render;
	this.config = config;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
	ChannelPipeline pipeline = ch.pipeline();

	pipeline.addLast(new ProtocolHandler(render, config));
    }
}
