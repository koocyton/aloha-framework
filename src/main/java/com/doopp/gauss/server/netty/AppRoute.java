package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRoutes;
import java.util.function.Consumer;
import java.util.logging.Handler;

@Slf4j
public class AppRoute {

    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        HelloHandle helloHandle = injector.getInstance(HelloHandle.class);

        return routes -> routes
                .get("/test", (req, resp) -> {
                    return appOutbound.sendJson(
                            (req1, resp1) -> helloHandle.hello(1L)
                    );
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
    }
}