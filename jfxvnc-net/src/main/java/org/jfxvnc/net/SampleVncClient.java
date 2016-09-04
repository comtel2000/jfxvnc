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
package org.jfxvnc.net;

import org.jfxvnc.net.rfb.VncConnection;
import org.jfxvnc.net.rfb.codec.security.SecurityType;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;

public class SampleVncClient {

  public static void main(String[] args) throws Exception {

    VncConnection connector = new VncConnection();
    ProtocolConfiguration config = connector.getConfiguration();
    
    if (args != null && args.length >= 3) {
      config.securityProperty().set(SecurityType.VNC_Auth);
      config.hostProperty().set(args[0]);
      config.portProperty().set(Integer.parseInt(args[1]));
      config.passwordProperty().set(args[2]);
      config.sharedProperty().set(Boolean.TRUE);
    } else {
      System.err.println("arguments missing (host port password)");
      config.securityProperty().set(SecurityType.VNC_Auth);
      config.hostProperty().set("127.0.0.1");
      config.portProperty().set(5902);
      config.passwordProperty().set("vnc");
      config.sharedProperty().set(Boolean.TRUE);
    }

    connector.connect().whenComplete((c, th) -> {
      if (th != null){
        th.printStackTrace();
      }
      c.disconnect();
    }).join();
    
  }
}
