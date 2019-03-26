package com.doopp.gauss.server;

import com.doopp.gauss.server.database.HikariDataSourceProvider;
import com.doopp.gauss.server.filter.ManageFilter;
import com.doopp.gauss.server.filter.OAuthFilter;
import com.doopp.gauss.server.netty.Dispatcher;
import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.RedisModule;
import com.github.pagehelper.PageInterceptor;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.helper.JdbcHelper;
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

                // application Properties
                binder -> Names.bindProperties(binder, properties),

                // mybatis
                new MyBatisModule() {
                    @Override
                    protected void initialize() {
                        install(JdbcHelper.MySQL);
                        bindDataSourceProviderType(HikariDataSourceProvider.class);
                        // bindDataSourceProviderType(DruidDataSourceProvider.class);
                        bindTransactionFactoryType(JdbcTransactionFactory.class);
                        addMapperClasses("com.doopp.gauss.oauth.dao");
                        addInterceptorClass(PageInterceptor.class);
                        // Names.bindProperties(binder(), new ApplicationProperties());
                    }
                },

                // redis
                new RedisModule(),

                // application
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
                .route(dispatcher.routesBuilder())
                .host(host)
                .port(port)
                .wiretap(true)
                .bindNow();

        System.out.printf("\nBackend System http://%s:%d/manage/login.html\n", host, port);
        System.out.printf("Chat System http://%s:%d/manage/chat-login.html\n\n", host, port);

        disposableServer.onDispose().block();
    }
}