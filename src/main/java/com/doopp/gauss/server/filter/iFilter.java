package com.doopp.gauss.server.filter;

import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public interface iFilter {

    boolean doFilter(HttpServerRequest request, HttpServerResponse response);
}
