package com.doopp.gauss.oauth.defined;

import com.doopp.gauss.server.resource.RequestAttribute;
import io.netty.util.AttributeKey;

public class CommonField {

    // 后台用的 session key & Cookie key
    public static String SESSION_KEY  = "se_id";

    public static String CURRENT_USER  = "current_user";

    public static String CURRENT_CHANNEL  = "current_channel";

    public static AttributeKey<RequestAttribute> REQUEST_ATTRIBUTE = AttributeKey.newInstance("request_attribute");
}
