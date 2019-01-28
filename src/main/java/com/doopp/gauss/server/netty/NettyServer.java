package com.doopp.gauss.server.netty;

import com.doopp.gauss.server.KTApplication;
import com.doopp.gauss.server.handle.HelloHandle;
import com.google.inject.Inject;
import com.google.inject.Injector;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.Consumer;

public class NettyServer {

    @Inject
    private Injector injector;

    public void run() {

        Consumer<HttpServerRoutes> rr = routes -> routes
                .get("/hello", (req, res) -> res.sendString(
                        injector.getInstance(HelloHandle.class).hello()
                ))
                .get("/boy", (req, res) -> res.sendString(
                        injector.getInstance(HelloHandle.class).boy(req)
                ))
                .ws("/game", (in, out) -> out.send(
                        injector.getInstance(HelloHandle.class).game(in.receive())
                ));

        DisposableServer disposableServer = HttpServer.create()
                .route(rr)
                .host("127.0.0.1")
                .port(8090)
                .bind()
                .block();

        disposableServer.onDispose().block();
    }
}



