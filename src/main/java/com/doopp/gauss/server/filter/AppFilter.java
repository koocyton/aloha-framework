package com.doopp.gauss.server.filter;

import com.doopp.gauss.app.defined.CommonError;
import com.doopp.gauss.app.defined.CommonField;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.server.exception.CommonException;
import com.doopp.gauss.app.service.UserService;
import com.doopp.gauss.server.resource.RequestAttribute;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import java.net.URI;
import java.util.Set;

public class AppFilter {

    private static Logger logger = LoggerFactory.getLogger(AppFilter.class);

    private CommonException filterException;

    @Inject
    private UserService userService;

    @Inject
    private Gson gson;

    public boolean doFilter(HttpServerRequest httpRequest, HttpServerResponse httpResponse, Injector injector) {

        String uri = URI.create(httpRequest.uri()).getPath();

        // 不过滤的uri
        String[] notFilters = new String[]{
                "/login",
                "/api/login",
                "/api/register",
                "/favicon.ico",
        };

        // 是否过滤
        boolean doFilter = true;

        // 如果uri中包含不过滤的uri，则不进行过滤
        for (String notFilter : notFilters) {
            if (uri.contains(notFilter)) {
                doFilter = false;
                break;
            }
        }

        // 执行过滤 验证通过的会话
        try {
            if (doFilter) {
                // 从 header 里拿到 access token
                Set<Cookie> cookies = httpRequest.cookies().get(CommonField.SESSION_KEY);
                // 如果 token 存在，反解 token
                if (cookies == null) {
                    // String sessionKey = ((Cookie) cookies).value();
                    // User user = userService.getUserByToken(sessionKey);
                    User user = userService.getUserById(1L);
                    // 如果能找到用户
                    if (user != null) {
                        RequestAttribute requestAttribute = injector.getInstance(RequestAttribute.class);
                        logger.info("");
                        logger.info("{}", requestAttribute);
                        requestAttribute.setAttribute(CommonField.CURRENT_USER, user);
                        return true;
                    }
                    // 如果不能找到用户
                    else {
                        filterException = new CommonException(CommonError.ACCOUNT_NO_EXIST);
                        return false;
                    }
                }
                // 如果 token 不对
                else {
                    filterException = new CommonException(CommonError.WRONG_SESSION);
                    return false;
                }
            }
            // 不用校验
            else {
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            filterException = new CommonException(CommonError.WRONG_SESSION);
            return false;
        }
    }

    public NettyOutbound sendFilterException(HttpServerResponse resp) {
        String monoJson = new Gson().toJson(filterException);
        return resp
                .status(HttpResponseStatus.OK)
                .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .sendString(Mono.just(monoJson));
    }
}
