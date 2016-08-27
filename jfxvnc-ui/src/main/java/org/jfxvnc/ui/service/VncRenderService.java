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
package org.jfxvnc.ui.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.jfxvnc.net.rfb.codec.ProtocolInitializer;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.net.rfb.codec.decoder.BellEvent;
import org.jfxvnc.net.rfb.codec.decoder.ColourMapEntriesEvent;
import org.jfxvnc.net.rfb.codec.decoder.ServerCutTextEvent;
import org.jfxvnc.net.rfb.codec.decoder.ServerDecoderEvent;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.RenderCallback;
import org.jfxvnc.net.rfb.render.RenderProtocol;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class VncRenderService extends Service<Boolean> implements RenderProtocol {

  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncRenderService.class);

  @Inject
  ProtocolConfiguration config;

  private static final int CONNECT_PORT = 5900;
  private static final int LISTENING_PORT = 5500;

  private final BooleanProperty listeningModeProperty = new SimpleBooleanProperty(false);
  private final IntegerProperty listeningPortProperty = new SimpleIntegerProperty(LISTENING_PORT);

  private final ReadOnlyBooleanWrapper connectProperty = new ReadOnlyBooleanWrapper(false);
  private final ReadOnlyBooleanWrapper onlineProperty = new ReadOnlyBooleanWrapper(false);

  private final ReadOnlyBooleanWrapper bellProperty = new ReadOnlyBooleanWrapper(false);
  private final ReadOnlyStringWrapper serverCutTextProperty = new ReadOnlyStringWrapper();

  private final ReadOnlyObjectWrapper<ConnectInfoEvent> connectInfoProperty = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyObjectWrapper<ProtocolState> protocolStateProperty = new ReadOnlyObjectWrapper<>(ProtocolState.CLOSED);
  private final ReadOnlyObjectWrapper<InputEventListener> inputProperty = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyObjectWrapper<ImageRect> imageProperty = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyObjectWrapper<ColourMapEntriesEvent> colourMapEntriesEventProperty = new ReadOnlyObjectWrapper<>();

  private final ReadOnlyObjectWrapper<Throwable> exceptionCaughtProperty = new ReadOnlyObjectWrapper<>();

  private final double minZoomLevel = 0.2;
  private final double maxZoomLevel = 5.0;

  private final DoubleProperty zoomLevelProperty = new SimpleDoubleProperty(1);
  private final BooleanProperty fullSceenProperty = new SimpleBooleanProperty(false);
  private final BooleanProperty restartProperty = new SimpleBooleanProperty(false);

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  public VncRenderService() {

    protocolStateProperty.addListener((l) -> {
      if (protocolStateProperty.get() == ProtocolState.CLOSED) {
        connectProperty.set(false);
      }
    });
    connectProperty.addListener((l, a, b) -> {
      if (!b) {
        onlineProperty.set(false);
      }
    });

    zoomLevelProperty.addListener((l, a, b) -> {
      if (b.doubleValue() > maxZoomLevel) {
        zoomLevelProperty.set(maxZoomLevel);
      } else if (b.doubleValue() < minZoomLevel) {
        zoomLevelProperty.set(minZoomLevel);
      }
    });
  }

  public void validateConnection() throws Exception {
    logger.warn("not implemented yet");
  }

  private boolean connect() throws Exception {
    connectProperty.set(true);
    shutdown();
    workerGroup = new NioEventLoopGroup();

    String host = config.hostProperty().get();
    int port = config.portProperty().get() > 0 ? config.portProperty().get() : CONNECT_PORT;

    Bootstrap b = new Bootstrap();
    b.group(workerGroup);
    b.channel(NioSocketChannel.class);
    b.option(ChannelOption.SO_KEEPALIVE, true);
    b.option(ChannelOption.TCP_NODELAY, true);
    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

    b.handler(new ProtocolInitializer(VncRenderService.this, config));

    logger.info("try to connect to {}:{}", host, port);
    ChannelFuture f = b.connect(host, port);
    f.await(5000);

    connectProperty.set(f.isSuccess());
    logger.info("connection {}", connectProperty.get() ? "established" : "failed");
    if (f.isCancelled()) {
      logger.warn("connection aborted");
    } else if (!f.isSuccess()) {
      logger.error("connection failed", f.cause());
      exceptionCaughtProperty.set(f.cause() != null ? f.cause() : new Exception("connection failed to host: " + host + ":" + port));
    }
    return connectProperty.get();
  }

  private void startListening() throws Exception {
    connectProperty.set(true);
    shutdown();
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();

    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100);

    b.childHandler(new ProtocolInitializer(VncRenderService.this, config));

    int port = listeningPortProperty.get() > 0 ? listeningPortProperty.get() : LISTENING_PORT;
    b.bind(port).addListener(l -> {
      logger.info("wait for incoming connection request on port: {}..", port);
      connectProperty.set(l.isSuccess());
    }).sync();

  }

  @PreDestroy
  @Override
  public boolean cancel() {
    Platform.runLater(() -> super.cancel());
    shutdown();
    connectProperty.set(false);
    return true;
  }

  private void shutdown() {
    if (workerGroup != null && !workerGroup.isTerminated()) {
      workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
    }
    if (bossGroup != null && !bossGroup.isTerminated()) {
      bossGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
    }
  }

  @Override
  protected Task<Boolean> createTask() {
    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        if (listeningModeProperty.get()) {
          startListening();
          return true;
        }
        return connect();
      }
    };
  }

  @Override
  public void render(ImageRect rect, RenderCallback callback) {
    imageProperty.set(rect);
    callback.renderComplete();
  }

  @Override
  public void eventReceived(ServerDecoderEvent event) {
    logger.trace("event received: {}", event);
    if (event instanceof ConnectInfoEvent) {
      connectInfoProperty.set((ConnectInfoEvent) event);
      onlineProperty.set(true);
      return;
    }
    if (event instanceof BellEvent) {
      bellProperty.set(!bellProperty.get());
      return;
    }
    if (event instanceof ServerCutTextEvent) {
      serverCutTextProperty.set(((ServerCutTextEvent) event).getText());
      return;
    }
    if (event instanceof ColourMapEntriesEvent) {
      colourMapEntriesEventProperty.set((ColourMapEntriesEvent) event);
      return;
    }

    logger.warn("not handled event: {}", event);
  }

  @Override
  public void exceptionCaught(Throwable t) {
    exceptionCaughtProperty.set(t);
  }

  @Override
  public void stateChanged(ProtocolState state) {
    protocolStateProperty.set(state);
    if (state == ProtocolState.CLOSED) {
      cancel();
    }
  }

  @Override
  public void registerInputEventListener(InputEventListener listener) {
    inputProperty.set(listener);
  }

  public ReadOnlyObjectProperty<ConnectInfoEvent> connectInfoProperty() {
    return connectInfoProperty.getReadOnlyProperty();
  }

  public ReadOnlyObjectProperty<ProtocolState> protocolStateProperty() {
    return protocolStateProperty;
  }

  public ReadOnlyObjectProperty<InputEventListener> inputProperty() {
    return inputProperty.getReadOnlyProperty();
  }

  public ReadOnlyObjectProperty<ImageRect> imageProperty() {
    return imageProperty.getReadOnlyProperty();
  }

  public ReadOnlyObjectProperty<ColourMapEntriesEvent> colourMapEntriesEventProperty() {
    return colourMapEntriesEventProperty.getReadOnlyProperty();
  }

  public ReadOnlyObjectProperty<Throwable> exceptionCaughtProperty() {
    return exceptionCaughtProperty.getReadOnlyProperty();
  }

  public ReadOnlyBooleanProperty connectProperty() {
    return connectProperty.getReadOnlyProperty();
  }

  public ReadOnlyBooleanProperty onlineProperty() {
    return onlineProperty.getReadOnlyProperty();
  }

  public ReadOnlyBooleanProperty bellProperty() {
    return bellProperty.getReadOnlyProperty();
  }

  public ReadOnlyStringProperty serverCutTextProperty() {
    return serverCutTextProperty.getReadOnlyProperty();
  }

  public DoubleProperty zoomLevelProperty() {
    return zoomLevelProperty;
  }

  public BooleanProperty fullSceenProperty() {
    return fullSceenProperty;
  }

  public BooleanProperty restartProperty() {
    return restartProperty;
  }

  public BooleanProperty listeningModeProperty() {
    return listeningModeProperty;
  }

  public IntegerProperty listeningPortProperty() {
    return listeningPortProperty;
  }
}
