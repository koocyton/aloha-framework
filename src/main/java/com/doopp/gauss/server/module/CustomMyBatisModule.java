package com.doopp.gauss.server.module;

import com.doopp.gauss.server.database.HikariDataSourceProvider;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.helper.JdbcHelper;

public class CustomMyBatisModule extends MyBatisModule {

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
}
