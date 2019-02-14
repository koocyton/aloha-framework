package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.entity.User;
import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Handler;

@Slf4j
public class AppRoute {

    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }


    private NettyOutbound sendJson(HttpServerRequest req, HttpServerResponse resp, Publisher<String> handler) {
        return resp
            .status(HttpResponseStatus.OK)
            .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .sendString(handler);
    }


    private NettyOutbound sendJson2(BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler) {
        return handler.apply(a,b);
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        HelloHandle helloHandle = injector.getInstance(HelloHandle.class);

        return routes -> {
            routes
                .get("/test3", (req, resp) -> resp
                    .status(HttpResponseStatus.OK)
                    .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .sendString(Mono.just("bbb"))
                )
                .get("/test2", (req, resp) -> {
                    return resp.sendString(helloHandle.hello(1L));
                })
                .get("/test", (req, resp) -> {
                    return sendJson(req, resp, ()->helloHandle.hello(1L));
                })
    //                .get("/test2", (req, resp) -> appOutbound.sendJson(
    //                        req, resp, injector.getInstance(HelloHandle.class).hello(1L)
    //                        )
    //                )
    //                .get("/user/{id}", (req, resp) -> {
    //                    Long id = Long.valueOf(req.param("id"));
    //                    return appOutbound.sendJson(
    //                            req, resp, injector.getInstance(HelloHandle.class).hello(id)
    //                    );
    //                })
    //                .get("/set_user_cookie/{id}", (req, resp) -> {
    //                    Long id = Long.valueOf(req.param("id"));
    //                    return appOutbound.sendJson(
    //                            req, resp, injector.getInstance(HelloHandle.class).setUserCookie(id, req)
    //                    );
    //                })
                    .ws("/game", (in, out) -> {
                        return appOutbound.sendWs(
                            in, out, injector.getInstance(HelloHandle.class).game()
                        );
                    })
                    .get("/**", appOutbound::sendStatic);
        };
    }
}