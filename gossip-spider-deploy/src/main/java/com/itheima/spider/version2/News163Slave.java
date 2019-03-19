/**
 * Copyright (C), 2013-2019, 第一吹B有限公司
 * FileName: News163Slave
 * Author: Administrator
 * Date: 2019/3/5
 * Description:
 * History:
 * <author>      <time>     <version>      <desc>
 * 作者姓名      修改时间      版本号          描述
 */
package com.itheima.spider.version2;

import com.google.gson.Gson;
import com.itheima.spider.kafka.KafkaSpiderProducer;
import com.itheima.spider.pojo.News;
import com.itheima.spider.utils.HttpClientUtils;
import com.itheima.spider.utils.JedisUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.util.List;

// 需求:  从redis中获取详情页的url, 解析url, 封装成news对象, 将news对象转成json字符串, 将其在保存到redis中, 一直获取url
public class News163Slave {

    //创建生产者对象
    private static KafkaSpiderProducer kafkaSpiderProducer = new KafkaSpiderProducer();

    public static void main(String[] args) throws Exception {
        while(true) {
            //1.从redis中获取url
            Jedis jedis = JedisUtils.getJedis();
            // 在list中一般只有两个内容, 第一个表示的当前key的名称, 第二个表示的是从这个key弹出来的元素
            List<String> list = jedis.brpop(20, "bigData:spider:news163:itemUrl");//每一次只有一个
            if(list == null || list.size()==0){
                break;
            }
            String itemUrl = list.get(1);

            jedis.close();
            //2. 解析url
            News news = parseItemNews(itemUrl);
            //3. 将news对象, 转成json字符串
            Gson gson = new Gson();
            String newsJson = gson.toJson(news);

            //4. 保存到redis中: list   集成完成kafka不需要写入redis
            kafkaSpiderProducer.saveSpiderTokafka(newsJson);
            // 将新添加的url添加到redis中set集合中，url去重处理
            jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:itemUrl", news.getDocurl());
            jedis.close();

           /* jedis = JedisUtils.getJedis();
            // key也是一个公共的key, 因为所有的news对象都要往这个key中存储
            jedis.lpush("bigData:spider:newsJson", newsJson);
            jedis.close();*/
        }
    }

    // 专门用来解析新闻的详情页
    private static News parseItemNews(String itemUrl) throws Exception {
        System.out.println(itemUrl);
        //1. 发送请求, 获取数据
        String html = HttpClientUtils.doGet(itemUrl);

        //2. 解析数据
        Document document = Jsoup.parse(html);
        News news = new News();
        //2.1 新闻的标题
        Elements titleEl = document.select("#epContentLeft>h1");
        news.setTitle(titleEl.text());
        //2.2 新闻的时间
        Elements timeAndSourceEl = document.select(".post_time_source");
        String timeAndSource = timeAndSourceEl.text();
        System.out.println(timeAndSource);
        String[] split = timeAndSource.split("　来源: ");

        news.setTime(split[0]);
        //2.3 新闻的来源
        news.setSource(split[1]);
        //2.4 新闻的内容
        Elements pEls = document.select("#endText p");
        news.setContent(pEls.text());
        //2.5 新闻的编辑
        Elements editorEl = document.select(".ep-editor");
        //责任编辑：李思_NBJ11322
        String editor = editorEl.text();
        editor =  editor.substring(editor.indexOf("：")+1,editor.lastIndexOf("_"));
        news.setEditor(editor);
        //2.6 新闻的链接
        news.setDocurl(itemUrl);


        return news;
    }



}
