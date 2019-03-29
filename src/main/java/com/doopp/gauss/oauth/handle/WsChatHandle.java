package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.oauth.defined.ChatAction;
import com.doopp.gauss.oauth.defined.CommonField;
import com.doopp.gauss.oauth.entity.vo.UserVO;
import com.doopp.gauss.oauth.utils.HttpClientUtil;
import com.doopp.kreactor.AbstractWebSocketServerHandle;
import com.doopp.kreactor.RequestAttribute;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.ws.rs.Path;

@Slf4j
@Path("/manage/chat/ws")
@Singleton
public class WsChatHandle extends AbstractWebSocketServerHandle {

    private static AttributeKey<RequestAttribute> REQUEST_ATTRIBUTE = AttributeKey.newInstance("request_attribute");

    @Inject
    private HttpClientUtil httpClientUtil;

    @Inject
    private Gson gson;

    private String sendAction(ChatAction action, UserVO user, String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAction(action);
        chatMessage.setName(user.getName());
        chatMessage.setMessage(message);
        return gson.toJson(chatMessage);
    }

    @Override
    public void connected(Channel channel) {
        RequestAttribute requestAttribute = channel.attr(REQUEST_ATTRIBUTE).get();
        UserVO userVO = requestAttribute.getAttribute(CommonField.CURRENT_USER, UserVO.class);
        super.connected(channel, String.valueOf(userVO.getId()));
        this.userJoinLeave("join", channel);
    }

    @Override
    public Mono<String> onTextMessage(TextWebSocketFrame frame, Channel channel) {
        return Mono.just(frame.text()).map(s -> {
            RequestAttribute requestAttribute = channel.attr(REQUEST_ATTRIBUTE).get();
            UserVO userVO = requestAttribute.getAttribute(CommonField.CURRENT_USER, UserVO.class);
            for(Channel mapChannel : super.getChannelMap().values()) {
                sendTextMessage(sendAction(ChatAction.CHAT, userVO, s), mapChannel);
            }
            return "";
        });
        //  return httpClientUtil.get("https://www.doopp.com", new HashMap<>())
        //          .map(byteBuf -> {
        //              String resp = byteBuf.toString(Charset.forName("UTF-8"));
        //              sendTextMessage(resp, channel);
        //              return resp;
        //          });
    }

    @Override
    public void disconnect(Channel channel) {
        userJoinLeave("leave", channel);
        super.disconnect(channel);
    }

    @Data
    private static class ChatMessage {
        private ChatAction action;
        private String name;
        private String message;
    }

    private void userJoinLeave(String joinLeave, Channel channel) {
        StringBuilder userList = new StringBuilder();
        for(Channel mapChannel : super.getChannelMap().values()) {
            if (mapChannel.attr(REQUEST_ATTRIBUTE)!=null) {
                userList.append(userList.toString().equals("") ? "" : ",");
                userList.append(mapChannel
                    .attr(REQUEST_ATTRIBUTE)
                    .get()
                    .getAttribute(CommonField.CURRENT_USER, UserVO.class)
                    .getName());
            }
        }

        if (channel!=null) {
            RequestAttribute requestAttribute = channel.attr(REQUEST_ATTRIBUTE).get();
            UserVO userVO = requestAttribute.getAttribute(CommonField.CURRENT_USER, UserVO.class);

            for(Channel mapChannel : super.getChannelMap().values()) {
                if (mapChannel.isActive()) {
                    if (joinLeave.equals("join")) {
                        sendTextMessage(sendAction(ChatAction.JOIN, userVO, "昂首阔步踏入江湖"), mapChannel);
                    } else if (joinLeave.equals("leave")) {
                        sendTextMessage(sendAction(ChatAction.JOIN, userVO, "挥一挥衣袖，不带走一两银子 ..."), mapChannel);
                    }
                    sendTextMessage(sendAction(ChatAction.USER_LIST, userVO, userList.toString()), mapChannel);
                }
            }
        }
    }
}
