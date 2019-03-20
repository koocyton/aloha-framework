package com.doopp.gauss.oauth.service;

import com.doopp.gauss.oauth.entity.Client;
import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.entity.vo.UserVO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ManageService {

    Mono<UserVO> getManagerByToken(String token);

    Mono<List<User>> getUsers();

    Mono<User> getUser(Long id);

    Mono<List<Client>> getClients();
}