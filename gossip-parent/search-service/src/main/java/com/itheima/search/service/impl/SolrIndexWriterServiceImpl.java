package com.itheima.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.gossip.pojo.News;
import com.itheima.search.service.IndexWriterService;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SolrIndexWriterServiceImpl implements IndexWriterService{
    @Autowired // 到spring容器中查找solrServer对象, 注入SolrServer
    private SolrServer cloudSolrServer;
    @Override
    public void saveBeans(List<News> list) throws  Exception{
        //CloudSolrServer
        cloudSolrServer.addBeans(list);
        cloudSolrServer.commit();
    }
}
