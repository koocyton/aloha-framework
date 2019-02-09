package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyPipeline;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.logging.SocketHandler;

public class AppRoute {

    private final static Logger logger = LoggerFactory.getLogger(AppRoute.class);

    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        return routes -> routes
                .get("/test", (req, resp) -> appOutbound.sendJson(
                        req, resp, injector.getInstance(HelloHandle.class).hello(1L)
                        )
                )
                .get("/user/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return appOutbound.sendJson(
                            req, resp, injector.getInstance(HelloHandle.class).hello(id)
                    );
                })
                .ws("/game2", this::wsHandler)
                .ws("/game4", (in, out) -> out
                        .sendString(Mono.just("Hello World!"))
                        .then(in.receive()
                                .asString()
                                .next()
                                .log()
                                .then())
                )
                .ws("/game5", (in, out) -> {
                    return out.options(NettyPipeline.SendOptions::flushOnEach).send(
                            in.withConnection(connection -> {
                                in.aggregateFrames()
                                        .receiveFrames()
                                        .map(WebSocketFrame::content)
                                        .map(byteBuf -> byteBuf
                                                .readCharSequence(byteBuf
                                                        .readableBytes(), Charset
                                                        .defaultCharset())
                                                .toString())
                                        .map(TextWebSocketFrame::new)
                                        .map(i -> new TextWebSocketFrame(i + ""));
                                // connection.addHandlerLast(new WebSocketFrameHandler());
                            }).receive()
                    );
                })
                .ws("/game6", (in, out) -> appOutbound.sendWs(
                        in, out, injector.getInstance(HelloHandle.class).game(in, out)
                        )
                )
                .ws("/game", (in, out) -> {
                    AA aa = new AA();
                    return out
                            .sendObject(in
                                    .withConnection(connection -> {
                                        connection
                                                .channel()
                                                .pipeline()
                                                // .deregister()
                                                .writeAndFlush(new TextWebSocketFrame("hello 1 "))
                                                .addListener((ChannelFutureListener) future -> {
                                                    if (future.isSuccess()) {
                                                        future.channel().writeAndFlush(new TextWebSocketFrame("hello 2    ss3"));
                                                        Thread.sleep(1000);
                                                        logger.info("Thread.sleep(1000)");
                                                    }
                                                });
                                    })
                                    .receive()
                                    .subscribe(
                                            tick -> logger.info("Tick {}", tick),
                                            (ex) -> logger.info("Error emitted"),
                                            () -> logger.info("Completed")
                                    )
                            );
                })
                .get("/**", appOutbound::sendStatic);
    }


    private Publisher<Void> wsHandler(WebsocketInbound in, WebsocketOutbound out) {
        return out.send(
                in.withConnection(connection -> {
                    connection.addHandlerLast(new WebSocketFrameHandler());
                }).receive()
        );
    }

    private class AA {

        public void bb() {

        }

        public void cc() {

        }

        public void dd() {

        }
    }
}
