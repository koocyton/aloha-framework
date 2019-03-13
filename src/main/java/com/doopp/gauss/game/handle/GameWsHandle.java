package com.doopp.gauss.game.handle;

import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;

@Slf4j
@Path("/oauth/websocket")
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
        super.allSendTextMessage("有人离开了哦");
    }
}
