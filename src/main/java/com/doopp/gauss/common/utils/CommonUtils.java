package com.doopp.gauss.common.utils;

import reactor.netty.http.server.HttpServerRequest;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class CommonUtils {

    /**
     * Returns 是否是移动客户端
     *
     * @param request Http Servlet Request
     * @return true or false
     */
    public static boolean isMobileClient(HttpServerRequest request) {
        String ua = request.requestHeaders().get("User-Agent");
        String[] agent = {"Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser"};
        boolean flag = false;
        if (!ua.contains("Windows NT") || (ua.contains("Windows NT") && ua.contains("compatible; MSIE 9.0;"))) {
            // 排除 苹果桌面系统
            if (!ua.contains("Windows NT") && !ua.contains("Macintosh")) {
                for (String item : agent) {
                    if (ua.contains(item)) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * Returns 客户端的 IP
     *
     * @param request Http Servlet Request
     * @return true or false
     */
    public static String clientIp(HttpServerRequest request)
    {
        String ip = request.requestHeaders().get("X-Forwarded-For");
        //
        if (ip!=null && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.requestHeaders().get("X-Real-IP");
        if (ip!=null && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.remoteAddress().toString();
    }

    public static Long getMaxValueKey(final Map<Long, Integer> map) {
        if (map == null) return null;
        int ii = 0;
        Integer maxValue = 0;
        Long maxValueKey = null;
        for(Long key : map.keySet()) {
            maxValue = (maxValueKey==null) ? map.get(key) : 0;
            maxValueKey = (maxValueKey==null) ? key : null;
            if (map.get(key)>=maxValue) {
                maxValue = map.get(key);
                maxValueKey = key;
            }
        }
        return maxValueKey;
    }

    public static String simpleJsonGet(String url, Map<String, String> headers) {
        HttpURLConnection http = null;
        InputStream is = null;
        try {
            URL urlGet = new URL(url);
            http = (HttpURLConnection) urlGet.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
            http.setDoOutput(true);
            http.setDoInput(true);
            http.connect();
            is = http.getInputStream();
            int size = is.available();
            byte[] jsonBytes = new byte[size];
            is.read(jsonBytes);
            return new String(jsonBytes, "UTF-8");
        }
        catch (Exception e) {
            return null;
        }
    }


}
