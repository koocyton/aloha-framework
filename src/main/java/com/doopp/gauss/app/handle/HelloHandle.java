package com.doopp.gauss.app.handle;

import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.app.entity.User;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public class HelloHandle {

    @Inject
    private UserDao userDao;

    private final static Logger logger = LoggerFactory.getLogger(HelloHandle.class);

    public User hello(Long id) {
        // RequestAttribute requestAttribute = injector.getInstance(RequestAttribute.class);
        // User user2 = requestAttribute.getAttribute(CommonField.CURRENT_USER, User.class);
        User user = userDao.getById(id);
        return user;
    }

    public User game(WebsocketInbound in, WebsocketOutbound out) {
        return userDao.getById(1L);
    }
}
