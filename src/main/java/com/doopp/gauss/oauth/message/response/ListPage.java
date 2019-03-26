package com.doopp.gauss.oauth.message.response;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class ListPage<T> {

    private Long total = 0L;
    private int pageSize = 30;
    private int currentPage = 1;
    private List<T> list;

    public ListPage(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        this.total = pageInfo.getTotal();
        this.pageSize = pageInfo.getPageSize();
        this.currentPage = pageInfo.getPageNum();
        this.list = list;
    }
}

