package org.jfxvnc.net.rfb.codec.security;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jfxvnc.net.rfb.codec.handshaker.RfbClientDecoder;
import org.jfxvnc.net.rfb.codec.handshaker.RfbClientEncoder;

public abstract class RfbSecurityHandshaker {

    public abstract RfbSecurityDecoder newSecurityDecoder();

    public abstract RfbSecurityEncoder newSecurityEncoder();
    
    private AtomicBoolean handshakeComplete = new AtomicBoolean(false);

    private final int securityType;

    public RfbSecurityHandshaker(int securityType) {
	this.securityType = securityType;
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
	channel.writeAndFlush(Unpooled.buffer(1).writeByte(securityType)).addListener(new ChannelFutureListener() {
	    @Override
	    public void operationComplete(ChannelFuture future) {
		if (!future.isSuccess()) {
		    promise.setFailure(future.cause());
		    return;
		}
		if (future.isSuccess()) {

		    ChannelPipeline p = future.channel().pipeline();
		    ChannelHandlerContext ctx = p.context(RfbClientDecoder.class);
		    p.addBefore(ctx.name(), "rfb-security-decoder", newSecurityDecoder());

		    ChannelHandlerContext ctx2 = p.context(RfbClientEncoder.class);
		    p.addBefore(ctx2.name(), "rfb-security-encoder", newSecurityEncoder());

		    promise.setSuccess();
		} else {
		    promise.setFailure(future.cause());
		}
	    }
	});
	return promise;
    }

    public final void finishHandshake(Channel channel, RfbSecurityMessage message) {
	setHandshakeComplete();

	ChannelPipeline p = channel.pipeline();
	p.remove("rfb-security-decoder");
	p.remove("rfb-security-encoder");

    }

}
