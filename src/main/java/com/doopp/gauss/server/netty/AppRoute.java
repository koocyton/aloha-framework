package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.defined.CommonError;
import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.exception.CommonException;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRoutes;
import java.util.function.Consumer;
import com.doopp.gauss.server.netty.HttpRouter;

public class AppRoute {

    private final static Logger logger = LoggerFactory.getLogger(AppRoute.class);

    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        return routes -> routes
                .ws("/game", (in, out) -> ((HttpRouter) in)
                    .filter(in)
                    .sendJson(injector.getInstance(HelloHandle.class).game())
                )
                .get("/test", (req, resp) ->
                        (new AppFilter(injector).doFilter(req, resp))
                                ? resp.sendString(
                                Mono.just(
                                        new GsonBuilder().create().toJson(
                                            injector.getInstance(HelloHandle.class).hello(1L)
                                        )
                                )
                        )
                                : appOutbound.sendJsonException(resp, new CommonException(CommonError.WRONG_SESSION)))

                .get("/test2", (req, resp) -> appOutbound.sendJson(
                        req, resp, injector.getInstance(HelloHandle.class).hello(1L)
                        )
                )
                .get("/user/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return appOutbound.sendJson(
                            req, resp, injector.getInstance(HelloHandle.class).hello(id)
                    );
                })
                .get("/set_user_cookie/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return appOutbound.sendJson(
                            req, resp, injector.getInstance(HelloHandle.class).setUserCookie(id, req)
                    );
                })
                .ws("/game", (in, out) -> {
                    return appOutbound.sendWs(
                            in, out, injector.getInstance(HelloHandle.class).game()
                    );
                })
                .get("/**", appOutbound::sendStatic);
    }
}