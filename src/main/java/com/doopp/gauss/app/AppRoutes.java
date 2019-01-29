package com.doopp.gauss.app;

import com.doopp.gauss.app.handle.HelloHandle;
import com.google.inject.Inject;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.Consumer;

public class AppRoutes {

    @Inject
    private HelloHandle helloHandle;

    public Consumer<HttpServerRoutes> routesConsumer() {
        return routes -> routes
            .get("/hello", (req, res) -> res.sendString(
                helloHandle.hello()
            ))
            .get("/boy", (req, res) -> res.sendString(
                helloHandle.boy(req)
            ))
            .ws("/game", (in, out) -> out.send(
                helloHandle.game(in.receive())
            ))
            .directory("/", resource);
    }
}
