package com.doopp.gauss.api.handle;

import com.doopp.gauss.api.service.OAuthService;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.common.message.CommonResponse;
import com.doopp.gauss.common.message.OAuthRequest;
import com.doopp.gauss.common.message.request.LoginRequest;
import com.doopp.gauss.common.message.request.RegisterRequest;
import com.doopp.gauss.common.message.response.SessionToken;
import com.google.inject.Inject;

import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public class OAuthHandle {

    @Inject
    private OAuthService oauthService;

    @POST
    @Path("/api/login")
    public CommonResponse<SessionToken> login(@BeanParam OAuthRequest<LoginRequest> commonRequest) throws CommonException {
        LoginRequest loginRequest = commonRequest.getData();
        oauthService.checkLoginRequest(commonRequest);
        User user = oauthService.userLogin(loginRequest.getAccount(), loginRequest.getPassword());
        com.doopp.gauss.common.message.response.SessionToken sessionToken = new com.doopp.gauss.common.message.response.SessionToken(oauthService.createSessionToken(user));
        return new CommonResponse<>(sessionToken);
    }

    @POST
    @Path("/api/register")
    public CommonResponse<SessionToken> register(@BeanParam OAuthRequest<RegisterRequest> commonRequest) throws CommonException {
        RegisterRequest registerRequest = commonRequest.getData();
        oauthService.checkRegisterRequest(commonRequest);
        User user = oauthService.userRegister(registerRequest.getAccount(), registerRequest.getPassword());
        com.doopp.gauss.common.message.response.SessionToken sessionToken = new SessionToken(oauthService.createSessionToken(user));
        return new CommonResponse<>(sessionToken);
    }
}
