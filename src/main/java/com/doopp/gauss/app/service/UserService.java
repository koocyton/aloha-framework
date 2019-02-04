package com.doopp.gauss.app.service;

import com.doopp.gauss.app.entity.User;

public interface UserService {

    User getUserByToken(String token);

}
