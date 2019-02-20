package com.doopp.gauss.server.handle;

import io.netty.channel.Channel;

public abstract class AbstractWebSocketServerHandle implements WebSocketServerHandle {

    @Override
    public void onConnect(Channel channel) {

    }

    @Override
    public String onFullTextMessage() {
        return "";
    }

    @Override
    public void onMessage() {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onError() {

    }
}
