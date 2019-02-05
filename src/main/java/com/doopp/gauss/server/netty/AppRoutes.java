package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.defined.CommonError;
import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.exception.CommonException;
import com.doopp.gauss.server.filter.AppFilter;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.Consumer;

public class AppRoutes {

    private final static Logger logger = LoggerFactory.getLogger(AppRoutes.class);

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        CustomOutbound customOutbound = new CustomOutbound();

        AppFilter appFilter = new AppFilter();

        return routes -> routes
                .get("/test", (req, resp) -> {
                    return AppFilter.INSTANCE
                            .doFilter2((request, response) -> {
                               return customOutbound.sendJson(
                                       req, resp, injector.getInstance(HelloHandle.class).hello(1L)
                               );
                            })
                            .unFilter2((request, response) -> {
                                return customOutbound.sendJsonException(resp, new CommonException(CommonError.WRONG_SESSION));
                            })
                            .send();
                })
                .get("/user/{id}", (req, resp) -> {
                    if (!appFilter.doFilter(req, resp, injector)) {
                        return customOutbound.sendJsonException(resp, new CommonException(CommonError.WRONG_SESSION));
                    }
                    else {
                        Long id = Long.valueOf(req.param("id"));
                        return customOutbound.sendJson(
                                req, resp, injector.getInstance(HelloHandle.class).hello(id)
                        );
                    }
                })
                .ws("/game", (in, out) -> {
                    return customOutbound.sendWs(
                            in, out, injector.getInstance(HelloHandle.class).game(in.receive())
                    );
                })
                .get("/**",(req, resp) -> {
                    if (!appFilter.doFilter(req, resp, injector)) {
                        return customOutbound.sendNotFoundPage(resp, new CommonException(CommonError.WRONG_SESSION));
                    }
                    else {
                        return customOutbound.sendStatic(req, resp);
                    }
                });
    }
}
