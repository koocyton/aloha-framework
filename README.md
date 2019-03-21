# aloha-framework

Reactor + Netty + Guice + Mybatis + Jedis(Redisson) + Gson(Grpc) + Websocket

``` html
Reactor-netty
Guice ( Ioc AOP )
MyBatis
HikariCP
Gson ( Json )
Pagehelper ( 分页 )
kryo (序列化)
slf4j ( 日志 )
```

自动扫描 handle 注解，和类型自动注入
``` html
@Path 请求路径
@GET  HttpRequest Method
@POST HttpRequest Method
AbstractWebSocketServerHandle 继承此类，处理 websocket 事件
RequestAttribute 类型，以后补充，用户存放当前请求的数据
HttpServerRequest reactor-netty 的 request
HttpServerResponse reactor-netty 的 response
@CookieParam 类型用于 Cookie 获取
@HeaderParam 用于获取头
@QueryParam 获取 url 参数
@PathParam 获取 url 路径解析
@FormParam 获取表单
@BeanParam 将 request json body 转对象 
@UploadFilesParam 上传的文件
```

@POST ， @GET ， @PATH 例子
```java
    @GET
    @Path("/authentication")
    public Mono<Authentication> authentication() {
        Authentication authentication = new Authentication(
                applicationProperties.l("admin.client.id"),
                applicationProperties.s("admin.client.secret")
        );
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
    
    @POST
    @Path("/auto-login")
    public Mono<SessionToken> autoLogin(@BeanParam OAuthRequest<LoginRequest> commonRequest) {
        LoginRequest loginRequest = commonRequest.getData();
        return oauthService
            .userAutoLogin(loginRequest.getAccount())
            .map(user ->new SessionToken(oauthService.createSessionToken(user)));
    }
```

Websocket 类
```java
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
```
