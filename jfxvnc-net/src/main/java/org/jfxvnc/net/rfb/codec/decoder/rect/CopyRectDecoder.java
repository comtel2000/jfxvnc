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
package org.jfxvnc.net.rfb.codec.decoder.rect;

import java.util.List;

import org.jfxvnc.net.rfb.codec.PixelFormat;
import org.jfxvnc.net.rfb.render.rect.CopyImageRect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CopyRectDecoder implements FrameRectDecoder {

  private FrameRect rect;

  public CopyRectDecoder(PixelFormat pixelFormat) {}

  @Override
  public boolean decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    if (!in.isReadable(4)) {
      return false;
    }
    int sryX = in.readShort();
    int srcY = in.readShort();

    out.add(new CopyImageRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), sryX, srcY));
    return true;
  }

  @Override
  public void setRect(FrameRect rect) {
    this.rect = rect;
  }

}
