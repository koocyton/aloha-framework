package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.NettyPipeline;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.SocketHandler;

public class AppRoute {

    private final static Logger logger = LoggerFactory.getLogger(AppRoute.class);

    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        AtomicInteger serverRes = new AtomicInteger();

        WebSocketFrameHandler handler = new WebSocketFrameHandler();

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
                .ws("/game", this::wsHandler)
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
                .ws("/game7", (in, out) -> {
                    return out.sendString(
                            in.receive()
                                    .asString()
                                    .publishOn(Schedulers.single())
                                    .doOnNext(s -> serverRes.incrementAndGet())
                    );
                })
                .ws("/game13", (in, out) -> {
                    return out.send(in
                            .receive()
                            .handle((byteBuf, byteBufSynchronousSink) -> {
                                byte[] byteArray = new byte[byteBuf.capacity()];
                                byteBuf.readBytes(byteArray);
                                String result = new String(byteArray);
                                logger.info("{}", byteBufSynchronousSink.currentContext());
                                logger.info("{}", byteBuf);
                            })
                    );
                })
                .get("/**", appOutbound::sendStatic);
    }


    private Publisher<Void> wsHandler(WebsocketInbound in, WebsocketOutbound out) {

        final String[] result = new String[1];

        return out.sendString(
                in
                    .withConnection(c-> {
                        for (int ii = 0; ii < 10; ii++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            c.channel().writeAndFlush(new TextWebSocketFrame(result[0] + "hello boy " + ii));
                            c.channel().closeFuture();
                        }
                    })
//                    .withConnection(connection -> {
//                        connection//.addHandlerLast(handler);
//                            .channel()
//                             .newSucceededFuture()
//                             .channel()//.writeAndFlush(new TextWebSocketFrame("hello 1 "));
//                             pipeline()
//                            .writeAndFlush(null)
//                            .addListener((ChannelFutureListener) future -> {
//                                                            if (future.isSuccess()) {
//                                                                for (int ii = 0; ii < 100; ii++) {
//                                                                    Thread.sleep(1000);
//                                                                    future.channel().writeAndFlush(new TextWebSocketFrame("hello boy"));
//                                                                }
//                                                            }
//                                // handler = future.channel();
//                            });
//                    })
                    .receive()
                    .map(byteBuf -> {
                        byte[] byteArray = new byte[byteBuf.capacity()];
                        byteBuf.readBytes(byteArray);
                        result[0] = new String(byteArray);
                        logger.info("{}", result[0]);
                        return result[0];
                    })
        );
    }
}
