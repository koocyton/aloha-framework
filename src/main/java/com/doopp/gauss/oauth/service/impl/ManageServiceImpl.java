package com.doopp.gauss.oauth.service.impl;

import com.doopp.gauss.oauth.dao.ClientDao;
import com.doopp.gauss.oauth.dao.UserDao;
import com.doopp.gauss.oauth.entity.Client;
import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.entity.vo.UserVO;
import com.doopp.gauss.oauth.message.CommonResponse;
import com.doopp.gauss.oauth.utils.EncryHelper;
import com.doopp.gauss.oauth.service.ManageService;
import com.doopp.gauss.server.redis.ShadedJedisUtils;
import com.doopp.gauss.oauth.utils.HttpClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ManageServiceImpl implements ManageService {

    @Inject
    @Named("managerSessionRedis")
    private ShadedJedisUtils managerSessionRedis;

    @Inject
    private Gson gson;

    @Inject
    private HttpClientUtil httpClient;

    @Inject
    private UserDao userDao;

    @Inject
    private ClientDao clientDao;

    @Inject
    @Named("admin.client.id")
    private Long clientId;

    @Inject
    @Named("admin.client.secret")
    private String clientSecret;

    @Inject
    @Named("admin.client.api_url")
    private String oauthApiUrl;

    @Override
    public Mono<UserVO> getManagerByToken(String token) {
        UserVO manager = managerSessionRedis.get(token, UserVO.class);

        if (manager == null) {
            int time = (int) (System.currentTimeMillis() / 1000);
            Map<String, String> headers = new HashMap<String, String>(){{
                put("authentication", "client=" + clientId + "&time=" + time + "&token=" + token + "&security=" + EncryHelper.md5(time + "_" + clientId + "_" + clientSecret));
            }};
            return httpClient.get(oauthApiUrl, headers)
                .map(byteBuf ->{
                    Type cuClassType = new TypeToken<CommonResponse<UserVO>>(){}.getType();
                    CommonResponse<UserVO> userVOCommonResponse = gson.fromJson(byteBuf.toString(Charset.forName("UTF-8")), cuClassType);
                    return (UserVO) userVOCommonResponse.getData();
                })
                .map(userVO -> {
                    managerSessionRedis.set(token, userVO);
                    return userVO;
                });
        }
        return Mono.just(manager);
    }

    @Override
    public Mono<List<User>> getUsers() {
        return Mono.just(userDao.getList());
    }

    @Override
    public  Mono<List<Client>> getClients() {
        return Mono.just(clientDao.getList());
    }

    @Override
    public  Mono<User> getUser(Long id) {
        return Mono.just(userDao.getById(id));
    }
}
