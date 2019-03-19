package com.itheima.search.service;

import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import org.apache.solr.client.solrj.SolrServerException;

import java.util.List;

public interface IndexSearcherService {

    //根据给定的关键词查询solr索引数据
    public List<News> findByKeywords(ResultBean resultBean) throws SolrServerException, Exception;

    //分页查询
    public PageBean findByPageQuery(ResultBean resultBean) throws  Exception;

}
