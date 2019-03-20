package com.doopp.gauss.server.resource;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UploadFilesParam {

    String value();

    String types() default "";
}

