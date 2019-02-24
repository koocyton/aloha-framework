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
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.nio.charset.Charset;
import java.time.Duration;

@Slf4j
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

        for(int ii=0; ii<10; ii++) {
            createHttpClient(ii);
        }

        DisposableServer disposableServer = HttpServer.create()
                .route(dispatcher.setHandleMethodRoute())
                .host(host)
                .port(port)
                .wiretap(true)
                .bindNow();

        System.out.printf("\nLaunched http server http://%s:%d/\n\n", host, port);

        disposableServer.onDispose().block();
    }

    private static void createHttpClient(int ii) {
        HttpClient.create()
            .post()
            .uri("https://www.doopp.com/reqinfo.php")
            .send(ByteBufFlux.fromString(Flux.just("a=1&", "b=2&", "c=3")))
            .responseSingle((res, content) -> content.map(byteBuf -> {
                    // log.info("{} : {}", ii, res.status().code());
                    log.info(byteBuf.toString(Charset.forName("UTF-8")));
                    return byteBuf;
                })
            )
            .subscribe();
    }
}