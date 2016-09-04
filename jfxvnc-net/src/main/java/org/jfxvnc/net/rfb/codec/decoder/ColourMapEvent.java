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

import io.netty.buffer.ByteBuf;

public class ColourMapEvent implements ServerDecoderEvent {

  private final int firstColor;
  private final int numberOfColor;
  private final ByteBuf colors;

  public ColourMapEvent(int firstColor, int numberOfColor, ByteBuf colors) {
    this.firstColor = firstColor;
    this.numberOfColor = numberOfColor;
    this.colors = colors;
  }

  public int getFirstColor() {
    return firstColor;
  }

  public int getNumberOfColor() {
    return numberOfColor;
  }

  public ByteBuf getColors() {
    return colors;
  }


  @Override
  public String toString() {
    return "ColourMapEvent [firstColor=" + firstColor + ", numberOfColor=" + numberOfColor + ", colors.capacity=" + colors.capacity() + "]";
  }
}
