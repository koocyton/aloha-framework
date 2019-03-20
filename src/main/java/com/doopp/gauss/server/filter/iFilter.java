package com.doopp.gauss.server.filter;

import com.doopp.gauss.server.resource.RequestAttribute;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public interface iFilter {

    Mono<RequestAttribute> doFilter(HttpServerRequest request, HttpServerResponse response, RequestAttribute requestAttribute);

    default boolean isNeedFilter(String uri, String[] notNeedFilters) {
        // 默认需要过滤
        boolean needFilter = true;
        // 如果有中包含不过滤的uri，则不进行过滤
        for (String notNeedFilter : notNeedFilters) {
            if (uri.length() >= notNeedFilter.length() && uri.substring(0, notNeedFilter.length()).equals(notNeedFilter)) {
                needFilter = false;
                break;
            }
        }
        // 返回
        return needFilter;
    }
}
