package org.jfxvnc.net.rfb.codec.fbu;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.List;

import javax.xml.ws.ProtocolException;

import org.jfxvnc.net.rfb.codec.IEncodings;
import org.jfxvnc.net.rfb.codec.IFrameDecoder;
import org.jfxvnc.net.rfb.codec.IServerMessageType;
import org.jfxvnc.net.rfb.codec.RfbPixelFormat;
import org.jfxvnc.net.rfb.codec.RfbProtocolEvent;
import org.jfxvnc.net.rfb.rect.CopyImageRect;
import org.jfxvnc.net.rfb.rect.RawImageRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FramebufferUpdateDecoder implements IFrameDecoder {

    private static Logger logger = LoggerFactory.getLogger(FramebufferUpdateDecoder.class);

    private RfbPixelFormat pixelFormat;
    private ByteBuf framebuffer;
    private int numberRects, currentRect;
    private int x, y, w, h, enc;
    private boolean init, newRect;

    public FramebufferUpdateDecoder(RfbPixelFormat pixelFormat) {
	this.pixelFormat = pixelFormat;
	init = true;
    }

    public int[] getSupportedEncodings() {
	return new int[] { IEncodings.RAW, IEncodings.COPY_RECT };
    }

    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf m, List<Object> out) throws Exception {

	if (init) {
	    logger.debug("init readable {} bytes", m.readableBytes());
	    if (!m.isReadable()) {
		return false;
	    }
	    if (m.getByte(0) != IServerMessageType.FRAMEBUFFER_UPDATE) {
		logger.error("no FBS type!!! {}", m.getByte(0));
		ctx.fireChannelReadComplete();
		return false;
	    }
	    if (!m.isReadable(4)) {
		return false;
	    }
	    m.skipBytes(2); // padding
	    numberRects = m.readUnsignedShort();
	    currentRect = 0;
	    logger.debug("number of rectangles: {}", numberRects);
	    if (numberRects < 1) {
		return false;
	    }
	    init = false;
	    newRect = true;
	    if (framebuffer == null) {
		framebuffer = Unpooled.buffer();
	    }
	}

	if (newRect) {
	    if (!readRect(ctx, m)) {
		return false;
	    }
	    newRect = false;
	}

	if (framebuffer.isWritable() && m.isReadable()) {
	    logger.debug("write rect: {} with {} bytes remaining: {}", currentRect, m.readableBytes(), framebuffer.writableBytes());
	    framebuffer.writeBytes(m, Math.min(m.readableBytes(), framebuffer.writableBytes()));
	}

	if (!framebuffer.isWritable()) {
	    logger.debug("read FB: {} ({})", framebuffer.readableBytes(), currentRect);
	    sendRect(out);
	    framebuffer.clear();

	    if (currentRect == numberRects) {
		init = true;
		ctx.fireUserEventTriggered(RfbProtocolEvent.FBU_REQUEST);
		return true;
	    }

	    if (!readRect(ctx, m)) {
		newRect = true;
	    }

	}
	return false;
    }

    private void sendRect(List<Object> out) {
	switch (enc) {
	case IEncodings.RAW:
	    // TODO: optimize me
	    //ServerInitMessage [frameBufferWidth=1280, frameBufferHeight=720, pixelFormat=ServerPixelFormat [bitPerPixel=32, depth=24, bigEndian=false, trueColor=true, redMax=255, greenMax=255, blueMax=255, redShift=16, greenShift=8, blueShift=0], serverName=pi's X desktop (raspberrypi:1)]
	    //ServerInitMessage [frameBufferWidth=1600, frameBufferHeight=900, pixelFormat=ServerPixelFormat [bitPerPixel=32, depth=24, bigEndian=false, trueColor=true, redMax=255, greenMax=255, blueMax=255, redShift=16, greenShift=8, blueShift=0], serverName=mt-vpn-windows7]
	    int[] pixels = new int[framebuffer.capacity() / 4];
	    if (pixelFormat.isBigEndian()) {
		Arrays.setAll(pixels,
			(i) -> framebuffer.getUnsignedByte(i * 4) << pixelFormat.getRedShift() | framebuffer.getUnsignedByte((i * 4) + 1) << pixelFormat.getGreenShift()
				| framebuffer.getUnsignedByte((i * 4) + 2) << pixelFormat.getBlueShift() | 0xff000000);
	    } else {
		Arrays.setAll(pixels,
			(i) -> framebuffer.getUnsignedByte((i * 4) + 2) << pixelFormat.getRedShift() | framebuffer.getUnsignedByte((i * 4) + 1) << pixelFormat.getGreenShift()
				| framebuffer.getUnsignedByte(i * 4) << pixelFormat.getBlueShift() | 0xff000000);
	    }
	    
//	    for (int i = 0; i < pixels.length; i++) {
//		pixels[i] = (framebuffer.readByte() & 0xff) << pixelFormat.getBlueShift()| (framebuffer.readByte() & 0xff) << pixelFormat.getGreenShift() | (framebuffer.readByte() & 0xff) << pixelFormat.getRedShift() | 0xff000000;
//		framebuffer.skipBytes(1);
//	    }

	    out.add(new RawImageRect(x, y, w, h, pixels));
	    break;
	case IEncodings.COPY_RECT:
	    out.add(new CopyImageRect(x, y, w, h, framebuffer.getShort(0), framebuffer.getShort(2)));
	    break;
	default:
	    // TODO: implement alternative encodings
	    break;
	}

    }

    private boolean readRect(ChannelHandlerContext ctx, ByteBuf m) {

	if (!m.isReadable(12)) {
	    return false;
	}
	x = m.readUnsignedShort();
	y = m.readUnsignedShort();
	w = m.readUnsignedShort();
	h = m.readUnsignedShort();
	enc = m.readInt();

	logger.debug("{},{},{},{} enc({})", x, y, w, h, enc);
	currentRect++;

	switch (enc) {
	case IEncodings.RAW:
	    framebuffer.capacity(w * h * pixelFormat.getBytePerPixel());
	    break;
	case IEncodings.COPY_RECT:
	    framebuffer.capacity(4);
	    break;
	default:
	    logger.warn("not supported encoding type: {}", enc);
	    ctx.fireExceptionCaught(new ProtocolException("not supported encoding type: " + enc));
	    break;
	}
	return true;
    }

}
