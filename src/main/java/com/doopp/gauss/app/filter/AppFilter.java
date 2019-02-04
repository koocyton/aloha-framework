package com.doopp.gauss.app.filter;

import com.doopp.gauss.app.defined.CommonError;
import com.doopp.gauss.app.defined.CommonField;
import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.app.exception.CommonException;
import com.doopp.gauss.app.service.UserService;
import com.google.inject.Inject;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import java.net.URI;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class AppFilter {

    private static Logger logger = LoggerFactory.getLogger(AppFilter.class);

    private Exception filterException;

    @Inject
    private UserService userService;

    public boolean doFilter(HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

        String uri = URI.create(httpRequest.uri()).getPath();

        // 不过滤的uri
        String[] notFilters = new String[]{
                "/login",
                "/api/login",
                "/api/register",
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
                    String sessionKey = ((Cookie) cookies).value();
                    User user = userService.getUserByToken(sessionKey);
                    // 如果能找到用户
                    //if (user != null) {
                        //httpRequest.receiveContent().
                        //ctx.channel().attr(AttributeKey.valueOf("currentUser")).set(user);
                    //    return true;
                    //}
                    // 如果不能找到用户
                    //else {
                        filterException = new CommonException(CommonError.ACCOUNT_NO_EXIST);
                    //    return false;
                    //}
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
        }
        filterException = new CommonException(CommonError.WRONG_SESSION);
        return false;
    }

    private static void filterMessage(HttpResponseStatus responseStatus, FullHttpResponse httpResponse, String message) {
        String json = "{\"status\":" + responseStatus.code() + ", \"message\":\"" + message + "\"}";
        httpResponse.setStatus(responseStatus);
        httpResponse.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        httpResponse.content().writeBytes(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
    }
}
