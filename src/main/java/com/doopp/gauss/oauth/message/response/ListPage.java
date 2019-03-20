package com.doopp.gauss.oauth.message.response;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class ListPage<T> {

    private Long total = 0L;
    private int pageSize = 30;
    private int currentPage = 1;
    private List<T> list;

    public <K> ListPage(List<K> list, Class<T> clazz) {
        PageInfo<K> pageInfo = new PageInfo<>(list);
        this.total = pageInfo.getTotal();
        this.pageSize = pageInfo.getPageSize();
        this.currentPage = pageInfo.getPageNum();
        this.list = (List<T>)list;
    }

    public ListPage(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        this.total = pageInfo.getTotal();
        this.pageSize = pageInfo.getPageSize();
        this.currentPage = pageInfo.getPageNum();
        this.list = list;
    }
}

