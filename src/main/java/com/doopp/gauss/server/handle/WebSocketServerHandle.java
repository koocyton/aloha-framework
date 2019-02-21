package com.doopp.gauss.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface WebSocketServerHandle<T> {

    void onConnect(Channel channel);

    void onMessage(Channel channel);

    String onTextMessage(Channel channel);

    T onBinaryMessage(Channel channel);

    ByteBuf onPingMessage(Channel channel);

    ByteBuf onPongMessage(Channel channel);

    void close(Channel channel);

    void onError(Channel channel);
}
