package com.doopp.gauss.admin.handle;

import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.message.CommonResponse;
import com.google.inject.Inject;
import reactor.core.publisher.Mono;

import javax.ws.rs.*;
import javax.ws.rs.Path;

@Path("/admin/api")
public class ManageHandle {

    @Inject
    private UserDao userDao;

    @POST
    @Path("/post-test")
    public Mono<CommonResponse<User>> sessionManager(@FormParam("id") Long id) {
        return Mono.just(new CommonResponse<>(userDao.getById(id)));
    }
}
