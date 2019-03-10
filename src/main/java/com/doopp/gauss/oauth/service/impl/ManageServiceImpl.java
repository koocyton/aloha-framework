package com.doopp.gauss.oauth.service.impl;

import com.doopp.gauss.common.dao.ClientDao;
import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.entity.Client;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.entity.vo.UserVO;
import com.doopp.gauss.common.message.CommonResponse;
import com.doopp.gauss.common.utils.EncryHelper;
import com.doopp.gauss.oauth.service.ManageService;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.doopp.gauss.server.util.HttpClientUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
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
    private CustomShadedJedis managerSessionRedis;

    @Inject
    private ApplicationProperties applicationProperties;

    @Inject
    private Gson gson;

    @Inject
    private HttpClientUtil httpClient;

    @Inject
    private UserDao userDao;

    @Inject
    private ClientDao clientDao;

    @Override
    public Mono<UserVO> getManagerByToken(String token) {
        UserVO manager = managerSessionRedis.get(token.getBytes(), UserVO.class);

        if (manager == null) {
            // 获取配置
            Long client = applicationProperties.l("admin.client.id");
            String secret = applicationProperties.s("admin.client.secret");
            String fetchUserApiUrl = applicationProperties.s("admin.client.api_url");
            int time = (int) (System.currentTimeMillis() / 1000);
            Map<String, String> headers = new HashMap<String, String>(){{
                put("authentication", "client=" + client + "&time=" + time + "&token=" + token + "&security=" + EncryHelper.md5(time + "_" + client + "_" + secret));
            }};
            return httpClient.get(fetchUserApiUrl, headers)
                .map(byteBuf ->{
                    Type cuClassType = new TypeToken<CommonResponse<UserVO>>(){}.getType();
                    CommonResponse<UserVO> userVOCommonResponse = gson.fromJson(byteBuf.toString(Charset.forName("UTF-8")), cuClassType);
                    return userVOCommonResponse.getData();
                })
                .map(userVO -> {
                    managerSessionRedis.set(token.getBytes(), userVO);
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
}
