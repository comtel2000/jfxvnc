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
package org.jfxvnc.net.rfb.codec.security.vncauth;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.jfxvnc.net.rfb.codec.security.RfbSecurityEncoder;
import org.jfxvnc.net.rfb.exception.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VncAuthEncoder extends MessageToByteEncoder<VncAuthSecurityMessage> implements RfbSecurityEncoder {

  private static Logger logger = LoggerFactory.getLogger(VncAuthEncoder.class);

  @Override
  protected void encode(ChannelHandlerContext ctx, VncAuthSecurityMessage msg, ByteBuf out) throws Exception {
    byte[] enc = encryptPassword(msg);
    logger.debug("VNC Auth encrypted: {}", enc);
    out.writeBytes(enc);
  }

  private byte[] encryptPassword(VncAuthSecurityMessage msg) throws ProtocolException {
    if (msg.getChallenge().length != 16)
      throw new ProtocolException("invalid challenge length " + msg.getChallenge().length);
    try {
      byte[] keyBytes = new byte[DESKeySpec.DES_KEY_LEN];
      byte[] pwdBytes = String.valueOf(msg.getPassword()).getBytes(StandardCharsets.US_ASCII);

      for (int i = 0; i < keyBytes.length; i++) {
        keyBytes[i] = i < pwdBytes.length ? reverseBitsByte(pwdBytes[i]) : 0;
      }

      KeySpec desKeySpec = new DESKeySpec(keyBytes);
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
      Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);

      return cipher.doFinal(msg.getChallenge());

    } catch (Exception e) {
      throw new ProtocolException("encrypt password failed", e);
    }
  }

  private byte reverseBitsByte(byte b) {
    byte f = 0;
    for (int position = 7; position >= 0; position--) {
      f += ((b & 1) << position);
      b >>= 1;
    }
    return f;
  }

}
