package com.doopp.gauss.server.netty;

import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.reactivestreams.Publisher;
import reactor.netty.NettyPipeline;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.util.function.Predicate;

public interface HttpRouter extends HttpServerRequest
{
    default <T> Predicate<T> filter(WebsocketInbound in) {
        return null;
        // return this;
    }

    default <T> Publisher<Void> sendJson(WebsocketInbound in, WebsocketOutbound out, T handle) {
        return out.options(NettyPipeline.SendOptions::flushOnEach)
            .sendString(in
                .receiveFrames()
                .map(frame -> {
                    if (frame instanceof TextWebSocketFrame) {
                        TextWebSocketFrame tf = (TextWebSocketFrame) frame;
                        return new GsonBuilder().create().toJson(handle);
                    }
                    return "no";
                })
            );
    }
}