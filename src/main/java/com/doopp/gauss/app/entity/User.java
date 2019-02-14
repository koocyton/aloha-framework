package com.doopp.gauss.app.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private Long id;

    private String name;

    public User(String name, Long id) {
        this.name = name;
        this.id = id;
    }

    public String toString() {
        return "{\"id\":" + this.id + ", \"name\":\"" + this.name + "\"}";
    }
}
