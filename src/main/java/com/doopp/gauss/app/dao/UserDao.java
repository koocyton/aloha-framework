package com.doopp.gauss.app.dao;

import com.doopp.gauss.app.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserDao {

    @Select("<script>" +
        "SELECT *" +
        "FROM oauth_user " +
        "Limit 1 "+
        "</script>")
    User getById();
}
