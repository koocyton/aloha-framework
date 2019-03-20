package com.doopp.gauss.oauth.service;

import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.exception.CommonException;
import com.doopp.gauss.oauth.message.OAuthRequest;
import com.doopp.gauss.oauth.message.request.LoginRequest;
import com.doopp.gauss.oauth.message.request.RegisterRequest;
import reactor.core.publisher.Mono;

public interface OAuthService {

    User checkRequestHeader(String authenticationHeader) throws CommonException;

    Mono<User> userRegister(String account, String password);

    Mono<User> userLogin(String account, String password);

    Mono<User> userAutoLogin(String account);

    void checkLoginRequest(OAuthRequest<LoginRequest> commonRequest) throws CommonException;

    void checkRegisterRequest(OAuthRequest<RegisterRequest> commonRequest) throws CommonException;

    String createSessionToken(User user);
}
