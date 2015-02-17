package org.jfxvnc.net.rfb.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jfxvnc.net.rfb.IProperty;
import org.jfxvnc.net.rfb.codec.handshaker.msg.RfbServerInitMessage;
import org.jfxvnc.net.rfb.codec.input.InputEventListener;
import org.jfxvnc.net.rfb.codec.input.InputEventMessage;
import org.jfxvnc.net.rfb.codec.input.KeyEventEncoder;
import org.jfxvnc.net.rfb.codec.input.PointerEventEncoder;
import org.jfxvnc.net.rfb.color.IPixelFormatRgb888;
import org.jfxvnc.net.rfb.rect.CanvasImageRect;
import org.jfxvnc.net.rfb.rect.ImageRect;
import org.jfxvnc.net.rfb.render.IRender;
import org.jfxvnc.net.rfb.render.RenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RfbProtocolHandler extends MessageToMessageDecoder<Object> {

    private static Logger logger = LoggerFactory.getLogger(RfbProtocolHandler.class);

    private final Map<String, Object> properties;

    private final RfbVersion clientVersion;
    private RfbServerInitMessage serverInit;
    
    private IRender render;
    private final AtomicReference<RfbProtocolEvent> state = new AtomicReference<RfbProtocolEvent>(RfbProtocolEvent.HANDSHAKE_STARTED);
    
    public RfbProtocolHandler(IRender render, Map<String, Object> prop) {
	properties = prop != null ? prop : Collections.emptyMap();
	clientVersion = prop.containsKey(IProperty.CLIENT_VERSION) ? (RfbVersion) prop.get(IProperty.CLIENT_VERSION) : RfbVersion.RFB_3_8;
	this.render = render;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connection closed");
        if (state.get() == RfbProtocolEvent.SECURITY_STARTED){
            ProtocolException e = new ProtocolException("connection closed without error message");
            render.exceptionCaught(e.getMessage(), e);
        }
        render.stateChanged(RfbProtocolEvent.CLOSED);
        super.channelInactive(ctx);
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {

	if (msg instanceof ImageRect){
	    render.render((ImageRect)msg, new RenderCallback(){
		@Override
		public void renderComplete() {
		    //logger.info("render completed");
		}
	    });
	    
	    return;
	}
	if (!(msg instanceof RfbServerInitMessage)) {
	    logger.error("unknown message: {}", msg);
	    ctx.fireChannelRead(msg);
	    return;
	}

	serverInit = (RfbServerInitMessage) msg;
	// handshake completed
	logger.info("handshake completed with {}", serverInit);
	
	logger.info("set prefered encodings..");
	sendPreferedEncodings(ctx, IEncodings.COPY_RECT, IEncodings.RAW);

	logger.info("set prefered pixelformat..");
	sendPixelFormatRgb888(ctx);
	
	CanvasImageRect rect = new CanvasImageRect(serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight(), serverInit.getServerName(), serverInit.getPixelFormat());
	

	
	ctx.pipeline().addBefore(ctx.name(), "rfb-frame-decoder", new VncFrameDecoder(serverInit.getPixelFormat()));
	
	ctx.pipeline().addBefore(ctx.name(), "rfb-keyevent-encoder", new KeyEventEncoder());
	ctx.pipeline().addBefore(ctx.name(), "rfb-pointerevent-encoder", new PointerEventEncoder());
//	ctx.pipeline().addBefore(ctx.name(), "rfb-cuttext-encoder", new ClientCutTextEncoder());
//	ctx.pipeline().addBefore(ctx.name(), "rfb-cuttext-decoder", new FramebufferUpdate2Decoder(initMessage.getPixelFormat()));

	
	render.registerInputEventListener(new InputEventListener() {
	    
	    @Override
	    public void fireInputEvent(InputEventMessage event) {
		ctx.writeAndFlush(event);
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

    
    public void sendPreferedEncodings(ChannelHandlerContext ctx, int... encodings) {
	ByteBuf setEncoding = ctx.alloc().buffer(4 + (4 * encodings.length));
	setEncoding.writeByte(IClientMessageType.SET_ENCODINGS);
	setEncoding.writeZero(1); // padding

	setEncoding.writeShort(encodings.length);
	for (int enc : encodings) {
	    setEncoding.writeInt(enc);
	}

	ctx.writeAndFlush(setEncoding);
    }

    public void sendPixelFormatRgb888(ChannelHandlerContext ctx) {
	ByteBuf buf = ctx.alloc().buffer(20);
	buf.writeByte(IClientMessageType.SET_PIXEL_FORMAT);
	buf.writeZero(3); // padding

	buf.writeByte(IPixelFormatRgb888.BITS_PER_PIXEL);
	buf.writeByte(IPixelFormatRgb888.DEPTH);
	buf.writeByte(IPixelFormatRgb888.BIG_ENDIAN_FLAG);
	buf.writeByte(IPixelFormatRgb888.TRUE_COLOUR_FLAG);
	buf.writeShort(IPixelFormatRgb888.RED_MAX);
	buf.writeShort(IPixelFormatRgb888.GREEN_MAX);
	buf.writeShort(IPixelFormatRgb888.BLUE_MAX);
	buf.writeByte(IPixelFormatRgb888.RED_SHIFT);
	buf.writeByte(IPixelFormatRgb888.GREEN_SHIFT);
	buf.writeByte(IPixelFormatRgb888.BLUE_SHIFT);

	buf.writeZero(3); // padding

	ctx.writeAndFlush(buf);
    }

    public void sendFramebufferUpdateRequest(ChannelHandlerContext ctx, boolean incremental, int x, int y, int w, int h) {
	ByteBuf buf = ctx.alloc().buffer(10);
	buf.writeByte(IClientMessageType.FRAMEBUFFER_UPDATE_REQUEST);
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
	if (cp.get(RfbProtocolHandshakeHandler.class) == null) {
	    ctx.pipeline().addBefore(ctx.name(), "rfb-handshake-handler", new RfbProtocolHandshakeHandler(clientVersion, properties));
	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	logger.error(cause.getMessage(), cause);
	render.exceptionCaught(cause.getMessage(), cause);
	ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	logger.debug("user event: {}", evt);
	if (evt instanceof RfbProtocolEvent){
	    RfbProtocolEvent uvent =  (RfbProtocolEvent)evt;
	    state.set(uvent);
	    if (uvent == RfbProtocolEvent.FBU_REQUEST){
		sendFramebufferUpdateRequest(ctx, true, 0, 0, serverInit.getFrameBufferWidth(), serverInit.getFrameBufferHeight());
	    }
	    
	    render.stateChanged(uvent);
	}
    }
}
