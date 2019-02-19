package com.doopp.gauss.admin.handle;

import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.message.CommonResponse;
import com.doopp.gauss.common.message.response.Authentication;
import com.doopp.gauss.server.application.ApplicationProperties;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Slf4j
public class LoginHandle {

    @Inject
    private ApplicationProperties applicationProperties;

    @Inject
    private UserDao userDao;

    @GET
    @Path("/admin/api/authentication")
    public CommonResponse<Authentication> authentication() {
        Authentication authentication = new Authentication(
            applicationProperties.l("admin.client.id"),
            applicationProperties.s("admin.client.secret")
        );
        return new CommonResponse<>(authentication);
    }

    @GET
    @Path("/admin/api/user")
    public CommonResponse<User> abc(@QueryParam("id") Long id) {
        log.info("{}", id);
        return new CommonResponse<>(userDao.getById(id));
    }
}
