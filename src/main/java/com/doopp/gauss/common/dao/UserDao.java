package com.doopp.gauss.common.dao;

import com.doopp.gauss.common.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserDao {

    @Select("SELECT * FROM `oauth_user` WHERE 1 ORDER BY `id` DESC")
    List<User> getList();

    @Select("SELECT count(*) FROM `oauth_user`")
    int count(Long id);

    @Select("SELECT * FROM `oauth_user` WHERE `id` = #{id,jdbcType=BIGINT} LIMIT 1")
    User getById(Long id);

    @Select("SELECT * FROM `oauth_user` WHERE `account` = #{account,jdbcType=VARCHAR} LIMIT 1")
    User getByAccount(String account);

    @Insert("INSERT INTO `oauth_user` SET `id` = ${id}, `name` = #{account}, `account` = #{account}, `password` = #{password}")
    void create(User user);
}

