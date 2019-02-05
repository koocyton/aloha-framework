package com.doopp.gauss.server;

import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.CustomMyBatisModule;
import com.doopp.gauss.server.module.RedisModule;
import com.doopp.gauss.server.netty.AppRoutes;
import com.google.inject.Guice;
import com.google.inject.Injector;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class KTApplication {

    public static void main(String[] args) {

        System.setProperty("applicationPropertiesConfig", args[0]);

        Injector injector = Guice.createInjector(
            new CustomMyBatisModule(),
            new RedisModule(),
            new ApplicationModule()
        );

        ApplicationProperties applicationProperties = injector.getInstance(ApplicationProperties.class);
        String host = applicationProperties.s("server.host");
        int port = applicationProperties.i("server.port");

        DisposableServer disposableServer = HttpServer.create()
                .route(new AppRoutes().getRoutesConsumer(injector))
                .host(host)
                .port(port)
                .bind()
                .block();

        System.out.printf("\nLaunched server http://%s:%d/index.html\n\n", host, port);

        disposableServer.onDispose().block();
    }
}
