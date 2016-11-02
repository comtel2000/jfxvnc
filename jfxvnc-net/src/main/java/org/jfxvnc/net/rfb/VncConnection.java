package org.jfxvnc.net.rfb;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;

import org.jfxvnc.net.rfb.codec.ProtocolInitializer;
import org.jfxvnc.net.rfb.render.DefaultProtocolConfiguration;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.RenderProtocol;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class VncConnection {

  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncConnection.class);

  private static final int DEF_CONNECT_PORT = 5900;
  private static final int DEF_LISTENING_PORT = 5500;

  private final ProtocolConfiguration config;

  private final ReadOnlyBooleanWrapper connected = new ReadOnlyBooleanWrapper(false);
  private final ReadOnlyBooleanWrapper connecting = new ReadOnlyBooleanWrapper(false);

  private final ThreadFactory executor;

  private NioEventLoopGroup workerGroup;

  private RenderProtocol render;

  private NioEventLoopGroup bossGroup;

  public VncConnection() {
    this(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("vnc-connection-" + t.getId());
        t.setDaemon(true);
        return t;
      }
    });
  }

  public VncConnection(ThreadFactory executor) {
    this.executor = Objects.requireNonNull(executor);
    this.config = new DefaultProtocolConfiguration();
  }

  public ProtocolConfiguration getConfiguration() {
    return this.config;
  }

  public void setRenderProtocol(RenderProtocol render) {
    this.render = Objects.requireNonNull(render);
  }

  public void addFaultListener(Consumer<Throwable> l) {}

  public void removeFaultListener(Consumer<Throwable> l) {}

  public CompletableFuture<VncConnection> connect() {
    shutdown();
    CompletableFuture<VncConnection> future = new CompletableFuture<>();

    workerGroup = new NioEventLoopGroup(2, executor);

    connecting.set(true);

    String host = config.hostProperty().get();
    int port = config.portProperty().get() > 0 ? config.portProperty().get() : DEF_CONNECT_PORT;

    Bootstrap b = new Bootstrap();
    b.group(workerGroup);
    b.channel(NioSocketChannel.class);
    b.option(ChannelOption.SO_KEEPALIVE, true);
    b.option(ChannelOption.TCP_NODELAY, true);
    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

    b.handler(new ProtocolInitializer(render, config));

    logger.debug("try to connect to {}:{}", host, port);
    b.connect(host, port).addListener((ChannelFuture in) -> {
      connecting.set(false);
      connected.set(in.isSuccess());
      if (!in.isSuccess()) {
        future.completeExceptionally(in.cause());
        return;
      }
      future.complete(this);
    });

    return future;
  }

  public CompletableFuture<VncConnection> startListeningMode() {

    shutdown();
    CompletableFuture<VncConnection> future = new CompletableFuture<>();
    connected.set(true);

    bossGroup = new NioEventLoopGroup(1, executor);
    workerGroup = new NioEventLoopGroup(2, executor);

    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100);

    b.childHandler(new ProtocolInitializer(render, config));
    int port = config.listeningPortProperty().get() > 0 ? config.listeningPortProperty().get() : DEF_LISTENING_PORT;

    b.bind(port).addListener(l -> {
      logger.debug("wait for incoming connection request on port: {}..", port);
      connected.set(l.isSuccess());
    }).addListener((ChannelFuture in) -> {
      if (!in.isSuccess()) {
        connecting.set(false);
        connected.set(false);
        future.completeExceptionally(in.cause());
      }
      future.complete(this);
    });

    return future;

  }

  @PreDestroy
  private void shutdown() {
    if (workerGroup != null && !workerGroup.isTerminated()) {
      workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
    }
    if (bossGroup != null && !bossGroup.isTerminated()) {
      bossGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
    }
  }

  public CompletableFuture<VncConnection> disconnect() {

    if (!isConnected()) {
      return CompletableFuture.completedFuture(this);
    }
    connecting.set(false);
    connected.set(false);
    return CompletableFuture.supplyAsync(() -> {
      shutdown();
      return this;
    });

  }

  public boolean isConnected() {
    return connected.get();
  }

  public boolean isConnecting() {
    return connecting.get();
  }

  public ReadOnlyBooleanProperty connectedProperty() {
    return connected.getReadOnlyProperty();
  }

  public ReadOnlyBooleanProperty connectingProperty() {
    return connecting.getReadOnlyProperty();
  }

}
