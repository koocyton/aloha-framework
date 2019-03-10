package com.doopp.gauss.oauth.service;

import com.doopp.gauss.common.entity.Client;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.entity.vo.UserVO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ManageService {

    Mono<UserVO> getManagerByToken(String token);

    Mono<List<User>> getUsers();

    Mono<List<Client>> getClients();
}