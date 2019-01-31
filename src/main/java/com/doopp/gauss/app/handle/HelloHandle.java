package com.doopp.gauss.app.handle;

import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

public class HelloHandle {

    @Inject
    private CustomShadedJedis sessionRedis;

    @Inject
    private UserDao userDao;

    private final static Logger logger = LoggerFactory.getLogger(HelloHandle.class);

    public Mono<String> boy(Long userId) {
        User boy = sessionRedis.get("boy99".getBytes(), User.class);
        if (boy==null) {
            logger.info("1 : {}", boy);
            boy = userDao.getById();
            sessionRedis.set("boy99".getBytes(), boy);
        }
        logger.info("2 : {}", boy);
        return Mono.just(boy.toString());
    }

    public Flux<String> hello() {
        return Flux.just(userDao.getById().toString());
    }

    public ByteBufFlux game(ByteBufFlux bbf) {
        logger.info("2 : {}", bbf);
        Publisher<User> pu = subscriber -> subscriber.onComplete();
        return ByteBufFlux.fromInbound(pu);
    }
}
