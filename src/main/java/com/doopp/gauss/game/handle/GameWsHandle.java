package com.doopp.gauss.game.handle;

import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Path("/game")
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Override
    public void onConnect(Channel channel) {
        super.allSendTextMessage("有新人加入哦");
        super.onConnect(channel);
    }

    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame("onTextMessage " + channel.id() + " : get " + frame.text()));
    }

    @Override
    public void close(Channel channel) {
        super.close(channel);
    }
}
