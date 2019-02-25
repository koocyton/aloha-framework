package com.doopp.gauss.admin.service;

import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.exception.CommonException;

import java.util.concurrent.ExecutionException;

public interface ManagerService {

    User getManagerByToken(String token) throws InterruptedException, ExecutionException, CommonException;
}
