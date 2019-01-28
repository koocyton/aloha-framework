package com.doopp.gauss.server.handle;

import com.doopp.gauss.server.dao.UserDao;
import com.doopp.gauss.server.entity.User;
import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.websocket.WebsocketInbound;

public class HelloHandle {

    @Inject
    private CustomShadedJedis sessionRedis;

    @Inject
    private UserDao userDao;

    private final static Logger logger = LoggerFactory.getLogger(HelloHandle.class);

    public Mono<String> boy(HttpServerRequest request) {
        User boy = sessionRedis.get("boy2".getBytes(), User.class);
        if (boy==null) {
            logger.info("1 : {}", boy);
            boy = userDao.getUser();
            sessionRedis.set("boy2".getBytes(), boy);
        }
        logger.info("2 : {}", boy);
        return Mono.just(boy.toString());
    }

    public Flux<String> hello() {
        return Flux.just("hello");
    }

    public ByteBufFlux  game(ByteBufFlux bbf) {
        logger.info("2 : {}", bbf);
        return ByteBufFlux.fromInbound(bbf);
    }
}
