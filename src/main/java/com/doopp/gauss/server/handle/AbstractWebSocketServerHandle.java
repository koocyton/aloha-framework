package com.doopp.gauss.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;

public abstract class AbstractWebSocketServerHandle implements WebSocketServerHandle {

    @Override
    public void onConnect(Channel channel) {

    }

    @Override
    public void onMessage() {

    }

    @Override
    public String onTextMessage() {
        return null;
    }

    @Override
    public ByteBuf onBinaryMessage() {
        return Unpooled.buffer(0);
    }

    @Override
    public ByteBuf onPingMessage() {
        return Unpooled.buffer(0);
    }

    @Override
    public ByteBuf onPongMessage() {
        return Unpooled.buffer(0);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onError() {

    }
}
