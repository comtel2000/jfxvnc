package org.jfxvnc.net.rfb.codec;

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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.net.rfb.codec.decoder.ServerEvent;
import org.jfxvnc.net.rfb.codec.encoder.ClientCutTextEncoder;
import org.jfxvnc.net.rfb.codec.encoder.InputEvent;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.codec.encoder.KeyButtonEventEncoder;
import org.jfxvnc.net.rfb.codec.encoder.PointerEventEncoder;
import org.jfxvnc.net.rfb.codec.handshaker.event.ServerInitEvent;
import org.jfxvnc.net.rfb.exception.ProtocolException;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.IRender;
import org.jfxvnc.net.rfb.render.RenderCallback;
import org.jfxvnc.net.rfb.render.rect.CanvasImageRect;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolHandler extends MessageToMessageDecoder<Object> {

    private static Logger logger = LoggerFactory.getLogger(ProtocolHandler.class);

    private final ProtocolConfiguration config;

    private ServerInitEvent serverInit;

    private IRender render;
    private final AtomicReference<ProtocolState> state = new AtomicReference<ProtocolState>(ProtocolState.HANDSHAKE_STARTED);

    private SslContext sslContext;

    public ProtocolHandler(IRender render, ProtocolConfiguration config) {
	if (config == null) {
	    throw new IllegalArgumentException("configuration must not be empty");
	}
	this.config = config;
	this.render = render;
    }   
    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
	if (config.sslProperty().get()) {
	    if (sslContext == null) {
		sslContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
	    }
	    ctx.pipeline().addFirst("ssl-handler", sslContext.newHandler(ctx.channel().alloc()));
	}
        super.channelRegistered(ctx);
    }

    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	logger.info("connection closed");
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
	    render.render((ImageRect) msg, new RenderCallback() {
		@Override
		public void renderComplete() {
		   // logger.debug("render completed");
		   // sendFramebufferUpdateRequest(ctx, true, 0, 0, serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight());
		}
	    });

	    return;
	}
	if (msg instanceof ServerEvent) {
	    render.eventReceived((ServerEvent) msg);
	    return;
	}
	
	if (!(msg instanceof ServerInitEvent)) {
	    logger.error("unknown message: {}", msg);
	    ctx.fireChannelRead(msg);
	    return;
	}

	serverInit = (ServerInitEvent) msg;
	logger.info("handshake completed with {}", serverInit);

	FrameDecoderHandler frameHandler = new FrameDecoderHandler(serverInit.getPixelFormat());

	ctx.pipeline().addBefore(ctx.name(), "rfb-frame-handler", frameHandler);
	ctx.pipeline().addBefore(ctx.name(), "rfb-keyevent-encoder", new KeyButtonEventEncoder());
	ctx.pipeline().addBefore(ctx.name(), "rfb-pointerevent-encoder", new PointerEventEncoder());
	ctx.pipeline().addBefore(ctx.name(), "rfb-cuttext-encoder", new ClientCutTextEncoder());
	
	sendPreferedEncodings(ctx, getPreferedEncodings(frameHandler.getSupportedEncodings()));

	
	sendPixelFormat(ctx, frameHandler.getSupportedPixelFormat());

	CanvasImageRect rect = new CanvasImageRect(serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight(), serverInit.getServerName(), serverInit.getPixelFormat());

	render.eventReceived(getConnectionDetails(ctx, frameHandler));
	
	render.registerInputEventListener(new InputEventListener() {
	    @Override
	    public void sendInputEvent(InputEvent event) {
		logger.debug("client event: {}", event);
		if (event != null){
		    ctx.writeAndFlush(event);
		}
	    }
	});

	render.render(rect, new RenderCallback() {
	    @Override
	    public void renderComplete() {
		logger.info("request full framebuffer");
		sendFramebufferUpdateRequest(ctx, false, 0, 0, serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight());
	    }
	});

    }

    private ConnectInfoEvent getConnectionDetails(ChannelHandlerContext ctx, FrameDecoderHandler handler) {
	ConnectInfoEvent details = new ConnectInfoEvent();
	details.setRemoteAddress(ctx.channel().remoteAddress().toString().substring(1));
	details.setServerName(serverInit.getServerName());
	details.setFrameWidth(serverInit.getFrameBufferWidth());
	details.setFrameHeight(serverInit.getFrameBufferHeight());
	details.setRfbProtocol(config.versionProperty().get().toString().trim());
	details.setSecurity(config.securityProperty().get());
	details.setServerPF(serverInit.getPixelFormat());
	details.setClientPF(handler.getSupportedPixelFormat());
	details.setSupportedEncodings(getPreferedEncodings(handler.getSupportedEncodings()));
	details.setConnectionType(config.sslProperty().get() ? "SSL" : "TCP (standard)");
	return details;
    }

    public int[] getPreferedEncodings(int[] supported) {
	return Arrays.stream(supported).filter((value) -> {
	    switch (value) {
	    case IEncodings.COPY_RECT:
		return config.copyRectEncProperty().get();
	    case IEncodings.RAW:
		return config.rawEncProperty().get();
	    case IEncodings.HEXTILE:
		return config.hextileEncProperty().get();
	    case IEncodings.CURSOR:
		return config.clientCursorProperty().get();
	    case IEncodings.DESKTOP_SIZE:
		return config.desktopSizeProperty().get();
	    default:
		return true;
	    }

	}).toArray();

    }

    public void sendPreferedEncodings(ChannelHandlerContext ctx, int[] encodings) {
	logger.info("set prefered encodings: {}", encodings);
	ByteBuf setEncoding = ctx.alloc().buffer(4 + (4 * encodings.length));
	setEncoding.writeByte(ClientEventType.SET_ENCODINGS);
	setEncoding.writeZero(1); // padding

	setEncoding.writeShort(encodings.length);
	for (int enc : encodings) {
	    setEncoding.writeInt(enc);
	}

	ctx.writeAndFlush(setEncoding);
    }

    public void sendPixelFormat(ChannelHandlerContext ctx, PixelFormat pf) {
	logger.info("set prefered pixelformat: {}", pf);
	ByteBuf buf = ctx.alloc().buffer(20);
	buf.writeByte(ClientEventType.SET_PIXEL_FORMAT);
	buf.writeZero(3); // padding

	buf.writeByte(pf.getBitPerPixel());
	buf.writeByte(pf.getDepth());
	buf.writeBoolean(pf.isBigEndian());
	buf.writeBoolean(pf.isTrueColor());
	buf.writeShort(pf.getRedMax());
	buf.writeShort(pf.getGreenMax());
	buf.writeShort(pf.getBlueMax());
	buf.writeByte(pf.getRedShift());
	buf.writeByte(pf.getGreenShift());
	buf.writeByte(pf.getBlueShift());
	buf.writeZero(3); // padding

	ctx.writeAndFlush(buf);
    }

    public void sendFramebufferUpdateRequest(ChannelHandlerContext ctx, boolean incremental, int x, int y, int w, int h) {
	ByteBuf buf = ctx.alloc().buffer(10);
	buf.writeByte(ClientEventType.FRAMEBUFFER_UPDATE_REQUEST);
	buf.writeByte(incremental ? 1 : 0);

	buf.writeShort(x);
	buf.writeShort(y);
	buf.writeShort(w);
	buf.writeShort(h);

	ctx.writeAndFlush(buf);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
	ChannelPipeline cp = ctx.pipeline();
	if (cp.get(ProtocolHandshakeHandler.class) == null) {
	    ctx.pipeline().addBefore(ctx.name(), "rfb-handshake-handler", new ProtocolHandshakeHandler(config));
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
	logger.debug("user event: {}", evt);
	if (evt instanceof ProtocolState) {
	    ProtocolState uvent = (ProtocolState) evt;
	    state.set(uvent);
	    if (uvent == ProtocolState.FBU_REQUEST) {
		sendFramebufferUpdateRequest(ctx, true, 0, 0, serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight());
	    }

	    render.stateChanged(uvent);
	}
    }
}
