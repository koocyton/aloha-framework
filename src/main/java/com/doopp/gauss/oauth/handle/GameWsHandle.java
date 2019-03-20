package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.oauth.utils.HttpClientUtil;
import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;
import java.nio.charset.Charset;

@Slf4j
@Path("/manage/chat/ws")
@Singleton
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Inject
    private HttpClientUtil httpClientUtil;

    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        httpClientUtil.get("https://www.doopp.com", null)
                .map(byteBuf -> {
                    log.info(byteBuf.toString());
                    sendTextMessage(byteBuf.toString(), channel);
                    return "";
                })
                .subscribe();
    }
}
