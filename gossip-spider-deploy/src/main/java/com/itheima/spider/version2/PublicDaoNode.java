/**
 * Copyright (C), 2013-2019, 第一吹B有限公司
 * FileName: PublicDaoNode
 * Author: Administrator
 * Date: 2019/3/5
 * Description:
 * History:
 * <author>      <time>     <version>      <desc>
 * 作者姓名      修改时间      版本号          描述
 */
package com.itheima.spider.version2;

import com.google.gson.Gson;
import com.itheima.spider.dao.NewsDao;
import com.itheima.spider.pojo.News;
import com.itheima.spider.utils.JedisUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

//需求 : 从redis中获取newsJson, json字符串转换成news对象, 调用dao的addNews方法, 进行保存数据, 将保存成功的数据中url添加redis中
public class PublicDaoNode {
    private static NewsDao newsDao = new NewsDao();
    public static void main(String[] args) {
        Gson gson = new Gson();
        while(true) {


            //1. 从redis中获取newsJson数据
            Jedis jedis = JedisUtils.getJedis();
            List<String> list = jedis.brpop(20, "bigData:spider:newsJson");
            jedis.close();
            if (list == null || list.size() == 0) {
                break;
            }
            String newsJson = list.get(1);
            //2. 将json数据 转换成news对象
            News news = gson.fromJson(newsJson, News.class);

            //2.1  在进行保存之前, 先进行一次判断, 是否已经爬取过
            jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:itemUrl", news.getDocurl());
            jedis.close();
            if(flag){
                continue;
            }
            //3. 调用dao,保存即可
            newsDao.saveNews(news);

            //4. 将新添加的url添加到redis中set集合中
            jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:itemUrl", news.getDocurl());
            jedis.close();
        }
    }

}