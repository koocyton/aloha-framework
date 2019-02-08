package com.doopp.gauss.server;

import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.netty.AppFilter;
import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.CustomMyBatisModule;
import com.doopp.gauss.server.module.RedisModule;
import com.doopp.gauss.server.netty.AppRoute;
import com.doopp.gauss.server.netty.AppOutbound;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class KTApplication {

    private final static Logger logger = LoggerFactory.getLogger(KTApplication.class);

    public static void main(String[] args) {

        // BasicConfigurator.configure();

        System.setProperty("applicationPropertiesConfig", args[0]);

        Injector injector = Guice.createInjector(
            new CustomMyBatisModule(),
            new RedisModule(),
            new ApplicationModule()
        );

        ApplicationProperties applicationProperties = injector.getInstance(ApplicationProperties.class);
        String host = applicationProperties.s("server.host");
        int port = applicationProperties.i("server.port");

        AppRoute appRoutes = new AppRoute(
            new AppOutbound(new AppFilter(injector))
        );

        DisposableServer disposableServer = HttpServer.create()
                .route(appRoutes.getRoutesConsumer(injector))
                .host(host)
                .port(port)
                .bind()
                .block();

        logger.warn("Launched server http://{}:{}/game.html", host, port);

        disposableServer.onDispose().block();
    }
}
