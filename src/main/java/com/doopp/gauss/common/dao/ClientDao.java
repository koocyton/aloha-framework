package com.doopp.gauss.common.dao;

import com.doopp.gauss.common.entity.Client;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClientDao {

    @Select("SELECT * FROM `oauth_client` WHERE 1 ORDER BY `id` DESC")
    List<Client> getList();














































    @Select("<script>" +
        "SELECT * FROM `oauth_client` WHERE `id` = ${id} LIMIT 1" +
        "</script>")
    Client getById(@Param("id") Long id);
}

