package com.doopp.kreactor;

import java.util.HashMap;
import java.util.Map;

public class RequestAttribute {

    private Map<String, Object> attributes = new HashMap<>();

    public <T> void setAttribute(String key, T object) {
        this.attributes.put(key, object);
    }

    public <T> T getAttribute(String key, Class<T> clazz) {
        return clazz.cast(this.attributes.get(key));
    }
}
