/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package org.jfxvnc.net.rfb.codec.handshaker;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jfxvnc.net.rfb.codec.ProtocolHandshakeHandler;
import org.jfxvnc.net.rfb.codec.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

public abstract class RfbClientHandshaker {

  private static Logger logger = LoggerFactory.getLogger(RfbClientHandshaker.class);

  public abstract RfbClientDecoder newRfbClientDecoder();

  public abstract RfbClientEncoder newRfbClientEncoder();

  private AtomicBoolean handshakeComplete = new AtomicBoolean(false);

  private final ProtocolVersion version;

  public RfbClientHandshaker(ProtocolVersion version) {
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

    channel.writeAndFlush(Unpooled.wrappedBuffer(version.getBytes())).addListener((ChannelFuture future) -> {
      if (!future.isSuccess()) {
        promise.setFailure(future.cause());
        return;
      }

      ChannelPipeline p = future.channel().pipeline();
      ChannelHandlerContext ctx = p.context(ProtocolHandshakeHandler.class);
      p.addBefore(ctx.name(), "rfb-handshake-decoder", newRfbClientDecoder());
      p.addBefore(ctx.name(), "rfb-handshake-encoder", newRfbClientEncoder());
      promise.setSuccess();

    });
    return promise;
  }

  public final void finishHandshake(Channel channel, ProtocolVersion response) {
    setHandshakeComplete();

    ChannelPipeline p = channel.pipeline();
    p.remove("rfb-handshake-decoder");
    p.remove("rfb-handshake-encoder");

    logger.debug("server {} - client {}", version, response);

  }

}
