package com.doopp.gauss.server.netty;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRoutes;

import java.nio.file.Paths;
import java.util.function.Consumer;

public class AppRoute {


    private AppOutbound appOutbound;

    public AppRoute(AppOutbound appOutbound) {
        this.appOutbound = appOutbound;
    }

    public Consumer<HttpServerRoutes> getRoutesConsumer(Injector injector) {

        return routes -> routes
                .get("/test", (req, resp) -> appOutbound.sendJson(
                        req, resp, injector.getInstance(HelloHandle.class).hello(1L)
                        )
                )
                .get("/user/{id}", (req, resp) -> {
                    Long id = Long.valueOf(req.param("id"));
                    return appOutbound.sendJson(
                            req, resp, injector.getInstance(HelloHandle.class).hello(id)
                    );
                })
                .ws("/game2", (in, out) -> out.send(in
                        .receive()
                        .map(ByteBuf::asReadOnly)
                    )
                )
                .ws("/game", (in, out) -> appOutbound.sendWs(
                        in, out, injector.getInstance(HelloHandle.class).game(in, out)
                        )
                )
                .get("/**", appOutbound::sendStatic);
    }
}
