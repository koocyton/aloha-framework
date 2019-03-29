package com.doopp.gauss.server.filter;

import com.doopp.gauss.oauth.service.OAuthService;
import com.doopp.gauss.oauth.defined.CommonError;
import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.exception.CommonException;
import com.doopp.kreactor.KReactorFilter;
import com.doopp.kreactor.RequestAttribute;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.net.URI;

@Slf4j
public class OAuthFilter implements KReactorFilter {

    private OAuthService oauthService;

    public OAuthFilter(Injector injector) {
        this.oauthService = injector.getInstance(OAuthService.class);
    }

    @Override
    public Mono<RequestAttribute> doFilter(HttpServerRequest request, HttpServerResponse response, RequestAttribute requestAttribute) {

        String uri = URI.create(request.uri()).getPath();

        // 不过滤的uri
        String[] notNeedFilters = new String[]{
            "/oauth/api/auto-login",
            "/oauth/api/login",
            "/oauth/api/register",
            "/oauth/api/test",
            "/oauth/websocket",
            "/favicon.ico"
        };

        // 是否过滤此 URL
        boolean needFilter = isNeedFilter(uri, notNeedFilters);

        // 如果需要过滤
        if (needFilter) {
            try {
                User user = oauthService.checkRequestHeader(request.requestHeaders().get("authentication"));
                // 如果用户存在
                if (user!=null) {
                    requestAttribute.setAttribute("currentUser", user);
                    return Mono.just(requestAttribute);
                }
                else {
                    return Mono.error(new CommonException(CommonError.WRONG_SESSION));
                }
            }
            catch (CommonException e) {
                return Mono.error(e);
            }
        }
        return Mono.just(requestAttribute);
    }
}
