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
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jfxvnc.net.rfb.codec.ProtocolVersion;

public class ProtocolVersionDecoder extends ByteToMessageDecoder {

    protected final Charset ASCII = StandardCharsets.US_ASCII;

    private final int length = 12;

    public ProtocolVersionDecoder() {
	setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
	if (!in.isReadable(length)) {
	    return;
	}
	byte[] rfb = new byte[length];
	in.readBytes(rfb);
	String rfbVersion = new String(rfb, ASCII);
	out.add(new ProtocolVersion(rfbVersion));
	ctx.pipeline().remove(this);
    }

}
