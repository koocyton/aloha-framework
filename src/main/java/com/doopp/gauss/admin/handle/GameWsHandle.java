package com.doopp.gauss.admin.handle;

import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;

@Slf4j
@Path("/game")
@Singleton
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Override
    public void onConnect(Channel channel) {
        super.onConnect(channel);
    }

    @Override
    public String onTextMessage(Channel channel) {
        return "onTextMessage : " + channel.id();
    }

    @Override
    public void close(Channel channel) {
        super.close(channel);
    }
}
