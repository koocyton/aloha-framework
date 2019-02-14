package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@Slf4j
public class AppRoute {

    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }

    private NettyOutbound sendJson(HttpServerRequest req, HttpServerResponse resp) {
        return null;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        HelloHandle helloHandle = injector.getInstance(HelloHandle.class);

        return routes -> {
            routes
                .get("/test", (req, resp) -> sendJson(req, resp))
                .get("/test", (req, resp) ->
                    appOutbound.sendJson(req, resp, () -> helloHandle.hello(1L))
                )
                .get("/user/{id}", (req, resp) ->
                    appOutbound.sendJson(req, resp, () -> helloHandle.hello(Long.valueOf(req.param("id"))))
                )
//                .get("/set_user_cookie/{id}", (req, resp) -> {
//                    Long id = Long.valueOf(req.param("id"));
//                    return appOutbound.sendJson(req, resp, () -> helloHandle.setUserCookie(id, req));
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