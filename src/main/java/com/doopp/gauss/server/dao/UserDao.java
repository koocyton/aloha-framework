package com.doopp.gauss.server.dao;

import com.doopp.gauss.server.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

    @Select("select * from user limit 1")
    User getUser();
}
