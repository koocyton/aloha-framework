package com.doopp.gauss.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface WebSocketServerHandle<T> {

    void onConnect(Channel channel);

    void onMessage();

    String onTextMessage();

    T onBinaryMessage();

    ByteBuf onPingMessage();

    ByteBuf onPongMessage();

    void onClose();

    void onError();
}
