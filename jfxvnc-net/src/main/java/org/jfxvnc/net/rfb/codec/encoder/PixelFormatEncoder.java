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
import org.jfxvnc.net.rfb.codec.PixelFormat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PixelFormatEncoder extends MessageToByteEncoder<PixelFormat> {

  @Override
  protected void encode(ChannelHandlerContext ctx, PixelFormat pf, ByteBuf out) throws Exception {
    out.writeByte(ClientEventType.SET_PIXEL_FORMAT);
    out.writeZero(3); // padding
    out.writeByte(pf.getBitPerPixel());
    out.writeByte(pf.getDepth());
    out.writeBoolean(pf.isBigEndian());
    out.writeBoolean(pf.isTrueColor());
    out.writeShort(pf.getRedMax());
    out.writeShort(pf.getGreenMax());
    out.writeShort(pf.getBlueMax());
    out.writeByte(pf.getRedShift());
    out.writeByte(pf.getGreenShift());
    out.writeByte(pf.getBlueShift());
    out.writeZero(3); // padding

    ctx.pipeline().remove(this);
  }
}
