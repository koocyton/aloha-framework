package com.doopp.gauss.admin.service.impl;

import com.doopp.gauss.admin.service.ManagerService;
import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.defined.CommonError;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;

public class ManagerServiceImpl implements ManagerService {

    @Inject
    private CustomShadedJedis managerSessionRedis;

    @Inject
    private ApplicationProperties applicationProperties;

    @Inject
    private UserDao userDao;

    @Override
    public User getManagerByToken(String token) throws InterruptedException, ExecutionException, CommonException {
        User manager = managerSessionRedis.get(token.getBytes(), User.class);
        if (manager==null) {
            // 获取配置
            Long client = applicationProperties.l("admin.client.id");
            String secret = applicationProperties.s("admin.client.secret");
            String fetchUserApiUrl = applicationProperties.s("admin.client.api_url");
            // 异步请求
            manager = userDao.getById(1L);
            if (manager!=null) {
                managerSessionRedis.set(token.getBytes(), manager);
            }
            else {
                throw new CommonException(CommonError.FAIL);
            }
        }
        return manager;
    }
}
