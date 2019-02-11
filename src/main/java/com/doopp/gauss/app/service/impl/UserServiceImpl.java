package com.doopp.gauss.app.service.impl;


import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.app.service.UserService;
import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Inject
    private UserDao userDao;

    @Inject
    private CustomShadedJedis userSessionRedis;

    @Override
    public User getUserByToken(String token) {
        return userDao.getById(1L);
    }

    @Override
    public User getUserById(Long id) {
        return userDao.getById(id);
    }

    @Override
    public String createSessionToken(User user) {
        this.removeSessionToken(user.getId());
        String userId = String.valueOf(user.getId());
        String token = UUID.randomUUID().toString();
        // 有效期 3 个月
        userSessionRedis.setex(userId, 7776000, token);
        userSessionRedis.setex(token, 7776000, userId);
        return token;
    }

    @Override
    public void removeSessionToken(String token) {
        userSessionRedis.del(token);
        String userId = userSessionRedis.get(token);
        if (userId!=null) {
            userSessionRedis.del(userId);
        }
    }

    @Override
    public void removeSessionToken(Long userId) {
        String id = String.valueOf(userId);
        userSessionRedis.del(id);
        String token = userSessionRedis.get(id);
        if (token!=null) {
            userSessionRedis.del(token);
        }
    }
}
