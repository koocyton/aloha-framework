package com.doopp.gauss.server;

import com.doopp.gauss.server.database.HikariDataSourceProvider;
import com.doopp.gauss.server.filter.ManageFilter;
import com.doopp.gauss.server.filter.OAuthFilter;
import com.doopp.gauss.server.module.ApplicationModule;
import com.doopp.gauss.server.module.RedisModule;
import com.doopp.kreactor.KReactorServer;
import com.github.pagehelper.PageInterceptor;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.helper.JdbcHelper;

import java.io.FileInputStream;
import java.util.Properties;

public class KTApplication {

    public static void main(String[] args) throws Exception {

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

        KReactorServer.create()
                .injector(injector)
                .bind("127.0.0.1", 8081)
                .basePackages("com.doopp.gauss.oauth.handle")
                .addFilter("/oauth", new OAuthFilter(injector))
                .addFilter("/manager", new ManageFilter(injector))
                .launch();
    }
}
