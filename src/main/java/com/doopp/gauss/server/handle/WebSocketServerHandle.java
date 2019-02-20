package com.doopp.gauss.server.handle;

import io.netty.channel.Channel;

public interface WebSocketServerHandle {

    void onConnect(Channel channel);

    void onMessage();

    String onFullTextMessage();

    void onClose();

    void onError();
}
