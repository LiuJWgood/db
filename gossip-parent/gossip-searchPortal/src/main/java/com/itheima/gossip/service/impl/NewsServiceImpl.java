package com.itheima.gossip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.gossip.mapper.NewsMapper;
import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.gossip.service.NewsService;
import com.itheima.search.service.IndexSearcherService;
import com.itheima.search.service.IndexWriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class NewsServiceImpl implements NewsService {
    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private JedisPool jedisPool;
    @Reference(timeout = 10*1000)
    private IndexWriterService indexWriterService;
    @Reference(timeout = 10*1000)
    private IndexSearcherService indexSearcherService;

    @Override
    public PageBean findByPageQuery(ResultBean resultBean) throws Exception {
        //1. 调用solr服务当中分页的方法, 获取结果数据
        PageBean pageBean = indexSearcherService.findByPageQuery(resultBean);
        //2. 结果数据进行处理: 详情数据(70字)
        for (News news : pageBean.getNewsList()) {
            String content = news.getContent();

            if(content.length()>100){
                content = content.substring(0,99)+"...";
                news.setContent(content);
            }
        }
        //3 返回给controller层
        return pageBean;
    }

    //根据关键词查询索引数据
    @Override
    public List<News> findByKeywords(ResultBean resultBean) throws Exception {
        //1. 调用solr服务, 获取结果数据
        List<News> newsList = indexSearcherService.findByKeywords(resultBean);
        //2. 结果数据进行处理: 详情数据(70字)
        for (News news : newsList) {
            String content = news.getContent();

            if(content.length()>70){
                content = content.substring(0,69)+"...";
                news.setContent(content);
            }
        }
        //3 返回给controller层
        return newsList;
    }


    @Override
    public void indexWriter() throws Exception {
        //1. 从redis中获取上一次的最大值id
        Jedis jedis = jedisPool.getResource();
        String nextMaxIdStr = jedis.get("bigData:gossip:nextMaxId");
        jedis.close();
        if (nextMaxIdStr == null || "".equals(nextMaxIdStr)) {
            nextMaxIdStr = "0";
        }
        int nextMaxId = Integer.parseInt(nextMaxIdStr);
        while (true) {

            List<News> newsList = newsMapper.findByNextMaxId(nextMaxId);
            if (newsList == null || newsList.size() == 0) {
                //无法获取到数据
                jedis = jedisPool.getResource();
                jedis.set("bigData:gossip:nextMaxId",nextMaxId+"");
                jedis.close();
                System.out.println("数据获取完成. 最大id值为:" + nextMaxId);
                break;
            }
            System.out.println("获取到了:"+newsList.size());
            //2. 执行相关的操作:日期的转换
            //2018-12-06 14:41:42 :
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            for (News news : newsList) {
                String oldTime = news.getTime();

                Date oldDate = format1.parse(oldTime);

                String newTime = format2.format(oldDate);
                news.setTime(newTime);
            }

            //3. 调用solr服务写入索引

            indexWriterService.saveBeans(newsList);

            //4. 获取最大值
            nextMaxId = newsMapper.getNextMaxId(nextMaxId);
        }
    }


}
