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

    private AppOutbound ob;

    public AppRoute(AppOutbound appOutbound) {
        this.ob = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        HelloHandle helloHandle = injector.getInstance(HelloHandle.class);

        return routes -> {
            routes
                .get("/test", (req, resp) ->
                    ob.sendJson(req, resp, () -> helloHandle.hello(1L))
                )
                .get("/user/{id}", (req, resp) ->
                    ob.sendJson(req, resp, () -> helloHandle.hello(Long.valueOf(req.param("id"))))
                )
                .get("/set_user_cookie/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return ob.sendJson(req, resp, () -> helloHandle.setUserCookie(id, req));
                })
                .ws("/game", (in, out) -> {
                    return ob.sendWs(
                        in, out, ()->injector.getInstance(HelloHandle.class).game()
                    );
                })
                .get("/**", ob::sendStatic);
        };
    }
}