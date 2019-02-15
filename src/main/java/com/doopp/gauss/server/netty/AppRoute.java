package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRoutes;
import java.util.function.Consumer;

@Slf4j
public class AppRoute {

    private AppOutbound ob;

    public AppRoute(AppOutbound appOutbound) {
        this.ob = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer() {

        return routes -> {
            routes
                    .get("/test_1", (req, resp) ->
                            ob.sendJson(req, resp, (injector) ->
                                    injector.getInstance(HelloHandle.class).hello(1L)
                            )
                    )
                    .get("/test", (req, resp) ->
                            ob.sendJson(req, resp, (injector) ->
                                    injector.getInstance(HelloHandle.class).hello(1L)
                            )
                    )
                    .get("/user/{id}", (req, resp) ->
                            ob.sendJson(req, resp, (injector) ->
                                    injector.getInstance(HelloHandle.class).hello(Long.valueOf(req.param("id")))
                            )
                    )
                    .get("/set_user_cookie/{id}", (req, resp) -> {
                        Long id = Long.valueOf(req.param("id"));
                        return ob.sendJson(req, resp, (injector) ->
                                injector.getInstance(HelloHandle.class).setUserCookie(id, req)
                        );
                    })
                    .ws("/game", (in, out) ->
                            ob.sendWs(
                                    in, out, (injector) -> injector.getInstance(HelloHandle.class).game()
                            )
                    )
                    .get("/**", ob::sendStatic);
        };
    }
}