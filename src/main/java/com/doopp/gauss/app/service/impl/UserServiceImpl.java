package com.doopp.gauss.app.service.impl;


import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.app.service.UserService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements UserService {

    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Inject
    private UserDao userDao;

    @Override
    public User getUserByToken(String token) {
        return userDao.getById(1L);
    }

}
