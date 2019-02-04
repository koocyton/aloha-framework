package com.doopp.gauss.server.module;

import com.doopp.gauss.app.AppRoutes;
import com.doopp.gauss.app.service.UserService;
import com.doopp.gauss.app.service.impl.UserServiceImpl;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.doopp.gauss.app.handle.HelloHandle;
import com.doopp.gauss.server.util.IdWorker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.inject.*;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class ApplicationModule extends AbstractModule {

	@Override
	public void configure() {
		bind(UserService.class).to(UserServiceImpl.class).in(Scopes.SINGLETON);
		bind(HelloHandle.class).in(Scopes.SINGLETON);
		bind(AppRoutes.class).in(Scopes.SINGLETON);
	}

	@Singleton
	@Provides
	public IdWorker userIdWorker() {
		return new IdWorker(1, 1);
	}

	@Singleton
	@Provides
	public ApplicationProperties applicationProperties() {
		return new ApplicationProperties();
	}

	@Provides
	public EventLoopGroup eventLoopGroup() {
		return new NioEventLoopGroup();
	}

	@Singleton
	@Provides
	public Gson gson () {
		return new GsonBuilder()
			.serializeNulls()
			.setDateFormat("yyyy-MM-dd HH:mm:ss")
			.setLongSerializationPolicy(LongSerializationPolicy.STRING)
			.create();
	}

//	@Singleton
//	@Provides
//	private Configuration viewConfiguration() {
//
//		Version version = new Version("2.3.23");
//		DefaultObjectWrapperBuilder defaultObjectWrapperBuilder = new DefaultObjectWrapperBuilder(version);
//
//		Configuration cfg = new Configuration(version);
//		cfg.setObjectWrapper(defaultObjectWrapperBuilder.build());
//		cfg.setDefaultEncoding("UTF-8");
//		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
//		cfg.setLogTemplateExceptions(false);
//		// Sets how errors will appear. Here we assume we are developing HTML pages.
//		// For production systems TemplateExceptionHandler.RETHROW_HANDLER is better.
//		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
//		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
//		cfg.setClassForTemplateLoading(this.getClass(), "/template");
//		// Bind instance for DI
//		// bind(Configuration.class).toInstance(cfg);
//		return cfg;
//	}
}
