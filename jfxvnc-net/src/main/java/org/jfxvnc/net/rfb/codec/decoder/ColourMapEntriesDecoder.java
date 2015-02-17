package org.jfxvnc.net.rfb.codec.decoder;

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

import java.util.List;

public class ColourMapEntriesDecoder implements FrameDecoder {

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

	out.add(new ColourMapEntriesEvent(firstColor, numberOfColor, red, green, blue));
	return true;
    }

}
