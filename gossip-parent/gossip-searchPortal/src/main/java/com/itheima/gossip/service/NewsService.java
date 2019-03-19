package com.itheima.gossip.service;

import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;

import java.util.List;

public interface NewsService {

    //写入索引的方法
    public void indexWriter() throws Exception;

    //根据关键字查询索引的方法
    public List<News> findByKeywords(ResultBean resultBean) throws  Exception;

    //分页查询
    public PageBean findByPageQuery(ResultBean resultBean) throws  Exception;
}
