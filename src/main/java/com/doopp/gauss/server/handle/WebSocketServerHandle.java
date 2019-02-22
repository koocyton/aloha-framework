package com.doopp.gauss.server.handle;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.*;

public interface WebSocketServerHandle<T> {

    void onConnect(Channel channel);

    void onMessage(WebSocketFrame frame, Channel channel);

    void onTextMessage(TextWebSocketFrame frame, Channel channel);

    void onBinaryMessage(BinaryWebSocketFrame frame, Channel channel);

    void onPingMessage(PingWebSocketFrame frame, Channel channel);

    void onPongMessage(PongWebSocketFrame frame, Channel channel);

    void close(CloseWebSocketFrame frame, Channel channel);
}
