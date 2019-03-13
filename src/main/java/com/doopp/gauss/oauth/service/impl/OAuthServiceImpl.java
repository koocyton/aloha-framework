package com.doopp.gauss.oauth.service.impl;

import com.doopp.gauss.oauth.service.OAuthService;
import com.doopp.gauss.common.dao.ClientDao;
import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.defined.CommonError;
import com.doopp.gauss.common.entity.Client;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.common.message.OAuthRequest;
import com.doopp.gauss.common.message.request.LoginRequest;
import com.doopp.gauss.common.message.request.RegisterRequest;
import com.doopp.gauss.common.utils.EncryHelper;
import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.doopp.gauss.server.util.IdWorker;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class OAuthServiceImpl implements OAuthService {

    @Inject
    private IdWorker userIdWorker;

    @Inject
    private UserDao userDao;

    @Inject
    private ClientDao clientDao;

    @Inject
    @Named("userSessionRedis")
    private CustomShadedJedis userSessionRedis;

    @Override
    public Mono<User> userLogin(String account, String password) {
        User user = userDao.getByAccount(account);
        if (user==null) {
            return Mono.error(new CommonException(CommonError.ACCOUNT_NO_EXIST));
        }
        if (!user.getPassword().equals(user.getHashPassword(password))) {
            return Mono.error(new CommonException(CommonError.PASSWORD_INCORRECT));
        }
        return Mono.just(user);
    }

    @Override
    public Mono<User> userAutoLogin(String account) {
        return this
            .userRegister(account, account)
            .onErrorResume(throwable -> {
                User user = userDao.getByAccount(account);
                return Mono.just(user);
            });
    }

    @Override
    public Mono<User> userRegister(String account, String password) {
        User user = new User();
        user.setId(userIdWorker.nextId());
        user.setAccount(account);
        user.setPassword(user.getHashPassword(password));
        try {
            // log.info("{}", user);
            userDao.create(user);
            return Mono.just(user);
        }
        catch(Exception e) {
            e.printStackTrace();
            return Mono.error(new CommonException(CommonError.ACCOUNT_EXIST));
        }
    }

    @Override
    public void checkLoginRequest(OAuthRequest<LoginRequest> commonRequest) throws CommonException {
        // 检查 time，client 和 security
        Client client = this.checkCommonRequest(commonRequest);
        // 是否接受登录请求
        if (!client.isAllow_login()) {
            throw new CommonException(CommonError.REJECT_LOGIN);
        }
    }

    @Override
    public void checkRegisterRequest(OAuthRequest<RegisterRequest> commonRequest) throws CommonException {
        // 检查 time，client 和 security
        Client client = this.checkCommonRequest(commonRequest);
        // 密码检查
        RegisterRequest registerRequest = commonRequest.getData();
        if (registerRequest.getPassword().length()<8) {
            throw new CommonException(CommonError.PASSWORD_IS_SHORT);
        }
        if (registerRequest.getPassword().equals(registerRequest.getRepeatPassword())) {
            throw new CommonException(CommonError.DIFFERENT_PASSWORD);
        }
        // 是否接受注册请求
        if (!client.isAllow_register()) {
            throw new CommonException(CommonError.REJECT_REGISTER);
        }
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

    private void removeSessionToken(String token) {
        userSessionRedis.del(token);
        String userId = userSessionRedis.get(token);
        if (userId!=null) {
            userSessionRedis.del(userId);
        }
    }

    private void removeSessionToken(Long userId) {
        String id = String.valueOf(userId);
        userSessionRedis.del(id);
        String token = userSessionRedis.get(id);
        if (token!=null) {
            userSessionRedis.del(token);
        }
    }

    private User getUserByToken(String token) {
        // log.info(">>> token {} ", token);
        String userId = userSessionRedis.get(token);
        if (userId==null) {
            return null;
        }
        // log.info(">>> userId {} ", userId);
        return userDao.getById(Long.valueOf(userId));
    }

    private Client checkCommonRequest(OAuthRequest commonRequest) throws CommonException {
        // 判断时间是否过期
        if (this.isExpired(commonRequest.getTime())) {
            throw new CommonException(CommonError.EXPIRE_TIME);
        }
        // 判断客户端是否伪造的 id
        Client client = clientDao.getById(commonRequest.getClient());
        if (client==null) {
            throw new CommonException(CommonError.CLIENT_FAILED);
        }
        // 校验客户端的请求是否有过安全加密
        if (!this.isSecurity(commonRequest.getTime(), commonRequest.getClient(), commonRequest.getSecurity(), client.getSecret())) {
            throw new CommonException(CommonError.UNSAFE_REQUEST);
        }
        //
        return client;
    }

    // 安全校验
    private boolean isSecurity(int time, Long clientId, String security, String secret) {
        String reSecurity = EncryHelper.md5(time + "_" + clientId + "_" + secret);
        return reSecurity.equals(security);
    }

    // 时间有效
    private boolean isExpired(int requestTime) {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        return ((currentTime-requestTime)>120);
    }

    @Override
    public User checkRequestHeader(String authenticationHeader) throws CommonException
    {
        if (authenticationHeader==null) {
            throw new CommonException(CommonError.ACCOUNT_NO_EXIST);
        }
        // log.info(">>> {}", authenticationHeader);
        Map<String, String> authenticationParams = this.getQueryParams(authenticationHeader);
        if (authenticationParams.get("time")==null
            || authenticationParams.get("client")==null
            || authenticationParams.get("security")==null
            || authenticationParams.get("token")==null) {
            throw new CommonException(CommonError.UNSAFE_REQUEST);
        }

        int time = Integer.valueOf(authenticationParams.get("time"));
        Long clientId = Long.valueOf(authenticationParams.get("client"));
        String security = authenticationParams.get("security");
        String token = authenticationParams.get("token");
        Client client = clientDao.getById(clientId);
        if (client==null) {
            throw new CommonException(CommonError.CLIENT_FAILED);
        }
        if (!this.isSecurity(time, clientId, security, client.getSecret())) {
            throw new CommonException(CommonError.UNSAFE_REQUEST);
        }
        User user = this.getUserByToken(token);
        if (user==null) {
            throw new CommonException(CommonError.ACCOUNT_NO_EXIST);
        }
        return user;
    }

    private Map<String, String> getQueryParams(String url) {
        Map<String, String> map = null;
        if (url != null && url.contains("&") && url.contains("=")) {
            map = new HashMap<>();
            String[] arrTemp = url.split("&");
            for (String str : arrTemp) {
                String[] qs = str.split("=");
                map.put(qs[0], qs[1]);
            }
        }
        return map;
    }
}
