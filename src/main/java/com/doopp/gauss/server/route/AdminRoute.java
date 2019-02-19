package com.doopp.gauss.server.route;

import com.doopp.gauss.admin.handle.LoginHandle;
import com.doopp.gauss.server.netty.AppOutbound;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRoutes;

@Slf4j
public class AdminRoute {

    private AppOutbound ob;

    public AdminRoute(AppOutbound appOutbound) {
        this.ob = appOutbound;
    }

    public void setRoutes(HttpServerRoutes routes) {

            routes
                    .get("/admin/api/authentication", (req, resp) -> resp.sendString(Mono.just("abc"))
//                            ob.sendJson(req, resp, (injector) ->
//                                    injector.getInstance(LoginHandle.class).authentication()
//                            )
                    )

//                    .get("/api/login", (req, resp) ->
//                            ob.sendJson(
//                                    req.receiveContent().doOnRequest((c)->{
//                                        log.info("{}", c);
//                                    })
//                    ))

//                    .post("/api/register", (req, resp) ->
//                            ob.sendJson(req, resp, (injector) ->
//                                    injector.getInstance(OAuthHandle.class).hello(Long.valueOf(req.param("id")))
//                            )
//                    )
//                    .get("/api/user", (req, resp) -> {
//                        Long id = Long.valueOf(req.param("id"));
//                        return ob.sendJson(req, resp, (injector) ->
//                                injector.getInstance(OAuthHandle.class).setUserCookie(id, req)
//                        );
//                    })
                    // .ws("/game", (in, out) ->
                    //         ob.sendWs(
                    //                 in, out, (injector) -> injector.getInstance(OAuthHandle.class).login()
                    //         )
                    // )
                    .get("/admin/**", ob::sendStatic);

    }
}