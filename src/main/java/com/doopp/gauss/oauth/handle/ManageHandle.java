package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.oauth.entity.Client;
import com.doopp.gauss.oauth.message.response.ListPage;
import com.doopp.gauss.oauth.service.ManageService;
import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.entity.vo.UserVO;
import com.doopp.gauss.oauth.message.response.Authentication;
import com.doopp.gauss.server.resource.RequestAttributeParam;
import com.github.pagehelper.PageHelper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.ws.rs.*;

@Slf4j
@Path("/manage/api")
public class ManageHandle {

    @Inject
    private ManageService manageService;

    @Inject
    @Named("admin.client.id")
    private Long clientId;

    @Inject
    @Named("admin.client.secret")
    private String clientSecret;

    @GET
    @Path("/authentication")
    public Mono<Authentication> authentication() {
        Authentication authentication = new Authentication(clientId, clientSecret);
        return Mono.just(authentication);
    }

    @GET
    @Path("/manager")
    public Mono<UserVO> currentManager(@RequestAttributeParam("current_user") UserVO user) {
        return Mono.just(user);
    }

    @GET
    @Path("/users")
    public Mono<ListPage<User>> users(@QueryParam("page") Integer page) {
        PageHelper.startPage(page, 30);
        return manageService.getUsers()
            .map(list->new ListPage<>(list, User.class));
    }

    @GET
    @Path("/apps")
    public Mono<ListPage<Client>> apps(@QueryParam("page") Integer page) {
        PageHelper.startPage(page, 30);
        return manageService.getClients()
            .map(list->new ListPage<>(list, Client.class));
    }
}
