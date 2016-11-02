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
import io.netty.channel.ChannelHandlerContext;

class ColourMapEntriesDecoder implements FrameDecoder {

  enum State {
    INIT, READ_MAP;
  }

  private State state = State.INIT;
  private int firstColor, numberOfColor, bufferSize;

  public ColourMapEntriesDecoder() {}

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (state == State.INIT) {
      if (!in.isReadable(12)) {
        return false;
      }
      in.skipBytes(2);
      firstColor = in.readUnsignedShort();
      numberOfColor = in.readUnsignedShort();
      bufferSize = (numberOfColor - firstColor) * 6;
      state = State.READ_MAP;
    }
    if (state == State.READ_MAP) {
      if (!in.isReadable(bufferSize)) {
        return false;
      }
      state = State.INIT;
      return out.add(new ColourMapEvent(firstColor, numberOfColor, in.readSlice(bufferSize).retain()));
    }

    return false;
  }

}
