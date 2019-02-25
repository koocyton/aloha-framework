# aloha-framework

Reactor + Netty + Guice + Mybatis + Jedis(Redisson) + Gson(Grpc) + Websocket

``` html
Reactor-netty
Guice ( Ioc AOP )
Netty ( Server )
MyBatis ( Database )
HikariCP
Gson ( json 序列化 )
slf4j ( 日志 )
```

自动扫描 handle 注解，和类型自动注入
``` html
@Path 请求路径
@GET  HttpRequest Method
@POST HttpRequest Method
WebSocketServerHandle 类用于处理 websocket
RequestAttribute 类型，以后补充，用户存放当前请求的数据
HttpServerRequest reactor-netty 的 request
HttpServerResponse reactor-netty 的 response
@CookieParam 类型用于 Cookie 获取
@HeaderParam 用于获取头
@QueryParam 获取 url 参数
@PathParam 获取 url 路径解析
@FormParam 获取表单
@BeanParam 将 request json body 转对象 
```

@POST ， @GET ， @PATH 例子
```java
    @GET
    @Path("/authentication")
    public CommonResponse<Authentication> authentication() {
        return CommonResponse.just(authentication);
    }

    @GET
    @Path("/manager")
    public CommonResponse<User> sessionManager() {
        return CommonResponse.just(userDao.getById(1L));
    }
```

Websocket 类
```java
@Path("/game")
@Singleton
public class GameWsHandle extends AbstractWebSocketServerHandle {

    @Override
    public void onConnect(Channel channel) {
        // 连接成功
    }
    
    @Override
    public void onTextMessage(TextWebSocketFrame frame, Channel channel) {
        // 收到消息 frame
    }

    @Override
    public void close(Channel channel) {
        // 关闭连接
        super.close(channel);
    }
}

```

websocket 解析方式有几个，下面这个可能也是可行的。而且结构更清晰，只是后面哪个 blockLast 感觉好怪
实际测试，并没有阻塞，线程也相对稳定
```java
(in, out) -> out.withConnection(c -> {
            // on connect
            handleObject.onConnect(c.channel());
            // get message
              in.aggregateFrames()
                  .receiveFrames()
                  .doOnNext(frame -> {
                      if (frame instanceof TextWebSocketFrame) {
                          handleObject.onTextMessage((TextWebSocketFrame) frame, c.channel());
                      } else if (frame instanceof BinaryWebSocketFrame) {
                          handleObject.onBinaryMessage((BinaryWebSocketFrame) frame, c.channel());
                      } else if (frame instanceof PingWebSocketFrame) {
                          handleObject.onPingMessage((PingWebSocketFrame) frame, c.channel());
                      } else if (frame instanceof PongWebSocketFrame) {
                          handleObject.onPongMessage((PongWebSocketFrame) frame, c.channel());
                      } else if (frame instanceof CloseWebSocketFrame) {
                          handleObject.close((CloseWebSocketFrame) frame, c.channel());
                      }
                   })
                   .blockLast();
        }).then();
```