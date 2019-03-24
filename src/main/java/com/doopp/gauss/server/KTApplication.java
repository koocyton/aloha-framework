package com.doopp.gauss.server;

import com.doopp.gauss.server.filter.ManageFilter;
import com.doopp.gauss.server.filter.OAuthFilter;
import com.doopp.gauss.server.netty.Dispatcher;
import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.CustomMyBatisModule;
import com.doopp.gauss.server.module.RedisModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class KTApplication {


    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        properties.load(new FileInputStream(args[0]));

        Injector injector = Guice.createInjector(
            binder -> Names.bindProperties(binder, properties),
            new CustomMyBatisModule(),
            new RedisModule(),
            new ApplicationModule()
        );

        String host = properties.getProperty("server.host", "127.0.0.1");
        int port = Integer.valueOf(properties.getProperty("server.port", "8081"));

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setInjector(injector);
        dispatcher.setHandlePackages("com.doopp.gauss.oauth.handle");
        dispatcher.addFilter("/oauth", new OAuthFilter(injector));
        dispatcher.addFilter("/manage", new ManageFilter(injector));

        DisposableServer disposableServer = HttpServer.create()
                .route(dispatcher.setHandleMethodRoute())
                .host(host)
                .port(port)
                .wiretap(true)
                .bindNow();

        System.out.printf("\nBackend System http://%s:%d/manage/login.html\n", host, port);
        System.out.printf("Chat System http://%s:%d/manage/chat-login.html\n\n", host, port);

        disposableServer.onDispose().block();
    }
}