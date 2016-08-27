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
package org.jfxvnc.net.rfb.codec.decoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

class ServerCutTextDecoder implements FrameDecoder {

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    if (!in.isReadable(8)) {
      return false;
    }

    in.markReaderIndex();

    in.skipBytes(4);
    int length = in.readInt();

    if (!in.isReadable(length)) {
      in.resetReaderIndex();
      return false;
    }

    byte[] text = new byte[length];
    in.readBytes(text);
    out.add(new ServerCutTextEvent(new String(text, StandardCharsets.ISO_8859_1)));
    return true;
  }

}
