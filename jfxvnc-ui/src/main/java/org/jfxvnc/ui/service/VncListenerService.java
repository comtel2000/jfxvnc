package org.jfxvnc.ui.service;

/*
 * #%L
 * jfxvnc-ui
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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.TimeUnit;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

public class VncListenerService extends Service<Void> {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncListenerService.class);

    private static final int PORT = 5500;

    private EventLoopGroup workerGroup;

    private NioEventLoopGroup bossGroup;

    public VncListenerService() {

    }

    private void startListening() throws Exception {
	if (workerGroup != null && !workerGroup.isTerminated()) {
	    workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
	}
	if (bossGroup != null && !bossGroup.isTerminated()) {
	    bossGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
	}

	bossGroup = new NioEventLoopGroup(1);
	workerGroup = new NioEventLoopGroup(1);

	ServerBootstrap b = new ServerBootstrap();
	b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
		.childHandler(new ChannelInitializer<SocketChannel>() {
		    @Override
		    public void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline p = ch.pipeline();
			// p.addLast(new EchoServerHandler());
		    }
		});

	ChannelFuture f = b.bind(PORT).sync();

    }

    @PreDestroy
    public void disconnect() {
	cancel();
	if (workerGroup != null) {
	    workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
	}
	if (bossGroup != null) {
	    bossGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
	}
    }

    @Override
    protected Task<Void> createTask() {
	return new Task<Void>() {
	    @Override
	    protected Void call() throws Exception {
		try {
		    startListening();
		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		}
		return null;
	    }
	};
    }

}
