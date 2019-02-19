package com.doopp.gauss.server.filter;

import com.doopp.gauss.admin.service.ManagerService;
import com.doopp.gauss.common.defined.CommonField;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.server.resource.RequestAttribute;
import com.google.inject.Inject;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.net.URI;
import java.util.Set;


@Slf4j
public class AdminFilter implements iFilter {

    @Inject
    private ManagerService managerService;

    @Inject
    private RequestAttribute requestAttribute;

    @Override
    public boolean doFilter(HttpServerRequest request, HttpServerResponse response) {

        String uri = URI.create(request.uri()).getPath();

        // 不过滤的uri
        String[] notFilters = new String[]{
            "/admin/login.html",
            "/admin/js",
            "/admin/css",
            "/admin/api/authentication",
            "/admin/api/login",

            "/api/login",
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
                Set<Cookie> cookies = request.cookies().get(CommonField.SESSION_KEY);
                //request.cookies().forEach((a, b)->{
                //    log.info("{}", a);
                //    log.info("{}", b);
                //});
                // 如果 token 存在，反解 token
                if (cookies != null) {
                    String sessionKey = ((Cookie) cookies).value();
                    User user = managerService.getManagerByToken(sessionKey);
                    // 如果能找到用户
                    if (user != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
