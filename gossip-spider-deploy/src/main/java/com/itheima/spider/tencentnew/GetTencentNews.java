/**
 * Copyright (C), 2013-2019, 第一吹B有限公司
 * FileName: GetTencentNews
 * Author: Administrator
 * Date: 2019/3/4
 * Description: 爬虫腾讯娱乐新闻
 * History:
 * <author>      <time>     <version>      <desc>
 * 作者姓名      修改时间      版本号          描述
 */
package com.itheima.spider.tencentnew;

import com.google.gson.Gson;
import com.itheima.spider.dao.NewsDao;
import com.itheima.spider.pojo.News;
import com.itheima.spider.utils.HttpClientUtils;
import com.itheima.spider.utils.JedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//腾讯娱乐爬虫程序
public class GetTencentNews {
    private static NewsDao newsDao = new NewsDao();
    private static News news = new News();
    public static void main(String[] args) throws Exception {
        /*//1. 确定首页的url
        String topUrl = "https://pacaio.match.qq.com/irs/rcd?cid=135&token=6e92c215fb08afa901ac31eca115a34f&callback=jspnphotnews&ext=ent&page=0&num=15&expIds=20190304A07UT8|20190228006874|20190304A0CWXY|20190228009769|20190304A0FA6X&callback=jspnphotnews";
        String noTopUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=0&callback=__jp7";

        //2. 发起请求, 获取数据
        String topJsonStr = HttpClientUtils.doGet(topUrl);
        String noTopJsonStr = HttpClientUtils.doGet(noTopUrl);

        //2.1 处理json数据: 去掉json数据两边的()
        String topJson  = parseJson(topJsonStr);
        String noTopJson  = parseJson(noTopJsonStr);
        System.out.println(topJson);

        //3. 解析数据
        List<News> topNewsList = parseNewsJson(topJson);
        List<News> noNewsTopList = parseNewsJson(noTopJson);

        //4. 保存数据
        saveNews(topNewsList);
        saveNews(noNewsTopList);*/
        page();

    }

    // 处理json数据的方法: 去掉()
    private static String parseJson(String jsonStr) {

        int first = jsonStr.indexOf("(");
        int last = jsonStr.lastIndexOf(")");

        return jsonStr.substring(first + 1, last);

    }


    // 解析新闻数据的方法
    private static List<News> parseNewsJson(String json) {
        //1.  将json字符串转换成对象或者map
        // gson是谷歌公司提供的一个专门用户转换json格式的工具
        Gson gson = new Gson();
        Map<String,Object> map = gson.fromJson(json, Map.class);

        //2. 解析新闻数据
        Double datanum =(Double) map.get("datanum");

        if( datanum.intValue() == 0 ){
            //此时就没有任何的数据了
            return null;
        }
        //2.1 获取到新闻的集合 : data
        //创建newsDate的list集合(所有数据,有些不需要)
        List<Map<String,Object>> newsData = (List<Map<String,Object>>)map.get("data");
        //创建new的集合
        List<News> newsList = new ArrayList<News>();
        for (Map<String, Object> newsMap : newsData) {
                    //判断是否已经爬取过
            // 获取详情url
            String docurl = (String) newsMap.get("surl");
            if(docurl == null || docurl == ""){
                continue;
            }
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:Tentcent:docurl",docurl);
            jedis.close();
            if (flag){
                //已经爬取过
                continue;
            }



            for (String key : newsMap.keySet()) {  //遍历一个map其实就是一个News对象
                if(key.equals("title")){
                    news.setTitle((String) newsMap.get(key));
                }
                if(key.equals("update_time")){
                    news.setTime((String) newsMap.get(key));
                }
                if(key.equals("source")){
                    news.setSource((String) newsMap.get(key));
                    news.setEditor((String) newsMap.get(key));
                }
                if(key.equals("intro")){
                    news.setContent((String) newsMap.get(key));
                }
                if(key.equals("surl")){
                    news.setDocurl((String) newsMap.get(key));
                }

            }
            newsList.add(news);
            System.out.println(news);
        }


        return newsList;

    }

    // 保存数据的方法
    public static void saveNews(List<News> newsList){

        for (News news : newsList) {

            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:Tentcent:docurl",news.getDocurl());
            jedis.close();
            if (flag){
                //已经爬取过
                continue;
            }

            newsDao.saveNews(news);

            //没有爬取过的数据 添加url到redis中
            jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:Tencent:docurl",news.getDocurl());

            jedis.close();
        }
    }


    //执行分页方法
    public static void page()throws Exception{


//0 . 获取热点新闻的数据
        //1. 确定首页的url
        String topUrl = "https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&id=&ext=ent&num=60&expIds=&callback=__jp0";
        String noTopUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=0&callback=__jp7";
        //2. 发起请求, 获取数据
        String topJsonStr = HttpClientUtils.doGet(topUrl);
        //2.1 处理json数据: 去掉json数据两边的()
        String topJson  = parseJson(topJsonStr);
        //3. 解析数据
        List<News> topNewsList = parseNewsJson(topJson);
        //4. 保存数据
        saveNews(topNewsList);

        //5. 分页获取非热点的数据
        Integer page = 1;
        while(true){
            //5.1 发起请求, 获取非热点新闻数据
            String noTopJsonStr = HttpClientUtils.doGet(noTopUrl);
            //5.2 处理json数据
            String noTopJson  = parseJson(noTopJsonStr);
            //5.3  解析数据
            List<News> noTopNewsList = parseNewsJson(noTopJson);

            //5.4 保存数据
            if(noTopNewsList == null){
                // 表示已经没有数据了
                break;
            }
            saveNews(noTopNewsList);

            //5.5 获取下一页的url
            noTopUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page="+page+"&callback=__jp7";
            page++;
        }



    }

}