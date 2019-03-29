package com.doopp.kreactor;

import java.util.LinkedHashMap;

public class ModelMap extends LinkedHashMap<String, Object> {

    public void addAttribute(String attributeName, Object attributeValue) {
        this.put(attributeName, attributeValue);
    }
}
