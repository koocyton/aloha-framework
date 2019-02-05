package com.doopp.gauss.server.module;

import com.doopp.gauss.app.dao.UserDao;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.database.HikariDataSourceProvider;
import com.google.inject.name.Names;
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
        addMapperClass(UserDao.class);
        Names.bindProperties(binder(), new ApplicationProperties());
    }
}
