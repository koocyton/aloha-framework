package com.doopp.gauss.app;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.inject.Inject;
import com.sun.net.httpserver.HttpsParameters;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class AppRoutes<Q, P> {

    private final static Logger logger = LoggerFactory.getLogger(AppRoutes.class);

    @Inject
    private HelloHandle helloHandle;

    @Inject
    private Gson gson;

    AppRoutes() {

    }

    public Consumer<HttpServerRoutes> getRoutesConsumer() throws URISyntaxException {

        // logger.info("{}", getClass().getResource(""));
        // logger.info("{}", getClass().getResource("/resources"));
        // logger.info("{}", getClass().getResource("/resources/public"));
        // logger.info("{}", getClass().getResourceAsStream("/public"));
        // logger.info("{}", getClass().getResource("/public"));

        Path publicPath = Paths.get(getClass().getResource("/public").toURI());

        return routes -> routes
            .get("/user/{id}", (req, resp) -> {
                Long id = Long.valueOf(req.param("id"));
                return sendJson(resp, helloHandle.hello(id));
            })
            .ws("/game", (in, out) -> out.send(
                helloHandle.game(in.receive())
            ))
            .directory("/", publicPath);
    }

    private NettyOutbound sendJson(HttpServerResponse resp, Mono<String> handle) {
        return resp
                .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .status(HttpResponseStatus.OK)
                .sendString(handle);
    }

}
