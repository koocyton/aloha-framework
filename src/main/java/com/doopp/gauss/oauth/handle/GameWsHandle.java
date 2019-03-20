package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.oauth.defined.CommonField;
import com.doopp.gauss.oauth.entity.User;
import com.doopp.gauss.oauth.entity.vo.UserVO;
import com.doopp.gauss.oauth.service.ManageService;
import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.doopp.gauss.server.resource.RequestAttribute;
import com.google.inject.Inject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Path("/manage/chat/ws")
public class GameWsHandle extends AbstractWebSocketServerHandle {
//
//    @Inject
//    private ManageService manageService;
//
//    private FluxProcessor<String, String> queueMessage = ReplayProcessor.create();
//
//    private Map<String, FluxProcessor<String, String>> queueMessageMap = new HashMap<>();
//
//    @Override
//    public void onConnect(Channel channel) {
//        RequestAttribute requestAttribute = channel.attr(CommonField.REQUEST_ATTRIBUTE).get();
//        UserVO currentUser = requestAttribute.getAttribute(CommonField.CURRENT_USER, UserVO.class);
//        // super.allSendTextMessage("有新人加入哦");
//        super.onConnect(channel);
//        Mono<User> userMono = manageService.getUser(currentUser.getId());
//        userMono.subscribe(user->{
//            super.allSendTextMessage(user.getName() + " 加入");
//        });
//    }
//
//    public Flux<String> receiveTextMessage(Channel channel) {
//        return queueMessage;
//    }
//
//    @Override
//    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
//        Flux.just(channel.id() + " : " + frame.text())
//                .map(Object::toString)
//                .subscribe(queueMessage::onNext);
//        // channel.writeAndFlush(new TextWebSocketFrame("onTextMessage " + channel.id() + " : get " + frame.text()));
//    }
//
//    @Override
//    public void disconnect(Channel channel) {
//        super.disconnect(channel);
//        super.allSendTextMessage("有人离开了哦");
//    }
}
