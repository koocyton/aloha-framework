package com.doopp.gauss.server;

import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.CustomMyBatisModule;
import com.doopp.gauss.server.module.RedisModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.Consumer;

public class KTApplication {

    public static void main(String[] args) throws Exception, NullPointerException {

        System.setProperty("applicationPropertiesConfig", args[0]);

        Injector injector = Guice.createInjector(
                new CustomMyBatisModule(),
                new RedisModule(),
                new ApplicationModule()
        );

        DisposableServer disposableServer = HttpServer.create()
                .route(new routes(injector))
                .host("127.0.0.1")
                .port(8090)
                .bind()
                .block();

        System.out.print("\n [OK] launched server http://127.0.0.1:8090/index.html" + "\n");

        disposableServer.onDispose().block();
    }

    private static class routes implements Consumer<HttpServerRoutes> {

        private Injector injector;

        routes (Injector injector) {
            this.injector = injector;
        }

        @Override
        public void accept(HttpServerRoutes httpServerRoutes) {

        }
    }
}
