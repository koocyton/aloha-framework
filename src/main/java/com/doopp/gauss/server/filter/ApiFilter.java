package com.doopp.gauss.server.filter;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

@Slf4j
public class ApiFilter implements iFilter {

    @Override
    public boolean doFilter(HttpServerRequest request, HttpServerResponse response) {
        return true;
    }
}
