package com.doopp.gauss.game.handle;

import com.doopp.gauss.common.dao.UserDao;
import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.oauth.service.ManageService;
import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Inject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.ws.rs.Path;

@Slf4j
@Path("/manage/chat/ws")
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Inject
    private ManageService manageService;

    @Override
    public void onConnect(Channel channel) {
        super.allSendTextMessage("有新人加入哦");
        super.onConnect(channel);
        Mono<User> userMono = manageService.getUser(1L);
        userMono.subscribe(user->{
            super.allSendTextMessage("有新人" + user.getName() + "加入哦");
        });
    }

    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame("onTextMessage " + channel.id() + " : get " + frame.text()));
    }

    @Override
    public void close(Channel channel) {
        super.close(channel);
        super.allSendTextMessage("有人离开了哦");
    }
}
