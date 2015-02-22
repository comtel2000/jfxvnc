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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.jfxvnc.net.rfb.ProtocolConfiguration;
import org.jfxvnc.net.rfb.codec.ProtocolInitializer;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.net.rfb.codec.decoder.BellEvent;
import org.jfxvnc.net.rfb.codec.decoder.ServerCutTextEvent;
import org.jfxvnc.net.rfb.codec.decoder.ServerEvent;
import org.jfxvnc.net.rfb.codec.encoder.InputEventListener;
import org.jfxvnc.net.rfb.render.ConnectInfoEvent;
import org.jfxvnc.net.rfb.render.IRender;
import org.jfxvnc.net.rfb.render.RenderCallback;
import org.jfxvnc.net.rfb.render.rect.ImageRect;
import org.slf4j.LoggerFactory;

public class VncRenderService extends Service<Boolean> implements IRender {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(VncRenderService.class);

    @Inject
    ProtocolConfiguration config;

    private final ReadOnlyBooleanWrapper connectProperty = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper onlineProperty = new ReadOnlyBooleanWrapper(false);

    private final ReadOnlyBooleanWrapper bellProperty = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyStringWrapper serverCutTextProperty = new ReadOnlyStringWrapper();

    private final ReadOnlyObjectWrapper<ConnectInfoEvent> detailsProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ProtocolState> protocolStateProperty = new ReadOnlyObjectWrapper<>(ProtocolState.CLOSED);
    private final ReadOnlyObjectWrapper<InputEventListener> inputProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ImageRect> imageProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Throwable> exceptionCaughtProperty = new ReadOnlyObjectWrapper<>();

    private final double minZoomLevel = 0.2;
    private final DoubleProperty zoomLevelProperty = new SimpleDoubleProperty(1);
    
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
    }

    public void validateConnection() throws Exception {
	logger.warn("not implemented yet");
    }

    private boolean connect() throws Exception {
	connectProperty.set(true);

	if (workerGroup != null && !workerGroup.isTerminated()) {
	    logger.warn("wait for shutting down old connect");
	    workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
	}
	workerGroup = new NioEventLoopGroup();
	
	String host = config.hostProperty().get();
	int port = config.portProperty().get();

	Bootstrap b = new Bootstrap();
	b.group(workerGroup);
	b.channel(NioSocketChannel.class);
	b.option(ChannelOption.SO_KEEPALIVE, true);
	b.option(ChannelOption.TCP_NODELAY, true);
	b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

	b.handler(new ProtocolInitializer(VncRenderService.this, config));

	logger.info("try to connect to {}:{}", host, port);
	ChannelFuture f = b.connect(host, port);

	f.awaitUninterruptibly();
	connectProperty.set(f.isSuccess());
	logger.info("connection {}", connectProperty.get() ? "established" : "failed");
	if (f.isCancelled()) {
	    logger.warn("connection aborted");
	} else if (!f.isSuccess()) {
	    exceptionCaughtProperty.set(f.cause());
	}
	return connectProperty.get();
    }

    @PreDestroy
    public void disconnect() {
	cancel();
	if (workerGroup != null){
	    workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
	}
	connectProperty.set(false);
    }

    @Override
    protected Task<Boolean> createTask() {
	return new Task<Boolean>() {
	    @Override
	    protected Boolean call() throws Exception {
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
    public void eventReceived(ServerEvent event) {
	logger.info("event received: {}", event);
	if (event instanceof ConnectInfoEvent) {
	    detailsProperty.set((ConnectInfoEvent) event);
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
	logger.warn("not handled event: {}", event);
    }

    @Override
    public void exceptionCaught(Throwable t) {
	exceptionCaughtProperty.set(t);
    }

    @Override
    public void stateChanged(ProtocolState state) {
	if (state == ProtocolState.CLOSED) {
	    Platform.runLater(() -> disconnect());
	}
	protocolStateProperty.set(state);
    }

    @Override
    public void registerInputEventListener(InputEventListener listener) {
	inputProperty.set(listener);
    }

    public ReadOnlyObjectProperty<ConnectInfoEvent> detailsProperty() {
	return detailsProperty.getReadOnlyProperty();
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

    public double getMinZoomLevel() {
	return minZoomLevel;
    }

    
}
