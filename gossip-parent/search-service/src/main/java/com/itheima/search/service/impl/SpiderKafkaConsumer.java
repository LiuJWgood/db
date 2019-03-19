package com.itheima.search.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itheima.gossip.pojo.News;
import com.itheima.search.service.IndexWriterService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SpiderKafkaConsumer implements MessageListener<String,String>{


    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();


    @Autowired
    private IndexWriterService indexWriterService;

    private Integer count = 5000;


    /**
     * 监听爬虫爬取的数据，建立索引
     *
     * @param data 新闻数据
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data) {

        try {
            //1.获取消息，解析新闻数据
            System.out.println("获取kafka中的爬虫数据.......");
            String newJson = data.value();

            //2.调用索引创建服务完成导入solr索引库的操作
            News news = gson.fromJson(newJson, News.class);
            String time = news.getTime();
            System.out.println("日期格式：" + time);
            String docurl = news.getDocurl();

            //3. 补全新闻数据的id属性，不要UUID，使用docUrl属性
       /*  String md5Hex = DigestUtils.md5Hex(docurl);
            System.out.println("新闻的id：" + md5Hex);*/
            news.setId(count);
            count++;

            List<News> newsList = new ArrayList<>(1);
            newsList.add(news);

            //处理日期格式问题
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            for (News newss : newsList) {
                String oldTime = newss.getTime();

                Date oldDate = format1.parse(oldTime);

                String newTime = format2.format(oldDate);
                newss.setTime(newTime);
            }


            indexWriterService.saveBeans(newsList);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
