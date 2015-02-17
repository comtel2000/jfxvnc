package org.jfxvnc.net.rfb.codec;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Arrays;
import java.util.Map;

import org.jfxvnc.net.rfb.IProperty;
import org.jfxvnc.net.rfb.codec.handshaker.RfbClientHandshaker;
import org.jfxvnc.net.rfb.codec.handshaker.RfbClientHandshakerFactory;
import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbSecurityResultMessage;
import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbSecurityTypesMessage;
import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbServerInitMessage;
import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbSharedMessage;
import org.jfxvnc.net.rfb.codec.security.ISecurityType;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityHandshaker;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityHandshakerFactory;
import org.jfxvnc.net.rfb.codec.security.RfbSecurityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RfbProtocolHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(RfbProtocolHandshakeHandler.class);

    private final RfbVersion clientVersion;

    private RfbClientHandshaker handshaker;

    private RfbSecurityHandshaker secHandshaker;

    private final Map<String, Object> properties;

    public RfbProtocolHandshakeHandler(RfbVersion clientVersion, Map<String, Object> properties) {
	this.clientVersion = clientVersion;
	this.properties = properties;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
	super.channelActive(ctx);
	ctx.pipeline().addBefore(ctx.name(), "rfb-version-decoder", new RfbVersionDecoder());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

	if (msg instanceof RfbVersion) {
	    handleServerVersion(ctx, (RfbVersion) msg);
	    return;
	}
	if (msg instanceof RfbSecurityTypesMessage) {
	    handleSecurityTypes(ctx, (RfbSecurityTypesMessage) msg);
	    return;
	}
	if (msg instanceof RfbSecurityMessage) {
	    handleSecurityMessage(ctx, (RfbSecurityMessage) msg);
	    return;
	}

	if (msg instanceof RfbSecurityResultMessage) {
	    handleSecurityResult(ctx, (RfbSecurityResultMessage) msg);
	    return;
	}

	if (msg instanceof RfbServerInitMessage) {
	    ctx.fireChannelRead(msg);
	} else {
	    logger.warn("unknown message: {}", msg);
	}

	if (handshaker != null && !handshaker.isHandshakeComplete()) {
	    handshaker.finishHandshake(ctx.channel(), clientVersion);
	    ctx.fireUserEventTriggered(RfbProtocolEvent.HANDSHAKE_COMPLETE);
	    ctx.pipeline().remove(this);
	    return;
	}
	throw new IllegalStateException("RFB handshaker should have been non finished yet");

    }

    private void handleServerVersion(ChannelHandlerContext ctx, RfbVersion version) {

	logger.info("server version: {}", version.toString().trim());
	if (version.isGreaterThan(clientVersion)) {
	    logger.info("set client version: {}", clientVersion);
	    version = clientVersion;
	}

	RfbClientHandshakerFactory hsFactory = new RfbClientHandshakerFactory();
	handshaker = hsFactory.newRfbClientHandshaker(version);
	handshaker.handshake(ctx.channel()).addListener(new ChannelFutureListener() {
	    @Override
	    public void operationComplete(ChannelFuture future) throws Exception {
		future.channel().pipeline().remove("rfb-version-decoder");
		if (!future.isSuccess()) {
		    ctx.fireExceptionCaught(future.cause());
		} else {
		    ctx.fireUserEventTriggered(RfbProtocolEvent.HANDSHAKE_STARTED);
		}

	    }
	});
    }

    private void handleSecurityTypes(ChannelHandlerContext ctx, RfbSecurityTypesMessage msg) {

	int[] supportTypes = msg.getSecurityTypes();
	if (supportTypes.length == 0) {
	    ctx.fireExceptionCaught(new ProtocolException("no security types supported"));
	    return;
	}

	RfbSecurityHandshakerFactory secFactory = new RfbSecurityHandshakerFactory();
	int userSecType = properties.containsKey(IProperty.SECURITY_TYPE) ? (Integer) properties.get(IProperty.SECURITY_TYPE) : ISecurityType.VNC_Auth;

	for (int type : supportTypes) {
	    if (type == userSecType) {
		if (type == ISecurityType.NONE) {
		    logger.info("no security supported");
		    ctx.fireUserEventTriggered(RfbProtocolEvent.SECURITY_COMPLETE);
		    return;
		}
		secHandshaker = secFactory.newRfbSecurityHandshaker(userSecType);
		if (secHandshaker == null) {
		    ctx.fireExceptionCaught(new ProtocolException(String.format("user authentication type: %s not supported by implementation", userSecType)));
		    return;
		}
		secHandshaker.handshake(ctx.channel()).addListener(new ChannelFutureListener() {
		    @Override
		    public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
			    ctx.fireExceptionCaught(future.cause());
			} else {
			    ctx.fireUserEventTriggered(RfbProtocolEvent.SECURITY_STARTED);
			}
		    }
		});
		return;
	    }
	}

	ctx.fireExceptionCaught(new ProtocolException(String.format("user authentication type: %s not supported by server (%s)", userSecType, Arrays.toString(supportTypes))));

    }

    private void handleSecurityMessage(ChannelHandlerContext ctx, RfbSecurityMessage msg) {
	msg.setCredentials(properties);
	ctx.writeAndFlush(msg).addListener(new ChannelFutureListener() {
	    @Override
	    public void operationComplete(ChannelFuture future) throws Exception {
		if (secHandshaker != null && !secHandshaker.isHandshakeComplete()) {
		    secHandshaker.finishHandshake(ctx.channel(), msg);
		}
		if (!future.isSuccess()) {
		    ctx.fireExceptionCaught(future.cause());
		}

	    }
	});
    }

    private void handleSecurityResult(ChannelHandlerContext ctx, RfbSecurityResultMessage msg) {
	if (msg.isPassed()) {
	    logger.info("security passed: {}", msg);
	    boolean sharedFlag = properties.containsKey(IProperty.SHARED_FLAG) ? (Boolean) properties.get(IProperty.SHARED_FLAG) : true;
	    ctx.writeAndFlush(new RfbSharedMessage(sharedFlag)).addListener(new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
		    if (!future.isSuccess()) {
			ctx.fireExceptionCaught(future.cause());
		    } else {
			ctx.fireUserEventTriggered(RfbProtocolEvent.SECURITY_COMPLETE);
		    }
		}
	    });
	    return;
	}
	ctx.fireUserEventTriggered(RfbProtocolEvent.SECURITY_FAILED);
	if (msg.getThrowable() != null) {
	    ctx.fireExceptionCaught(msg.getThrowable());
	}

    }

}
