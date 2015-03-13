package org.jfxvnc.net.rfb.codec;

/*
 * #%L
 * jfxvnc-net
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


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.RenderProtocol;

public class ProtocolInitializer extends ChannelInitializer<SocketChannel> {
    private RenderProtocol render;
    private ProtocolConfiguration config;

    public ProtocolInitializer(RenderProtocol render, ProtocolConfiguration config) {
	super();
	this.render = render;
	this.config = config;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
	ChannelPipeline pipeline = ch.pipeline();

	pipeline.addLast(new ProtocolHandler(render, config));
    }
}
