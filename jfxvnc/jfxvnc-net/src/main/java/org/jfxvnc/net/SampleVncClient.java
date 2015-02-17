package org.jfxvnc.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.jfxvnc.net.rfb.IProperty;
import org.jfxvnc.net.rfb.codec.RfbProtocolEvent;
import org.jfxvnc.net.rfb.codec.RfbProtocolHandler;
import org.jfxvnc.net.rfb.codec.input.InputEventListener;
import org.jfxvnc.net.rfb.codec.security.ISecurityType;
import org.jfxvnc.net.rfb.rect.ImageRect;
import org.jfxvnc.net.rfb.render.ConnectionDetails;
import org.jfxvnc.net.rfb.render.IRender;
import org.jfxvnc.net.rfb.render.RenderCallback;

public class SampleVncClient {

    public static void main(String[] args) throws Exception {

	Map<String, Object> properties = new HashMap<>();

	if (args != null && args.length >= 3) {
	    properties.put(IProperty.SECURITY_TYPE, ISecurityType.VNC_Auth);
	    properties.put(IProperty.HOST, args[0]);
	    properties.put(IProperty.PORT, Integer.parseInt(args[1]));
	    properties.put(IProperty.PASSWORD, args[2]);
	    properties.put(IProperty.SHARED_FLAG, Boolean.TRUE);
	} else {
	    System.err.println("arguments missing (host port password)");
	    properties.put(IProperty.SECURITY_TYPE, ISecurityType.VNC_Auth);
	    properties.put(IProperty.HOST, "127.0.0.1");
	    properties.put(IProperty.PORT, 5902);
	    properties.put(IProperty.PASSWORD, "comtel");
	    properties.put(IProperty.SHARED_FLAG, Boolean.TRUE);
	}

	String host = String.valueOf(properties.get(IProperty.HOST));
	int port = (int) properties.get(IProperty.PORT);

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
		    ch.pipeline().addLast(new RfbProtocolHandler(new IRender() {

			@Override
			public void render(ImageRect rect, RenderCallback callback) {
			    System.err.println(rect);
			    callback.renderComplete();
			}

			@Override
			public void exceptionCaught(String msg, Throwable t) {
			}

			@Override
			public void stateChanged(RfbProtocolEvent state) {
			}

			@Override
			public void registerInputEventListener(InputEventListener listener) {
			}

			@Override
			public void showInformation(ConnectionDetails details) {
			}
		    }, properties));
		}
	    });

	    ChannelFuture f = b.connect(host, port).sync();

	    f.channel().closeFuture().sync();
	} finally {
	    workerGroup.shutdownGracefully();
	}
    }
}