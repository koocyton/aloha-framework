package com.doopp.gauss.common.dao;

import com.doopp.gauss.common.entity.Client;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClientDao {

    @Select("<script>" +
        "        SELECT\n" +
        "        *\n" +
        "        FROM\n" +
        "        `oauth_client`\n" +
        "        WHERE\n" +
        "        1\n" +
        "        ORDER BY `id` DESC" +
        "</script>")
    Client getById(@Param("id") Long id);

    @Select("<script>" +
        "SELECT * FROM `oauth_client` WHERE `id` = ${id} LIMIT 1" +
        "</script>")
    List<Client> getList();
}

