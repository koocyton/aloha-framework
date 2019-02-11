package com.doopp.gauss.app.service;

import com.doopp.gauss.app.entity.User;

public interface UserService {

    User getUserById(Long id);

    User getUserByToken(String token);

    String createSessionToken(User user);

    void removeSessionToken(String token);

    void removeSessionToken(Long userId);

}
