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
package org.jfxvnc.net.rfb.codec.encoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jfxvnc.net.rfb.codec.ClientEventType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class ClientCutTextEncoder extends MessageToMessageEncoder<ClientCutText> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ClientCutText msg, List<Object> out) throws Exception {
    byte[] text = msg.getText().getBytes(StandardCharsets.ISO_8859_1);
    ByteBuf buf = ctx.alloc().buffer(8 + text.length);
    buf.writeByte(ClientEventType.CLIENT_CUT_TEXT);
    buf.writeZero(3);
    buf.writeInt(text.length);
    buf.writeBytes(text);

    out.add(buf);
  }

}
