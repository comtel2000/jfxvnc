package org.jfxvnc.net.rfb.codec.security;

/*
 * #%L
 * RFB protocol
 * %%
 * Copyright (C) 2015 comtel2000
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


public interface ISecurityType {

    int INVALID = 0;
    int NONE = 1;
    int VNC_Auth = 2;
    int RA2 = 5;
    int RA2ne = 6;
    int Tight = 16;
    int Ultra = 17;
    int TLS = 18;
    int VeNCrypt = 19;
    int GTK_VNC_SAS = 20;
    int MD5 = 21;
    int Colin_Dean_xvp = 22;

}
