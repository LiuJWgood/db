package com.itheima.page.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author itheima
 * @Title: PageKafkaConsumer
 * @ProjectName gossip-parent
 * @Description: 热搜词消息的监听类
 * @date 2018/12/1916:04
 */
@Component
public class PageKafkaConsumer implements MessageListener<Integer, String>{

    //热词缓存服务
    @Autowired
    private SearchPageService searchPageService;


    /**
     * Invoked with data from kafka.
     *
     * @param data the data to be processed.
     */
    @Override
    public void onMessage(ConsumerRecord<Integer, String> data) {
        try {
            //1. 读取热搜关键词
            System.out.println("获取kafka中的热门关键词数据.......");
            String keywords = data.value();
            System.out.println("获取topic中的数据进行消费，内容：" + keywords);

            //2. 根据关键词，生成新闻的缓存数据
            searchPageService.genSearchHtml(keywords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
