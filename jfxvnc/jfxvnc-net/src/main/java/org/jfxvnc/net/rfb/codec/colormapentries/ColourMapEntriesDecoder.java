package org.jfxvnc.net.rfb.codec.colormapentries;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.jfxvnc.net.rfb.codec.IFrameDecoder;

public class ColourMapEntriesDecoder implements IFrameDecoder {

    public ColourMapEntriesDecoder() {
    }

    @Override
    public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable(12)) {
	    return false;
	}
	in.skipBytes(2);
	int firstColor = in.readUnsignedShort();
	int numberOfColor = in.readUnsignedShort();
	int red = in.readUnsignedShort();
	int green = in.readUnsignedShort();
	int blue = in.readUnsignedShort();

	out.add(new ColourMapEntries(firstColor, numberOfColor, red, green, blue));
	return true;
    }

}
