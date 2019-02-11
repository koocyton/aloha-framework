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

        AtomicInteger clientRes = new AtomicInteger();
        AtomicInteger serverRes = new AtomicInteger();

        return out.options(NettyPipeline.SendOptions::flushOnEach)
                        .sendString(in.receive()
                                .asString()
                                .publishOn(Schedulers.single())
                                .doOnNext(s -> {
                                    logger.info("11 {}", s);
                                    serverRes.incrementAndGet();
                                })
                                .map(it -> {
                                    logger.info("22 {}", it);
                                    return it + " !! ";
                                })
                                );
    }


    private Publisher<Void> wsHandler2(WebsocketInbound in, WebsocketOutbound out) {

        VV vv = new VV();
        return out.sendObject(
//                 in
//                    .withConnection(c-> {
//                        for (int ii = 0; ii < 10; ii++) {
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            c.channel().writeAndFlush(new TextWebSocketFrame(result[0] + "hello boy " + ii));
//                            c.channel().closeFuture();
//                        }
//                    })

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

                in
//                        .withConnection(c->{
//                            vv.setChannel(c.channel());
//                            c.channel().writeAndFlush(new TextWebSocketFrame("hihihi"));
//                        })
                        .receiveObject()
                        .doOnSubscribe(m -> {
                            logger.info("11 {}", m);
                        })
                        // .publishOn(Schedulers.single())
                        .doOnNext(object -> {
                            // logger.info("22 {}", object);
                            if (object instanceof  TextWebSocketFrame) {
                                TextWebSocketFrame tsf = (TextWebSocketFrame) object;
                                // vv.sendString(tsf.text());
                                tsf.retain();
                            }
                            // webSocketFrame.retain();
                        })
                        .log("server-reply")
//                        .map(webSocketFrame -> {
//                            // logger.info("11 {}", webSocketFrame);
//                            webSocketFrame.retain();
//                            return webSocketFrame;
//                        })
//                        .receiveObject()
//                        .map(channel->{
//                            logger.info("{}", channel);
//                            return (Channel) channel;
//                        })

                // .receiveFrames()
//                        .map(webSocketFrame -> {
//                            logger.info("11 {}", webSocketFrame);
//                            return webSocketFrame.content();
//                        })
//                        .map(byteBuf -> {
//                            logger.info("22 {}", byteBuf);
//                                return byteBuf
//                                        .readCharSequence(
//                                                byteBuf.readableBytes(),
//                                                Charset.defaultCharset()
//                                        )
//                                        .toString();
//                        })
//                        .map(text -> {
//                            logger.info("33 {}", text);
//                            return new TextWebSocketFrame(text);
//                        })
//                        .map(f -> {
//                            logger.info("44 {}", f);
//                            return f;
//                        })

//                in.withConnection(c->{
//                            logger.info("11 {}", c.channel());
//                            c.channel().writeAndFlush(new TextWebSocketFrame("first hello"));
//                        })
//                        .receiveObject()
//                        .doOnSubscribe(receive -> {
//                                logger.info("22 {}", receive);
//                        })
//                        .doOnNext(object -> {
//                            logger.info("33 {}", object);
//                            if (object instanceof WebSocketFrame) {
//                                WebSocketFrame wf = (WebSocketFrame) object;
//                                wf.retain();
//                            }
//                })


//                in
//                    .receive()
//                    .map(s->{
//                        return s;
//                    })
//                    .map(byteBuf -> {
//                        byte[] byteArray = new byte[byteBuf.capacity()];
//                        byteBuf.readBytes(byteArray);
//                        String result= new String(byteArray);
//                        logger.info("{}", result);
//                        return byteBuf;
//                    })

//                    .map(byteBuf->byteBuf)
        );
    }
}


class VV {

    private Channel chanel;

    void setChannel(Channel channel) {
        this.chanel = channel;
    }

    void sendString(String msg) {
        System.out.println(chanel);
        chanel.writeAndFlush(new TextWebSocketFrame(msg));
        chanel.closeFuture();
    }
}