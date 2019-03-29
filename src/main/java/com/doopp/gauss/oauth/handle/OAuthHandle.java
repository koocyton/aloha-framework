package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.oauth.service.OAuthService;
import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.entity.vo.UserVO;
import com.doopp.gauss.oauth.exception.CommonException;
import com.doopp.gauss.oauth.mapper.UserMapper;
import com.doopp.gauss.oauth.message.OAuthRequest;
import com.doopp.gauss.oauth.message.request.LoginRequest;
import com.doopp.gauss.oauth.message.request.RegisterRequest;
import com.doopp.gauss.oauth.message.response.SessionToken;
import com.doopp.kreactor.RequestAttributeParam;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path("/oauth/api")
public class OAuthHandle {

    @Inject
    private OAuthService oauthService;

    @POST
    @Path("/auto-login")
    @Produces({MediaType.APPLICATION_JSON, "charset=UTF-8"})
    public Mono<SessionToken> autoLogin(@BeanParam OAuthRequest<LoginRequest> commonRequest) {
        try {
            oauthService.checkLoginRequest(commonRequest);
        }
        catch(CommonException e) {
            return Mono.error(e);
        }
        LoginRequest loginRequest = commonRequest.getData();
        return oauthService
            .userAutoLogin(loginRequest.getAccount())
            .map(user ->new SessionToken(oauthService.createSessionToken(user)));
    }

    @POST
    @Path("/login")
    @Produces({MediaType.APPLICATION_JSON, "charset=UTF-8"})
    public Mono<SessionToken> login(@BeanParam OAuthRequest<LoginRequest> commonRequest) {
        try {
            oauthService.checkLoginRequest(commonRequest);
        }
        catch(CommonException e) {
            return Mono.error(e);
        }
        LoginRequest loginRequest = commonRequest.getData();
        return oauthService
            .userLogin(
                loginRequest.getAccount(),
                loginRequest.getPassword())
            .map(user ->
                new SessionToken(oauthService.createSessionToken(user))
            );
    }

    @POST
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON, "charset=UTF-8"})
    public Mono<SessionToken> register(@BeanParam OAuthRequest<RegisterRequest> commonRequest) {
        try {
            oauthService.checkRegisterRequest(commonRequest);
        }
        catch(CommonException e) {
            return Mono.error(e);
        }
        RegisterRequest registerRequest = commonRequest.getData();
        return oauthService
            .userRegister(
                registerRequest.getAccount(),
                registerRequest.getPassword())
            .map(user ->
                new SessionToken(oauthService.createSessionToken(user))
            );
    }

    @GET
    @Path("/user")
    @Produces({MediaType.APPLICATION_JSON, "charset=UTF-8"})
    public Mono<UserVO> getUserInfo(@RequestAttributeParam("currentUser") User user) {
        UserVO userVO = UserMapper.INSTANCE.toUserDTO(user);
        return Mono.just(userVO);
    }
}
