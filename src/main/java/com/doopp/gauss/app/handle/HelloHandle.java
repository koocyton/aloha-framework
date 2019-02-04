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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class HelloHandle {

    @Inject
    private CustomShadedJedis sessionRedis;

    @Inject
    private UserDao userDao;

    private final static Logger logger = LoggerFactory.getLogger(HelloHandle.class);

    @GET
    @Path("/liso/bad")
    public User hello(Long id) {
        User user = userDao.getById(id);
        return user;
    }

    public ByteBufFlux game(ByteBufFlux bbf) {
        logger.info("2 : {}", bbf);
        Publisher<User> pu = subscriber -> subscriber.onComplete();
        return ByteBufFlux.fromInbound(pu);
    }
}
