package com.doopp.gauss.admin.handle;

import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;

@Slf4j
@Path("/game")
@Singleton
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame("onTextMessage " + channel.id() + " : get " + frame.text()));
    }

    @Override
    public void close(Channel channel) {
        super.close(channel);
    }
}
