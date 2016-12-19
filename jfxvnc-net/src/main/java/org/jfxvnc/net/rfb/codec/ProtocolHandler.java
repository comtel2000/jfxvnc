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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.jfxvnc.net.rfb.codec.decoder.FrameDecoderHandler;
import org.jfxvnc.net.rfb.codec.decoder.ServerDecoderEvent;
import org.jfxvnc.net.rfb.codec.encoder.ClientCutTextEncoder;
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonEventEncoder;
import org.jfxvnc.net.rfb.codec.encoder.PixelFormatEncoder;
import org.jfxvnc.net.rfb.codec.encoder.PointerEventEncoder;
import org.jfxvnc.net.rfb.codec.encoder.PreferedEncoding;
import org.jfxvnc.net.rfb.codec.encoder.PreferedEncodingEncoder;
import org.jfxvnc.net.rfb.codec.handshaker.event.ServerInitEvent;
import org.jfxvnc.net.rfb.exception.ProtocolException;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.RenderProtocol;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class ProtocolHandler extends MessageToMessageDecoder<Object> {

  private static Logger logger = LoggerFactory.getLogger(ProtocolHandler.class);

  private final ProtocolConfiguration config;

  private ServerInitEvent serverInit;

  private RenderProtocol render;
  
  private final AtomicReference<ProtocolState> state = new AtomicReference<ProtocolState>(ProtocolState.HANDSHAKE_STARTED);

  private SslContext sslContext;

  public ProtocolHandler(RenderProtocol render, ProtocolConfiguration config) {
    this.config = Objects.requireNonNull(config, "configuration must not be empty");
    this.render = Objects.requireNonNull(render, "render must not be empty");
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    if (config.sslProperty().get()) {
      if (sslContext == null) {
        sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
      }
      ctx.pipeline().addFirst("ssl-handler", sslContext.newHandler(ctx.channel().alloc()));
    }
    super.channelRegistered(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    logger.debug("connection closed");
    if (state.get() == ProtocolState.SECURITY_STARTED) {
      ProtocolException e = new ProtocolException("connection closed without error message");
      render.exceptionCaught(e);
    }
    userEventTriggered(ctx, ProtocolState.CLOSED);
    super.channelInactive(ctx);
  }

  @Override
  protected void decode(final ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {

    if (msg instanceof ImageRect) {
      final ImageRect rect = (ImageRect) msg;
      render.render(rect);
      return;
    }
    if (msg instanceof ServerDecoderEvent) {
      final ServerDecoderEvent event = (ServerDecoderEvent) msg;
      render.eventReceived(event);
      return;
    }

    if (!(msg instanceof ServerInitEvent)) {
      logger.error("unknown message: {}", msg);
      ctx.fireChannelRead(msg);
      return;
    }

    serverInit = (ServerInitEvent) msg;
    logger.debug("handshake completed with {}", serverInit);

    FrameDecoderHandler frameHandler = new FrameDecoderHandler(serverInit.getPixelFormat());
    if (!frameHandler.isPixelFormatSupported()) {
      ProtocolException e = new ProtocolException(String.format("pixelformat: (%s bpp) not supported yet", serverInit.getPixelFormat().getBitPerPixel()));
      exceptionCaught(ctx, e);
      return;
    }

    ChannelPipeline cp = ctx.pipeline();

    cp.addBefore(ctx.name(), "rfb-encoding-encoder", new PreferedEncodingEncoder());
    PreferedEncoding prefEncodings = getPreferedEncodings(frameHandler.getSupportedEncodings());
    ctx.write(prefEncodings);

    cp.addBefore(ctx.name(), "rfb-pixelformat-encoder", new PixelFormatEncoder());
    ctx.write(serverInit.getPixelFormat());
    ctx.flush();

    cp.addBefore(ctx.name(), "rfb-frame-handler", frameHandler);
    cp.addBefore(ctx.name(), "rfb-keyevent-encoder", new KeyButtonEventEncoder());
    cp.addBefore(ctx.name(), "rfb-pointerevent-encoder", new PointerEventEncoder());
    cp.addBefore(ctx.name(), "rfb-cuttext-encoder", new ClientCutTextEncoder());

    render.eventReceived(getConnectInfoEvent(ctx, prefEncodings));

    render.registerInputEventListener(event -> ctx.writeAndFlush(event, ctx.voidPromise()));

    logger.debug("request full framebuffer update");
    sendFramebufferUpdateRequest(ctx, false, 0, 0, serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight());

    logger.trace("channel pipeline: {}", cp.toMap().keySet());
  }

  private ConnectInfoEvent getConnectInfoEvent(ChannelHandlerContext ctx, PreferedEncoding enc) {
    ConnectInfoEvent details = new ConnectInfoEvent();
    details.setRemoteAddress(ctx.channel().remoteAddress().toString().substring(1));
    details.setServerName(serverInit.getServerName());
    details.setFrameWidth(serverInit.getFrameBufferWidth());
    details.setFrameHeight(serverInit.getFrameBufferHeight());
    details.setRfbProtocol(config.versionProperty().get());
    details.setSecurity(config.securityProperty().get());
    details.setServerPF(serverInit.getPixelFormat());
    details.setClientPF(config.clientPixelFormatProperty().get());
    details.setSupportedEncodings(enc.getEncodings());
    details.setConnectionType(config.sslProperty().get() ? "SSL" : "TCP (standard)");
    return details;
  }

  public PreferedEncoding getPreferedEncodings(Encoding[] supported) {
    Encoding[] enc = Arrays.stream(supported).filter(value -> {
      switch (value) {
        case COPY_RECT:
          return config.copyRectEncProperty().get();
        case HEXTILE:
          return config.hextileEncProperty().get();
        case RAW:
          return config.rawEncProperty().get();
        case CURSOR:
          return config.clientCursorProperty().get();
        case DESKTOP_SIZE:
          return config.desktopSizeProperty().get();
        case ZLIB:
          return config.zlibEncProperty().get();
        default:
          return true;
      }
    }).toArray(Encoding[]::new);

    logger.info("encodings: {}", Arrays.toString(enc));
    return new PreferedEncoding(enc);
  }

  public void sendFramebufferUpdateRequest(ChannelHandlerContext ctx, boolean incremental, int x, int y, int w, int h) {
    ByteBuf buf = ctx.alloc().buffer(10, 10);
    buf.writeByte(ClientEventType.FRAMEBUFFER_UPDATE_REQUEST);
    buf.writeByte(incremental ? 1 : 0);

    buf.writeShort(x);
    buf.writeShort(y);
    buf.writeShort(w);
    buf.writeShort(h);

    ctx.writeAndFlush(buf, ctx.voidPromise());
  }
  
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    ChannelPipeline cp = ctx.pipeline();
    if (cp.get(ProtocolHandshakeHandler.class) == null) {
      cp.addBefore(ctx.name(), "rfb-handshake-handler", new ProtocolHandshakeHandler(config));
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.error(cause.getMessage(), cause);
    render.exceptionCaught(cause);
    ctx.close();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    logger.trace("user event: {}", evt);
    if (evt instanceof ProtocolState) {
      ProtocolState uvent = (ProtocolState) evt;
      state.set(uvent);
      if (uvent == ProtocolState.FBU_REQUEST) {
        render.renderComplete((rect) -> {
          if (rect != null){
            sendFramebufferUpdateRequest(ctx, true, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
          }else{
            sendFramebufferUpdateRequest(ctx, true, 0, 0, serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight());
          }
        });
      }

      render.stateChanged(uvent);
    }
  }
}
