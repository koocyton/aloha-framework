package com.doopp.gauss.server.netty;

import com.doopp.gauss.common.exception.CommonException;
import com.doopp.gauss.server.message.CommonResponse;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@Slf4j
public class StaticHandle {

//    private final HashMap<String, iFilter> filters = new HashMap<>();
//
//    private Injector injector;
//
//    public void setInjector(Injector injector) {
//        this.injector = injector;
//    }
//
//    public void addFilter(String path, Class<? extends iFilter> filterClass) {
//        this.filters.put(path, injector.getInstance(filterClass));
//    }
//
//    private boolean doFilter(HttpServerRequest request, HttpServerResponse response) {
//        String uri = URI.create(request.uri()).getPath();
//        for(String key : this.filters.keySet()) {
//            int keyLength = key.length();
//            if (uri.substring(0, keyLength).equals(key)) {
//                 return this.filters.get(key).doFilter(request, response);
//            }
//        }
//        return true;
//    }
//
//    public <T> NettyOutbound sendWs(WebsocketInbound in, WebsocketOutbound out, Function<Injector, T> function) {
//        return out.options(NettyPipeline.SendOptions::flushOnEach)
//                .sendString(in
//                        .receiveFrames()
//                        .map(frame -> {
//                            if (frame instanceof TextWebSocketFrame) {
//                                return new GsonBuilder().create().toJson(function.apply(injector));
//                            }
//                            return "\n";
//                        })
//                );
//    }
//
//    public <T> NettyOutbound sendJson(HttpServerRequest req, HttpServerResponse resp, Function<Injector, T> function) {
//        if (!this.doFilter(req, resp)) {
//            return this.sendJsonException(resp, new CommonException(CommonError.WRONG_SESSION));
//        }
//        String json = new GsonBuilder().create().toJson(function.apply(injector));
//        return resp
//                .status(HttpResponseStatus.OK)
//                .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
//                .sendString(Mono.just(json));
//    }

    public NettyOutbound sendStatic(HttpServerRequest req, HttpServerResponse resp) {
//        if (!this.doFilter(req, resp)) {
//            return this.sendNotFoundPage(resp, new CommonException(CommonError.WRONG_SESSION));
//        }
        String requestUri = (req.uri().equals("/") || req.uri().equals("")) ? "/index.html" : req.uri();
        String requirePath = "/public" + requestUri;

        URL fileUrl = this.getClass().getResource(requirePath);
        if (fileUrl == null) {
            return resp.status(HttpResponseStatus.NOT_FOUND);
        }

        if (!fileUrl.toString().contains(".jar!")) {
            try {
                Path filePath = Paths.get(fileUrl.toURI());
                return resp
                        .header(HttpHeaderNames.CONTENT_TYPE, contentType(requirePath.substring(requirePath.lastIndexOf(".") + 1)))
                        .sendFile(filePath);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        try (InputStream fileIs = this.getClass().getResourceAsStream(requirePath)) {
            if (fileIs == null) {
                return resp.status(HttpResponseStatus.NOT_FOUND);
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] bs = new byte[1024];
            int len;
            while ((len = fileIs.read(bs)) != -1) {
                bout.write(bs, 0, len);
            }
            ByteBuf buf = Unpooled.wrappedBuffer(bout.toByteArray()).retain();
            return resp
                    .header(HttpHeaderNames.CONTENT_TYPE, contentType(requirePath.substring(requirePath.lastIndexOf(".") + 1)))
                    .send(ByteBufMono.just(buf));
        } catch (IOException ue) {
            throw new RuntimeException(ue);
        }
    }

    public NettyOutbound sendJsonException(HttpServerResponse resp, CommonException commonException) {
        CommonResponse<Object> commonResponse = new CommonResponse<>(null);
        commonResponse.setErr_code(commonException.getCode());
        commonResponse.setErr_msg(commonException.getMessage());
        String monoJson = new Gson().toJson(commonResponse);
        return resp
                .status(commonException.getCode())
                .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .sendString(Mono.just(monoJson));
    }

    public NettyOutbound sendNotFoundPage(HttpServerResponse resp, CommonException commonException) {
        return resp
            .status(HttpResponseStatus.NOT_FOUND)
            .header(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
            .sendString(Mono.just(commonException.getMessage()));
    }

    public NettyOutbound sendRedirect(HttpServerResponse resp, String redirect) {
        return resp
            .status(HttpResponseStatus.PERMANENT_REDIRECT)
            .sendString(Mono.just(""));
    }

    private String contentType(String fileExt) {
        String mime = fileExt2Mimes.get(fileExt);
        return mime != null ? mime : "text/plain";
    }

    private static final HashMap<String, String> fileExt2Mimes = new HashMap<>(256);

    static {
        fileExt2Mimes.put("", "application/octet-stream");
        fileExt2Mimes.put("323", "text/h323");
        fileExt2Mimes.put("acx", "application/internet-property-stream");
        fileExt2Mimes.put("ai", "application/postscript");
        fileExt2Mimes.put("aif", "audio/x-aiff");
        fileExt2Mimes.put("aifc", "audio/x-aiff");
        fileExt2Mimes.put("aiff", "audio/x-aiff");
        fileExt2Mimes.put("asf", "video/x-ms-asf");
        fileExt2Mimes.put("asr", "video/x-ms-asf");
        fileExt2Mimes.put("asx", "video/x-ms-asf");
        fileExt2Mimes.put("au", "audio/basic");
        fileExt2Mimes.put("avi", "video/x-msvideo");
        fileExt2Mimes.put("axs", "application/olescript");
        fileExt2Mimes.put("bas", "text/plain");
        fileExt2Mimes.put("bcpio", "application/x-bcpio");
        fileExt2Mimes.put("bin", "application/octet-stream");
        fileExt2Mimes.put("bmp", "image/bmp");
        fileExt2Mimes.put("c", "text/plain");
        fileExt2Mimes.put("cat", "application/vnd.ms-pkiseccat");
        fileExt2Mimes.put("cdf", "application/x-cdf");
        fileExt2Mimes.put("cer", "application/x-x509-ca-cert");
        fileExt2Mimes.put("class", "application/octet-stream");
        fileExt2Mimes.put("clp", "application/x-msclip");
        fileExt2Mimes.put("cmx", "image/x-cmx");
        fileExt2Mimes.put("cod", "image/cis-cod");
        fileExt2Mimes.put("cpio", "application/x-cpio");
        fileExt2Mimes.put("crd", "application/x-mscardfile");
        fileExt2Mimes.put("crl", "application/pkix-crl");
        fileExt2Mimes.put("crt", "application/x-x509-ca-cert");
        fileExt2Mimes.put("csh", "application/x-csh");
        fileExt2Mimes.put("css", "text/css");
        fileExt2Mimes.put("dcr", "application/x-director");
        fileExt2Mimes.put("der", "application/x-x509-ca-cert");
        fileExt2Mimes.put("dir", "application/x-director");
        fileExt2Mimes.put("dll", "application/x-msdownload");
        fileExt2Mimes.put("dms", "application/octet-stream");
        fileExt2Mimes.put("doc", "application/msword");
        fileExt2Mimes.put("dot", "application/msword");
        fileExt2Mimes.put("dvi", "application/x-dvi");
        fileExt2Mimes.put("dxr", "application/x-director");
        fileExt2Mimes.put("eps", "application/postscript");
        fileExt2Mimes.put("etx", "text/x-setext");
        fileExt2Mimes.put("evy", "application/envoy");
        fileExt2Mimes.put("exe", "application/octet-stream");
        fileExt2Mimes.put("fif", "application/fractals");
        fileExt2Mimes.put("flr", "x-world/x-vrml");
        fileExt2Mimes.put("gif", "image/gif");
        fileExt2Mimes.put("gtar", "application/x-gtar");
        fileExt2Mimes.put("gz", "application/x-gzip");
        fileExt2Mimes.put("h", "text/plain");
        fileExt2Mimes.put("hdf", "application/x-hdf");
        fileExt2Mimes.put("hlp", "application/winhlp");
        fileExt2Mimes.put("hqx", "application/mac-binhex40");
        fileExt2Mimes.put("hta", "application/hta");
        fileExt2Mimes.put("htc", "text/x-component");
        fileExt2Mimes.put("htm", "text/html");
        fileExt2Mimes.put("html", "text/html");
        fileExt2Mimes.put("htt", "text/webviewhtml");
        fileExt2Mimes.put("ico", "image/x-icon");
        fileExt2Mimes.put("ief", "image/ief");
        fileExt2Mimes.put("iii", "application/x-iphone");
        fileExt2Mimes.put("ins", "application/x-internet-signup");
        fileExt2Mimes.put("isp", "application/x-internet-signup");
        fileExt2Mimes.put("jfif", "image/pipeg");
        fileExt2Mimes.put("jpe", "image/jpeg");
        fileExt2Mimes.put("jpeg", "image/jpeg");
        fileExt2Mimes.put("png", "image/png");// -- new added
        fileExt2Mimes.put("jpg", "image/jpeg");
        fileExt2Mimes.put("js", "application/x-javascript");
        fileExt2Mimes.put("latex", "application/x-latex");
        fileExt2Mimes.put("lha", "application/octet-stream");
        fileExt2Mimes.put("lsf", "video/x-la-asf");
        fileExt2Mimes.put("lsx", "video/x-la-asf");
        fileExt2Mimes.put("lzh", "application/octet-stream");
        fileExt2Mimes.put("m13", "application/x-msmediaview");
        fileExt2Mimes.put("m14", "application/x-msmediaview");
        fileExt2Mimes.put("m3u", "audio/x-mpegurl");
        fileExt2Mimes.put("man", "application/x-troff-man");
        fileExt2Mimes.put("mdb", "application/x-msaccess");
        fileExt2Mimes.put("me", "application/x-troff-me");
        fileExt2Mimes.put("mht", "message/rfc822");
        fileExt2Mimes.put("mhtml", "message/rfc822");
        fileExt2Mimes.put("mid", "audio/mid");
        fileExt2Mimes.put("mny", "application/x-msmoney");
        fileExt2Mimes.put("mov", "video/quicktime");
        fileExt2Mimes.put("movie", "video/x-sgi-movie");
        fileExt2Mimes.put("mp2", "video/mpeg");
        fileExt2Mimes.put("mp3", "audio/mpeg");
        fileExt2Mimes.put("mpa", "video/mpeg");
        fileExt2Mimes.put("mpe", "video/mpeg");
        fileExt2Mimes.put("mpeg", "video/mpeg");
        fileExt2Mimes.put("mpg", "video/mpeg");
        fileExt2Mimes.put("mpp", "application/vnd.ms-project");
        fileExt2Mimes.put("mpv2", "video/mpeg");
        fileExt2Mimes.put("ms", "application/x-troff-ms");
        fileExt2Mimes.put("mvb", "application/x-msmediaview");
        fileExt2Mimes.put("nws", "message/rfc822");
        fileExt2Mimes.put("oda", "application/oda");
        fileExt2Mimes.put("p10", "application/pkcs10");
        fileExt2Mimes.put("p12", "application/x-pkcs12");
        fileExt2Mimes.put("p7b", "application/x-pkcs7-certificates");
        fileExt2Mimes.put("p7c", "application/x-pkcs7-mime");
        fileExt2Mimes.put("p7m", "application/x-pkcs7-mime");
        fileExt2Mimes.put("p7r", "application/x-pkcs7-certreqresp");
        fileExt2Mimes.put("p7s", "application/x-pkcs7-signature");
        fileExt2Mimes.put("pbm", "image/x-portable-bitmap");
        fileExt2Mimes.put("pdf", "application/pdf");
        fileExt2Mimes.put("pfx", "application/x-pkcs12");
        fileExt2Mimes.put("pgm", "image/x-portable-graymap");
        fileExt2Mimes.put("pko", "application/ynd.ms-pkipko");
        fileExt2Mimes.put("pma", "application/x-perfmon");
        fileExt2Mimes.put("pmc", "application/x-perfmon");
        fileExt2Mimes.put("pml", "application/x-perfmon");
        fileExt2Mimes.put("pmr", "application/x-perfmon");
        fileExt2Mimes.put("pmw", "application/x-perfmon");
        fileExt2Mimes.put("pnm", "image/x-portable-anymap");
        fileExt2Mimes.put("pot,", "application/vnd.ms-powerpoint");
        fileExt2Mimes.put("ppm", "image/x-portable-pixmap");
        fileExt2Mimes.put("pps", "application/vnd.ms-powerpoint");
        fileExt2Mimes.put("ppt", "application/vnd.ms-powerpoint");
        fileExt2Mimes.put("prf", "application/pics-rules");
        fileExt2Mimes.put("ps", "application/postscript");
        fileExt2Mimes.put("pub", "application/x-mspublisher");
        fileExt2Mimes.put("qt", "video/quicktime");
        fileExt2Mimes.put("ra", "audio/x-pn-realaudio");
        fileExt2Mimes.put("ram", "audio/x-pn-realaudio");
        fileExt2Mimes.put("ras", "image/x-cmu-raster");
        fileExt2Mimes.put("rgb", "image/x-rgb");
        fileExt2Mimes.put("rmi", "audio/mid");
        fileExt2Mimes.put("roff", "application/x-troff");
        fileExt2Mimes.put("rtf", "application/rtf");
        fileExt2Mimes.put("rtx", "text/richtext");
        fileExt2Mimes.put("scd", "application/x-msschedule");
        fileExt2Mimes.put("sct", "text/scriptlet");
        fileExt2Mimes.put("setpay", "application/set-payment-initiation");
        fileExt2Mimes.put("setreg", "application/set-registration-initiation");
        fileExt2Mimes.put("sh", "application/x-sh");
        fileExt2Mimes.put("shar", "application/x-shar");
        fileExt2Mimes.put("sit", "application/x-stuffit");
        fileExt2Mimes.put("snd", "audio/basic");
        fileExt2Mimes.put("spc", "application/x-pkcs7-certificates");
        fileExt2Mimes.put("spl", "application/futuresplash");
        fileExt2Mimes.put("src", "application/x-wais-source");
        fileExt2Mimes.put("sst", "application/vnd.ms-pkicertstore");
        fileExt2Mimes.put("stl", "application/vnd.ms-pkistl");
        fileExt2Mimes.put("stm", "text/html");
        fileExt2Mimes.put("svg", "image/svg+xml");
        fileExt2Mimes.put("sv4cpio", "application/x-sv4cpio");
        fileExt2Mimes.put("sv4crc", "application/x-sv4crc");
        fileExt2Mimes.put("swf", "application/x-shockwave-flash");
        fileExt2Mimes.put("t", "application/x-troff");
        fileExt2Mimes.put("tar", "application/x-tar");
        fileExt2Mimes.put("tcl", "application/x-tcl");
        fileExt2Mimes.put("tex", "application/x-tex");
        fileExt2Mimes.put("texi", "application/x-texinfo");
        fileExt2Mimes.put("texinfo", "application/x-texinfo");
        fileExt2Mimes.put("tgz", "application/x-compressed");
        fileExt2Mimes.put("tif", "image/tiff");
        fileExt2Mimes.put("tiff", "image/tiff");
        fileExt2Mimes.put("tr", "application/x-troff");
        fileExt2Mimes.put("trm", "application/x-msterminal");
        fileExt2Mimes.put("tsv", "text/tab-separated-values");
        fileExt2Mimes.put("txt", "text/plain");
        fileExt2Mimes.put("uls", "text/iuls");
        fileExt2Mimes.put("ustar", "application/x-ustar");
        fileExt2Mimes.put("vcf", "text/x-vcard");
        fileExt2Mimes.put("vrml", "x-world/x-vrml");
        fileExt2Mimes.put("wav", "audio/x-wav");
        fileExt2Mimes.put("wcm", "application/vnd.ms-works");
        fileExt2Mimes.put("wdb", "application/vnd.ms-works");
        fileExt2Mimes.put("wks", "application/vnd.ms-works");
        fileExt2Mimes.put("wmf", "application/x-msmetafile");
        fileExt2Mimes.put("wps", "application/vnd.ms-works");
        fileExt2Mimes.put("wri", "application/x-mswrite");
        fileExt2Mimes.put("wrl", "x-world/x-vrml");
        fileExt2Mimes.put("wrz", "x-world/x-vrml");
        fileExt2Mimes.put("xaf", "x-world/x-vrml");
        fileExt2Mimes.put("xbm", "image/x-xbitmap");
        fileExt2Mimes.put("xla", "application/vnd.ms-excel");
        fileExt2Mimes.put("xlc", "application/vnd.ms-excel");
        fileExt2Mimes.put("xlm", "application/vnd.ms-excel");
        fileExt2Mimes.put("xls", "application/vnd.ms-excel");
        fileExt2Mimes.put("xlt", "application/vnd.ms-excel");
        fileExt2Mimes.put("xlw", "application/vnd.ms-excel");
        fileExt2Mimes.put("xof", "x-world/x-vrml");
        fileExt2Mimes.put("xpm", "image/x-xpixmap");
        fileExt2Mimes.put("xwd", "image/x-xwindowdump");
        fileExt2Mimes.put("z", "application/x-compress");
        fileExt2Mimes.put("zip", "application/zip");
    }
}
