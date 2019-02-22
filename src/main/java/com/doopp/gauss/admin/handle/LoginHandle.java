package com.doopp.gauss.admin.handle;

import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.message.CommonResponse;
import com.doopp.gauss.common.message.response.Authentication;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Slf4j
@Path("/admin/api")
public class LoginHandle {

    @Inject
    private ApplicationProperties applicationProperties;

    @Inject
    private UserDao userDao;

    @GET
    @Path("/authentication")
    public Mono<CommonResponse<Authentication>> authentication() {
        Authentication authentication = new Authentication(
            applicationProperties.l("admin.client.id"),
            applicationProperties.s("admin.client.secret")
        );
        return Mono.just(new CommonResponse<>(authentication));
    }

    @GET
    @Path("/manager")
    public Mono<CommonResponse<User>> sessionManager() {
        return Mono.just(new CommonResponse<>(userDao.getById(1L)));
    }
}
