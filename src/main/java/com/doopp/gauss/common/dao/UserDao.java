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

    @Insert("INSERT INTO\n" +
        "          `oauth_user`\n" +
        "        SET\n" +
        "           `user_id` = ${user_id,jdbcType=BIGINT},\n" +
        "           `platform` = #{platform},\n" +
        "           `platform_id` = #{platform_id},\n" +
        "           `platform_token` = #{platform_token},\n" +
        "           `account` = #{account},\n" +
        "           `password` = #{password},\n" +
        "           `created_at` = #{created_at}")
    void create(User user);

    @Update("UPDATE\n" +
        "            `oauth_user`\n" +
        "        SET\n" +
        "            `platform` = #{platform},\n" +
        "            `platform_id` = #{platform_id},\n" +
        "            `platform_token` = #{platform_token}\n" +
        "        WHERE\n" +
        "            `id`=${id,jdbcType=BIGINT}")
    void update(User user);

    @Delete("DELETE FROM `oauth_user` WHERE `id`=#{id,jdbcType=BIGINT} LIMIT 1")
    void delete(Long id);
}

