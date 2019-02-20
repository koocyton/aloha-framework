package com.doopp.gauss.server;

import com.doopp.gauss.server.filter.AdminFilter;
import com.doopp.gauss.server.filter.ApiFilter;
import com.doopp.gauss.server.netty.Dispatcher;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.CustomMyBatisModule;
import com.doopp.gauss.server.module.RedisModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class KTApplication {

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

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setInjector(injector);
        dispatcher.setHandlePackages("com.doopp.gauss.admin.handle", "com.doopp.gauss.api.handle");
        dispatcher.addFilter(  "/api", new ApiFilter()  );
        dispatcher.addFilter("/admin", new AdminFilter());

        DisposableServer disposableServer = HttpServer.create()
                .route(dispatcher.setHandleMethodRoute())
                .host(host)
                .port(port)
                .wiretap(true)
                .bindNow();

        System.out.printf("\nLaunched http server http://%s:%d/\n\n", host, port);

        disposableServer.onDispose().block();
    }
}