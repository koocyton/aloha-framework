package com.doopp.gauss.server.module;

import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.server.database.HikariDataSourceProvider;
import com.github.pagehelper.PageInterceptor;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.configuration.settings.ConfigurationSetting;
import org.mybatis.guice.configuration.settings.InterceptorConfigurationSettingProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;

import javax.inject.Provider;
import java.util.Properties;

public class CustomMyBatisModule extends MyBatisModule {

    @Override
    protected void initialize() {
        install(JdbcHelper.MySQL);
        bindDataSourceProviderType(HikariDataSourceProvider.class);
        // bindDataSourceProviderType(DruidDataSourceProvider.class);
        bindTransactionFactoryType(JdbcTransactionFactory.class);
        addMapperClasses("com.doopp.gauss.oauth.dao");
        addInterceptorClass(PageInterceptor.class);
        Names.bindProperties(binder(), new ApplicationProperties());
    }
}
