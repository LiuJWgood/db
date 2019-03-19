/**
 * Copyright (C), 2013-2019, 第一吹B有限公司
 * FileName: NewsTencentMaster
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
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//需求:  解析腾讯的新闻数据, 封装成news对象, 转换成json数据, 保存到redis中, 更改去重判断的key的值
public class NewsTencentMaster {

    //创建生产者对象
    private static KafkaSpiderProducer kafkaSpiderProducer = new KafkaSpiderProducer();

    public static void main(String[] args) throws Exception {
        //1. 确定首页url
        String topNewsUrl = "https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&id=&ext=ent&num=60";
        String noTopNewsUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=0";
        page(topNewsUrl,noTopNewsUrl);
        /*//2. 发送请求, 获取数据

        String topNewsJson = HttpClientUtils.doGet(topNewsUrl);
        String noTopNewsJson = HttpClientUtils.doGet(noTopNewsUrl);
        //3. 解析数据
        List<News> topNewsList = parseNewsJson(topNewsJson);
        System.out.println(topNewsList.size());
        List<News> noTopNewsList = parseNewsJson(noTopNewsJson);
        System.out.println(noTopNewsList.size());

        //4. 保存数据
        addNews(topNewsList);
        addNews(noTopNewsList);*/
    }
    //分页的方法
    public static void page(String topNewsUrl , String noTopNewsUrl) throws  Exception{
        // 1 表示处理热点新闻的数据
        //1.2. 发送请求, 获取数据
        String topNewsJson = HttpClientUtils.doGet(topNewsUrl);
        //1.3. 解析数据
        List<News> topNewsList = parseNewsJson(topNewsJson);
        System.out.println(topNewsList.size());
        //1.4. 保存数据
        addNews(topNewsList);

        //2. 对非热点的新闻处理
        Integer page = 1;
        while(true) {
            //2.2 发送请求, 获取数据
            String noTopNewsJson = HttpClientUtils.doGet(noTopNewsUrl);
            //2.3. 解析数据
            List<News> noTopNewsList = parseNewsJson(noTopNewsJson);
            if (noTopNewsList == null) {
                break;
            }

            System.out.println(noTopNewsList.size());
            //2.4. 保存数据
            addNews(noTopNewsList);


            //2.5. 获取下一页的url

            noTopNewsUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page="+page;
            page++;
        }

    }

    /**
     * 将新闻数据写入redis中  (改成写入kafka中)
     * @param newsList
     */
    public static void addNews(List<News> newsList){
        Gson gson = new Gson();
        for (News news : newsList) {
            //1. 将news对象转成json字符串
            String newsJson = gson.toJson(news);


           /* //2. 将json保存redis中
            Jedis jedis = JedisUtils.getJedis();
            jedis.lpush("bigData:spider:newsJson",newsJson);
            jedis.close();*/

            //2. 将json保存kafka中
            kafkaSpiderProducer.saveSpiderTokafka(newsJson);
            //3.将新添加的url添加到redis中set集合中，url去重处理
            Jedis jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:itemUrl", news.getDocurl());
            jedis.close();

        }
    }


    //解析新闻列表数据
    private static List<News> parseNewsJson(String newsJson) {
        //1. 将字符串的json数据转换成对象
        Gson gson = new Gson();
        Map<String,Object> map = gson.fromJson(newsJson, Map.class);

        //2. 获取当前这个json中一共有多少个新闻数据
        Double num = (Double) map.get("datanum");

        if(num.intValue() == 0){
            return null;
        }

        //3. 获取新闻的数据
        List<Map<String,Object>> newsListData = (List<Map<String,Object>>)map.get("data");
        List<News> newsList = new ArrayList<News>();
        for (Map<String, Object> newsListDatum : newsListData) { //一个map就是一个新闻的对象
            String itemUrl =(String) newsListDatum.get("vurl");
            if (itemUrl.contains("video")){
                continue;
            }
            //去重判断
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:itemUrl", itemUrl);
            jedis.close();
            if(flag){ //为 true ,表示当前这个url已经爬取过
                continue;
            }

            News news = new News();
            //4. 解析新闻数据
            //4.1 获取新闻的标题
            String title = (String) newsListDatum.get("title");
            news.setTitle(title);
            //4.2 获取新闻的时间
            String update_time =(String) newsListDatum.get("update_time");
            news.setTime(update_time);
            //4.3 获取新闻的来源和 编辑
            String source =(String) newsListDatum.get("source");
            news.setSource(source);
            news.setEditor(source);
            //4.4. 获取新闻的正文
            String content = (String)newsListDatum.get("intro");
            news.setContent(content);
            //4.5 获取新闻的url

            news.setDocurl(itemUrl);
            newsList.add(news);
        }
        return newsList;
    }
}