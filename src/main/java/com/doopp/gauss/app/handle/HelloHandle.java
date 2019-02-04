package com.doopp.gauss.app.handle;

import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.app.defined.CommonField;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.server.resource.RequestAttribute;
import com.google.inject.Inject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.ByteBufFlux;

public class HelloHandle {

    @Inject
    private UserDao userDao;

    @Inject
    private RequestAttribute requestAttribute;

    private final static Logger logger = LoggerFactory.getLogger(HelloHandle.class);

    public User hello(Long id) {
        User user2 = requestAttribute.getAttribute(CommonField.CURRENT_USER, User.class);
        logger.info("{}", user2);
        User user = userDao.getById(id);
        return user;
    }

    public ByteBufFlux game(ByteBufFlux bbf) {
        logger.info("2 : {}", bbf);
        Publisher<User> pu = subscriber -> subscriber.onComplete();
        return ByteBufFlux.fromInbound(pu);
    }
}
