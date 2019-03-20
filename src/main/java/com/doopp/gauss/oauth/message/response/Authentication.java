package com.doopp.gauss.oauth.message.response;

import com.doopp.gauss.oauth.utils.EncryHelper;
import lombok.Data;

@Data
public class Authentication {

    private int time;

    private String client;

    private String security;

    public Authentication(Long client, String security) {
        int time = (int) (System.currentTimeMillis() / 1000);
        this.setTime(time);
        this.setClient(String.valueOf(client));
        this.setSecurity(EncryHelper.md5(time + "_" + client + "_" + security));
    }
}

