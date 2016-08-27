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

import org.jfxvnc.net.rfb.codec.ClientEventType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PointerEventEncoder extends MessageToByteEncoder<PointerEvent> {

  @Override
  protected void encode(ChannelHandlerContext ctx, PointerEvent msg, ByteBuf out) throws Exception {
    ByteBuf buf = ctx.alloc().buffer(6);
    try {
      buf.writeByte(ClientEventType.POINTER_EVENT);
      buf.writeByte(msg.getButtonMask());
      buf.writeShort(msg.getxPos());
      buf.writeShort(msg.getyPos());
      out.writeBytes(buf);
    } finally {
      buf.release();
    }
  }

}
