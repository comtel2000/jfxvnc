package org.jfxvnc.net.rfb.codec.security;

/*
 * #%L
 * RFB protocol
 * %%
 * Copyright (C) 2015 comtel2000
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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

    private final SecurityType securityType;

    public RfbSecurityHandshaker(SecurityType securityType2) {
	this.securityType = securityType2;
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
	channel.writeAndFlush(Unpooled.buffer(1).writeByte(securityType.getType())).addListener(new ChannelFutureListener() {
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
