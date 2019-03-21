package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.oauth.utils.HttpClientUtil;
import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.ws.rs.Path;
import java.nio.charset.Charset;
import java.util.HashMap;

@Slf4j
@Path("/manage/chat/ws")
@Singleton
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Inject
    private HttpClientUtil httpClientUtil;

    @Override
    public Mono<String> onTextMessage(TextWebSocketFrame frame, Channel channel) {
        return httpClientUtil.get("https://www.doopp.com", new HashMap<>())
                .map(byteBuf -> {
                    String resp = byteBuf.toString(Charset.forName("UTF-8"));
                    sendTextMessage(resp, channel);
                    return resp;
                });
    }
}
