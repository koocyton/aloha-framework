package com.doopp.gauss.app;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.gson.Gson;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class AppRoutes {

    private final static Logger logger = LoggerFactory.getLogger(AppRoutes.class);

    @Inject
    private HelloHandle helloHandle;

    @Inject
    private Gson gson;

    private final Path publicPath = getPublicPath();

    AppRoutes() throws URISyntaxException {

    }

    public Consumer<HttpServerRoutes> getRoutesConsumer() throws URISyntaxException, NoSuchMethodException {

        // Path publicPath = this.getPublicPath();

        return routes -> routes
                .get("/user/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return sendJson(req, resp, helloHandle.hello(id));
                })
                .ws("/game", (in, out) -> out.send(
                    helloHandle.game(in.receive())
                ))
                .directory("/", publicPath);
    }

    private Path getPublicPath() throws URISyntaxException {
        // logger.info("{}", getClass().getResource(""));
        // logger.info("{}", getClass().getResource("/resources"));
        // logger.info("{}", getClass().getResource("/resources/public"));
        // logger.info("{}", getClass().getResourceAsStream("/public"));
        // logger.info("{}", getClass().getResource("/public"));
        return Paths.get(AppRoutes.class.getResource("/public").toURI());
    }

    private <T> NettyOutbound sendJson(HttpServerRequest req, HttpServerResponse resp, T handle) {
        Mono<String> monoJson = Mono.just(gson.toJson(handle));
        return resp
                .status(HttpResponseStatus.OK)
                .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .sendString(monoJson);
    }
}
