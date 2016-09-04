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
package org.jfxvnc.net.rfb.codec;

import java.util.Arrays;

import org.jfxvnc.net.rfb.codec.decoder.ProtocolVersionDecoder;
import org.jfxvnc.net.rfb.codec.handshaker.RfbClientHandshaker;
import org.jfxvnc.net.rfb.codec.handshaker.RfbClientHandshakerFactory;
import org.jfxvnc.net.rfb.codec.handshaker.event.SecurityResultEvent;
import org.jfxvnc.net.rfb.codec.handshaker.event.SecurityTypesEvent;
import org.jfxvnc.net.rfb.codec.handshaker.event.ServerInitEvent;
import org.jfxvnc.net.rfb.codec.handshaker.event.SharedEvent;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityHandshaker;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityHandshakerFactory;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityMessage;
import org.jfxvnc.net.rfb.codec.security.SecurityType;
import org.jfxvnc.net.rfb.exception.ProtocolException;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

public class ProtocolHandshakeHandler extends ChannelInboundHandlerAdapter {

  private static Logger logger = LoggerFactory.getLogger(ProtocolHandshakeHandler.class);

  private RfbClientHandshaker handshaker;

  private RfbSecurityHandshaker secHandshaker;

  private final ProtocolConfiguration config;

  public ProtocolHandshakeHandler(ProtocolConfiguration config) {
    this.config = config;
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    ctx.pipeline().addBefore(ctx.name(), "rfb-version-decoder", new ProtocolVersionDecoder());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    if (msg instanceof ProtocolVersion) {
      handleServerVersion(ctx, (ProtocolVersion) msg);
      return;
    }
    if (msg instanceof SecurityTypesEvent) {
      handleSecurityTypes(ctx, (SecurityTypesEvent) msg);
      return;
    }

    if (msg instanceof RfbSecurityMessage) {
      handleSecurityMessage(ctx, (RfbSecurityMessage) msg);
      return;
    }

    if (msg instanceof SecurityResultEvent) {
      handleSecurityResult(ctx, (SecurityResultEvent) msg);
      return;
    }

    if (msg instanceof ServerInitEvent) {
      handshaker.finishHandshake(ctx.channel(), config.versionProperty().get());
      ChannelPipeline cp = ctx.pipeline();
      cp.fireUserEventTriggered(ProtocolState.HANDSHAKE_COMPLETE);
      cp.remove(this);
      cp.fireChannelRead(msg);
      return;
    }

    throw new ProtocolException("unknown message occurred: " + msg);

  }

  private void handleServerVersion(final ChannelHandlerContext ctx, ProtocolVersion version) {

    logger.debug("server version: {}", version);
    if (version.isGreaterThan(config.versionProperty().get())) {
      logger.debug("set client version: {}", config.versionProperty().get());
      version = config.versionProperty().get();
    }

    RfbClientHandshakerFactory hsFactory = new RfbClientHandshakerFactory();
    handshaker = hsFactory.newRfbClientHandshaker(version);
    handshaker.handshake(ctx.channel()).addListener((future) -> {
      if (!future.isSuccess()) {
        ctx.fireExceptionCaught(future.cause());
      } else {
        ctx.pipeline().fireUserEventTriggered(ProtocolState.HANDSHAKE_STARTED);
      }

    });
  }

  private void handleSecurityTypes(final ChannelHandlerContext ctx, SecurityTypesEvent msg) {

    SecurityType[] supportTypes = msg.getSecurityTypes();
    if (supportTypes.length == 0) {
      ctx.fireExceptionCaught(new ProtocolException("no security types supported"));
      return;
    }

    SecurityType userSecType = config.securityProperty().get();
    boolean isSupported = Arrays.stream(supportTypes).anyMatch(i -> i == userSecType);
    if (!isSupported) {
      ctx.fireExceptionCaught(new ProtocolException(
          String.format("Authentication: '%s' is not supported. The server supports only (%s)", userSecType, Arrays.toString(supportTypes))));
      return;
    }

    if (userSecType == SecurityType.NONE) {
      logger.debug("none security type available");
      ctx.writeAndFlush(Unpooled.buffer(1).writeByte(userSecType.getType()));
      ctx.pipeline().fireUserEventTriggered(ProtocolState.SECURITY_COMPLETE);
      return;
    }

    RfbSecurityHandshakerFactory secFactory = new RfbSecurityHandshakerFactory();

    secHandshaker = secFactory.newRfbSecurityHandshaker(userSecType);
    if (secHandshaker == null) {
      ctx.fireExceptionCaught(new ProtocolException(String.format("Authentication: '%s' is not supported yet", userSecType)));
      return;
    }
    secHandshaker.handshake(ctx.channel(), msg.isResponse()).addListener((future) -> {
      if (future.isSuccess()) {
        ctx.pipeline().fireUserEventTriggered(ProtocolState.SECURITY_STARTED);
      } else {
        ctx.fireExceptionCaught(future.cause());
      }
    });

  }

  private void handleSecurityMessage(final ChannelHandlerContext ctx, final RfbSecurityMessage msg) {
    msg.setCredentials(config);
    ctx.writeAndFlush(msg).addListener((future) -> {
      if (secHandshaker != null && !secHandshaker.isHandshakeComplete()) {
        secHandshaker.finishHandshake(ctx.channel(), msg);
      }
      if (!future.isSuccess()) {
        ctx.fireExceptionCaught(future.cause());
      }

    });
  }

  private void handleSecurityResult(final ChannelHandlerContext ctx, final SecurityResultEvent msg) {
    if (msg.isPassed()) {
      logger.debug("security passed: {}", msg);
      boolean sharedFlag = config.sharedProperty().get();
      ctx.writeAndFlush(new SharedEvent(sharedFlag)).addListener((future) -> {
        if (!future.isSuccess()) {
          ctx.fireExceptionCaught(future.cause());
        } else {
          ctx.pipeline().fireUserEventTriggered(ProtocolState.SECURITY_COMPLETE);
        }
      });
      return;
    }
    ctx.pipeline().fireUserEventTriggered(ProtocolState.SECURITY_FAILED);
    if (msg.getThrowable() != null) {
      ctx.fireExceptionCaught(msg.getThrowable());
    }

  }

}
