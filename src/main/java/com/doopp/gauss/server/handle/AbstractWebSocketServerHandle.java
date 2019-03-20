package com.doopp.gauss.server.handle;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.ReplayProcessor;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractWebSocketServerHandle implements WebSocketServerHandle {

    private Map<String, Channel> channelMap = new HashMap<>();

    private Map<String, FluxProcessor<String, String>> queueMessageMap = new HashMap<>();

    private Map<String, Channel[]> channelGroupMap = new HashMap<>();

    private static AttributeKey<String> CHANNEL_UNIQUE_KEY = AttributeKey.newInstance("channel_unique_key");

    @Override
    public void connected(Channel channel) {
        String channelKey = channel.id().asLongText();
        this.connected(channel, channelKey);
    }

    @Override
    public void connected(Channel channel, String channelKey) {
        channel.attr(CHANNEL_UNIQUE_KEY).set(channelKey);
        this.disconnect(channelKey);
        channelMap.put(channelKey, channel);
        queueMessageMap.put(channelKey, ReplayProcessor.create());
        log.info("Online number : {}", channelMap.size());
        // channel.writeAndFlush(new TextWebSocketFrame("connected " + channel.id()));
    }

    @Override
    public void sendTextMessage(String text, Channel channel) {
        // channel.writeAndFlush(new TextWebSocketFrame(text));
        Flux.just(text).map(Object::toString)
                .subscribe(s->
                    queueMessageMap.get(channel.attr(CHANNEL_UNIQUE_KEY).get()).onNext(s)
                );
    }

    @Override
    public Flux<String> receiveTextMessage(Channel channel) {
        return queueMessageMap.get(channel.attr(CHANNEL_UNIQUE_KEY).get());
    }

    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        this.sendTextMessage(frame.text(), channel);
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

    @Override
    public void disconnect(Channel channel) {
        if (channel!=null && channel.isActive()) {
            channel.disconnect();
            channel.close();
        }
        try {
            channelMap.remove(channel.attr(CHANNEL_UNIQUE_KEY).get());
            queueMessageMap.remove(channel.attr(CHANNEL_UNIQUE_KEY).get());
        }
        catch(Exception e) {
            // e.printStackTrace();
        }
    }
}
