package com.itheima.gossip.mapper;

import com.itheima.gossip.pojo.News;

import java.util.List;

public interface NewsMapper {

    //1. 获取数据
    public List<News>  findByNextMaxId(Integer nextMaxId);

    //2. 获取最大值
    public Integer getNextMaxId(Integer nextMaxId);
}
