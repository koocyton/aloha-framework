package com.doopp.gauss.server.filter;

import com.doopp.gauss.oauth.defined.CommonError;
import com.doopp.gauss.oauth.defined.CommonField;
import com.doopp.gauss.oauth.exception.CommonException;
import com.doopp.gauss.oauth.service.ManageService;
import com.doopp.kreactor.KReactorFilter;
import com.doopp.kreactor.RequestAttribute;
import com.google.inject.Injector;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.net.URI;
import java.util.Set;


@Slf4j
public class ManageFilter implements KReactorFilter {

    private ManageService managerService;

    public ManageFilter(Injector injector) {
        this.managerService = injector.getInstance(ManageService.class);
    }

    @Override
    public Mono<RequestAttribute> doFilter(HttpServerRequest request, HttpServerResponse response, RequestAttribute requestAttribute) {

        String uri = URI.create(request.uri()).getPath();

        // 不过滤的uri
        String[] notNeedFilters = new String[]{
                "/manage/login.html",
                "/manage/js",
                "/manage/css",
                "/manage/api/authentication",
                "/manage/api/login",
                "/manage/api/test",
                "/manage/api/post-test",
        };

        // 是否过滤此 URL
        boolean needFilter = isNeedFilter(uri, notNeedFilters);

        // 如果需要过滤
        if (needFilter) {
            // 从 header 里拿到 access token
            Set<Cookie> cookieSet = request.cookies().get(CommonField.SESSION_KEY);
            // log.info("{}", cookieSet);
            // 如果 token 存在，反解 token
            if (cookieSet != null) {
                String sessionKey = cookieSet.iterator().next().value();
                return managerService
                        .getManagerByToken(sessionKey)
                        .map(userVO -> {
                            requestAttribute.setAttribute(CommonField.CURRENT_USER, userVO);
                            return requestAttribute;
                        });
            }
            else {
                return Mono.error(new CommonException(CommonError.WRONG_SESSION));
            }
        }
        return Mono.just(requestAttribute);
    }
}
