package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.defined.CommonField;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.app.service.UserService;
import com.doopp.gauss.server.resource.RequestAttribute;
import com.google.inject.Injector;
import io.netty.handler.codec.http.cookie.Cookie;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import java.net.URI;
import java.util.Set;

public class AppFilter {

    private Injector injector;

    public AppFilter(Injector injector) {

        this.injector = injector;
    }

    boolean doFilter(HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

        String uri = URI.create(httpRequest.uri()).getPath();

        // 不过滤的uri
        String[] notFilters = new String[]{
                "/game.html",
                "/index.html",
                "/favicon.ico",
                "/css",
                "/js",
                "/bootstrap",
                "/tpl",
                "/user",
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
                if (cookies != null) {
                    UserService userService = injector.getInstance(UserService.class);
                    String sessionKey = ((Cookie) cookies).value();
                    User user = userService.getUserByToken(sessionKey);
                    // 如果能找到用户
                    if (user != null) {
                        RequestAttribute requestAttribute = injector.getInstance(RequestAttribute.class);
                        requestAttribute.setAttribute(CommonField.CURRENT_USER, user);
                        return true;
                    }
                    // 如果不能找到用户
                    else {
                        return false;
                    }
                }
                // 如果 token 不对
                else {
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
            return false;
        }
    }
}
