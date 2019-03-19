package com.itheima.search.service;

import com.itheima.gossip.pojo.News;

import java.util.List;

public interface IndexWriterService {

    //写入索引的方法
    public void saveBeans(List<News> list) throws  Exception;

}
