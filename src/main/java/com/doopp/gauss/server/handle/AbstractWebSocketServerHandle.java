package com.doopp.gauss.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.HashMap;

public abstract class AbstractWebSocketServerHandle implements WebSocketServerHandle {

    protected HashMap<ChannelId, Channel> channels = new HashMap<>();

    @Override
    public void onConnect(Channel channel) {

    }

    @Override
    public void onMessage(Channel channel) {

    }

    @Override
    public String onTextMessage(Channel channel) {
        return null;
    }

    @Override
    public ByteBuf onBinaryMessage(Channel channel) {
        return Unpooled.buffer(0);
    }

    @Override
    public ByteBuf onPingMessage(Channel channel) {
        return Unpooled.buffer(0);
    }

    @Override
    public ByteBuf onPongMessage(Channel channel) {
        return Unpooled.buffer(0);
    }

    @Override
    public void onClose(Channel channel) {

    }

    @Override
    public void onError(Channel channel) {

    }
}
