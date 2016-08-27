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
package org.jfxvnc.net.rfb.codec.security;

import org.jfxvnc.net.rfb.codec.security.vncauth.VncAuthHandshaker;

public class RfbSecurityHandshakerFactory {

  public RfbSecurityHandshakerFactory() {}

  public RfbSecurityHandshaker newRfbSecurityHandshaker(SecurityType securityType) {

    if (securityType == SecurityType.VNC_Auth) {
      return new VncAuthHandshaker(securityType);
    }

    return null;

  }

}
