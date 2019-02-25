package com.doopp.gauss.api.service;


import com.doopp.gauss.common.entity.Client;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.common.message.OAuthRequest;
import com.doopp.gauss.common.message.request.LoginRequest;
import com.doopp.gauss.common.message.request.RegisterRequest;

public interface OAuthService {

    Client checkCommonRequest(OAuthRequest commonRequest) throws CommonException;

    User checkRequestHeader(String authenticationHeader) throws CommonException;

    void checkLoginRequest(OAuthRequest<LoginRequest> commonRequest) throws CommonException;

    void checkRegisterRequest(OAuthRequest<RegisterRequest> commonRequest) throws CommonException;

    User userRegister(String account, String password) throws CommonException;

    User userLogin(String account, String password) throws CommonException;

    String createSessionToken(User user);

    void removeSessionToken(String token);

    void removeSessionToken(Long userId);

}
