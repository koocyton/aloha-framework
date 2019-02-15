package com.doopp.gauss.app.handle;

import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.app.service.UserService;
import com.doopp.gauss.server.message.response.SessionToken;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRequest;

@Slf4j
public class HelloHandle {

    @Inject
    private UserDao userDao;

    @Inject
    private UserService userService;

    public User hello(Long id) {
        log.info("{}", id);
        return userService.getUserById(id);
    }

    public SessionToken setUserCookie(Long id, HttpServerRequest request) {
        request.cookies().forEach((a, b)->{
            log.info("{}", a);
            log.info("{}", b);
        });
        String token = userService.createSessionToken(userService.getUserById(id));
        return new SessionToken(token);
    }

    public User game() {
        return userDao.getById(1L);
    }
}
