package org.jfxvnc.net.rfb.codec.handshaker;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

import org.jfxvnc.net.rfb.codec.RfbProtocolHandshakeHandler;
import org.jfxvnc.net.rfb.codec.RfbVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RfbClientHandshaker {

    private static Logger logger = LoggerFactory.getLogger(RfbClientHandshaker.class);

    public abstract RfbClientDecoder newRfbClientDecoder();

    public abstract RfbClientEncoder newRfbClientEncoder();

    private AtomicBoolean handshakeComplete = new AtomicBoolean(false);

    private final RfbVersion version;

    public RfbClientHandshaker(RfbVersion version) {
	this.version = version;
    }

    public boolean isHandshakeComplete() {
	return handshakeComplete.get();
    }

    private void setHandshakeComplete() {
	handshakeComplete.set(true);
    }

    public ChannelFuture handshake(Channel channel) {
	return handshake(channel, channel.newPromise());
    }

    public final ChannelFuture handshake(Channel channel, final ChannelPromise promise) {

	channel.writeAndFlush(Unpooled.copiedBuffer(version.getBytes())).addListener(new ChannelFutureListener() {
	    @Override
	    public void operationComplete(ChannelFuture future) {
		if (!future.isSuccess()) {
		    promise.setFailure(future.cause());
		    return;
		}
		if (future.isSuccess()) {
		    ChannelPipeline p = future.channel().pipeline();
		    ChannelHandlerContext ctx = p.context(RfbProtocolHandshakeHandler.class);
		    p.addBefore(ctx.name(), "rfb-handshake-decoder", newRfbClientDecoder());
		    p.addBefore(ctx.name(), "rfb-handshake-encoder", newRfbClientEncoder());
		    promise.setSuccess();
		} else {
		    promise.setFailure(future.cause());
		}
	    }
	});
	return promise;
    }

    public final void finishHandshake(Channel channel, RfbVersion response) {
	setHandshakeComplete();
	
	ChannelPipeline p = channel.pipeline();
	p.remove("rfb-handshake-decoder");
	p.remove("rfb-handshake-encoder");

	logger.info("server {} - client {}", String.valueOf(version).trim(), String.valueOf(response).trim());

    }

}
