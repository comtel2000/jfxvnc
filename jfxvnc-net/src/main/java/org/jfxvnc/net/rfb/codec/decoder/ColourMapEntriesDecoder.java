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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

class ColourMapEntriesDecoder implements FrameDecoder {

  protected ByteBuf colorBuf;

  private int firstColor;
  private int numberOfColor;

  public ColourMapEntriesDecoder() {
  }

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    if (colorBuf == null) {
      if (!in.isReadable(12)) {
        return false;
      }
      in.skipBytes(2);
      firstColor = in.readUnsignedShort();
      numberOfColor = in.readUnsignedShort();
      int size = numberOfColor - firstColor;
      colorBuf = Unpooled.buffer(size * 6, size * 6);
    }
    colorBuf.writeBytes(in);

    if (!colorBuf.isWritable()) {
      return out.add(new ColourMapEntriesEvent(firstColor, numberOfColor, colorBuf));
    }
    return false;
  }

}
