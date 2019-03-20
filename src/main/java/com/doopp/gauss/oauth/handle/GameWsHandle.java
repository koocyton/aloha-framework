package com.doopp.gauss.oauth.handle;

import com.doopp.gauss.server.handle.AbstractWebSocketServerHandle;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;

@Slf4j
@Path("/manage/chat/ws")
@Singleton
public class GameWsHandle extends AbstractWebSocketServerHandle {

}
