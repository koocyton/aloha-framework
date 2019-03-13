package com.doopp.gauss.server.handle;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractWebSocketServerHandle implements WebSocketServerHandle {

    private Map<String, Channel> channelMap = new HashMap<>();

    @Override
    public void onConnect(Channel channel) {
        channelMap.put(channel.id().asLongText(), channel);
        log.info("{}", channelMap.size());
        // channel.writeAndFlush(new TextWebSocketFrame("connected " + channel.id()));
    }

    public void sendTextMessage(String text, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(text));
    }

    public Flux<String> receiveTextMessage(Channel channel) {
        return Flux.just("aaa", "bbb", "ccc");
    }

    public void allSendTextMessage(String text) {
        for(Channel channel : channelMap.values()) {
            channel.writeAndFlush(new TextWebSocketFrame(text));
        }
    }

    @Override
    public void onTextMessage(String text, Channel channel) {
        // channel.writeAndFlush(new TextWebSocketFrame("message " + text));
    }

    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        // channel.writeAndFlush(new TextWebSocketFrame("message " + channel.id()));
    }

    @Override
    public void onBinaryMessage(BinaryWebSocketFrame frame, Channel channel) {
        channel.writeAndFlush(Unpooled.buffer(0));
    }

    @Override
    public void onPingMessage(PingWebSocketFrame frame, Channel channel) {
        channel.writeAndFlush(new PongWebSocketFrame());
    }

    @Override
    public void onPongMessage(PongWebSocketFrame frame, Channel channel) {
        channel.writeAndFlush(new PingWebSocketFrame());
    }

    public void disconnect(Channel channel) {
        if (channel!=null) {
            try {
                channelMap.remove(channel.id().asLongText());
                channel.disconnect();
                channel.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
