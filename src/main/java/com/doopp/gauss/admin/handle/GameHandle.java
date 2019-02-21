package com.doopp.gauss.admin.handle;

import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;

@Slf4j
@Path("/game")
@Singleton
public class GameHandle extends AbstractWebSocketServerHandle {

    @Override
    public void onConnect(Channel channel) {
        this.channels.put(channel.id(), channel);
        log.info("{}", channel.id());
    }

    @Override
    public String onTextMessage(Channel channel) {
        log.info("{}", channel.id());
        return "GameHandle : hello";
    }
}
