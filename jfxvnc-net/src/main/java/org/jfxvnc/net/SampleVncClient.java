package org.jfxvnc.net;

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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.net.rfb.codec.ProtocolHandler;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.net.rfb.codec.decoder.ServerEvent;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.codec.security.ISecurityType;
import org.jfxvnc.net.rfb.render.IRender;
import org.jfxvnc.net.rfb.render.RenderCallback;
import org.jfxvnc.net.rfb.render.rect.ImageRect;

public class SampleVncClient {

    public static void main(String[] args) throws Exception {

	ProtocolConfiguration config = new ProtocolConfiguration();

	if (args != null && args.length >= 3) {
	    config.securityProperty().set(ISecurityType.VNC_Auth);
	    config.hostProperty().set(args[0]);
	    config.portProperty().set(Integer.parseInt(args[1]));
	    config.passwordProperty().set(args[2]);
	    config.sharedProperty().set(Boolean.TRUE);
	} else {
	    System.err.println("arguments missing (host port password)");
	    config.securityProperty().set(ISecurityType.VNC_Auth);
	    config.hostProperty().set("127.0.0.1");
	    config.portProperty().set(5902);
	    config.passwordProperty().set("vnc");
	    config.sharedProperty().set(Boolean.TRUE);
	}

	String host = config.hostProperty().get();
	int port = config.portProperty().get();

	// final SslContext sslContext =
	// SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);

	EventLoopGroup workerGroup = new NioEventLoopGroup(1);
	try {
	    Bootstrap b = new Bootstrap();
	    b.group(workerGroup);
	    b.channel(NioSocketChannel.class);
	    b.option(ChannelOption.SO_KEEPALIVE, true);
	    b.option(ChannelOption.TCP_NODELAY, true);

	    b.handler(new ChannelInitializer<SocketChannel>() {
		@Override
		public void initChannel(SocketChannel ch) throws Exception {

		    // use ssl
		    // ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
		    ch.pipeline().addLast(new ProtocolHandler(new IRender() {

			@Override
			public void render(ImageRect rect, RenderCallback callback) {
			    System.out.println(rect);
			    callback.renderComplete();
			}

			@Override
			public void exceptionCaught(Throwable t) {
			    t.printStackTrace();
			}

			@Override
			public void stateChanged(ProtocolState state) {
			    System.out.println(state);
			}

			@Override
			public void registerInputEventListener(InputEventListener listener) {
			}

			@Override
			public void eventReceived(ServerEvent evnt) {
			    System.out.println(evnt);
			}

		    }, config));
		}
	    });

	    ChannelFuture f = b.connect(host, port).sync();

	    f.channel().closeFuture().sync();
	} finally {
	    workerGroup.shutdownGracefully();
	}
    }
}