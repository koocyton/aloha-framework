package com.doopp.gauss.server.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private Long id;

    private String name;

    public String toString() {
        return "{\"id\":" + this.id + ", \"name\":\"" + this.name + "\"}";
    }
}
