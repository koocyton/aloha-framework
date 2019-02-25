package com.doopp.gauss.admin.handle;

import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.defined.CommonError;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.exception.CommonException;
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
    public CommonResponse<User> sessionManager(@FormParam("id") Long id) throws CommonException {
        User user = userDao.getById(id);
        if (user==null) {
            throw new CommonException(CommonError.ACCOUNT_NO_EXIST);
        }
        return CommonResponse.just(user);
    }
}
