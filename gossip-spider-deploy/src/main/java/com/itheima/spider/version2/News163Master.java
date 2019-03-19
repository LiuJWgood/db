/**
 * Copyright (C), 2013-2019, 第一吹B有限公司
 * FileName: News163Master
 * Author: Administrator
 * Date: 2019/3/5
 * Description:
 * History:
 * <author>      <time>     <version>      <desc>
 * 作者姓名      修改时间      版本号          描述
 */
package com.itheima.spider.version2;

import com.google.gson.Gson;
import com.itheima.spider.utils.HttpClientUtils;
import com.itheima.spider.utils.JedisUtils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

public class News163Master {
    public static void main(String[] args) throws Exception {
        //1. 确定首页的url
        String indexUrl = "http://ent.163.com/special/000380VU/newsdata_index.js";

       /* //2. 发送请求, 获取数据
        String newsJsonStr = HttpClientUtils.doGet(indexUrl);


        newsJsonStr = parseJson(newsJsonStr);

        //3. 解析数据
        parseNewsJson(newsJsonStr);*/
        page(indexUrl);

    }
    //分页的操作
    public static void page(String indexUrl) throws  Exception{
        String page = "02";
        while(true) {
            //2. 发送请求, 获取数据
            String newsJsonStr = HttpClientUtils.doGet(indexUrl);
            if(newsJsonStr == null ){
                break;
            }

            newsJsonStr = parseJson(newsJsonStr);

            //3. 解析数据
            parseNewsJson(newsJsonStr);


            //5. 获取下一页url
            indexUrl = "http://ent.163.com/special/000380VU/newsdata_index_" + page + ".js";
            int i = Integer.parseInt(page);
            i++;
            if(i <10){
                page = "0"+i;  //10
            }else{
                page = i+"";
            }
        }
    }
    //解析新闻的列表json
    private static void parseNewsJson(String newsJsonStr) throws  Exception{
        /**
         * json数据的格式有几种?  二种
         *      ["","",{}] :  在js中是一个数组 ,在java中表示的数组 或者 集合
         *      {"":"","":[]}: 在js中是一个对象, 在java中表示的对象 或者 map
         *
         * 如何判断一个json数据那种数据类型呢?
         *      只需要查看json字符串最外层是那种符号, 如果是[]那么就是数组和集合, 如果{}就是一个对象或者map
         */
        Gson gson = new Gson();
        List<Map<String,Object>> list = gson.fromJson(newsJsonStr, List.class);

        for (Map<String, Object> map : list) {
            // 解析新闻 : 标题, 新闻的内容, 新闻的链接, 新闻的时间, 新闻来源, 新闻的编辑
            // 获取到新闻的详情页的url, 解析url, 获取新闻的数据
            String itemUrl = (String) map.get("docurl");
            if(itemUrl.contains("photoview")  ){
                continue;
            }
            if(!itemUrl.contains("http://ent.163.com")){
                continue;
            }
            //当获取到新闻的url后, 判断一下, 这个url是否已经爬取过
            Jedis jedis = JedisUtils.getJedis();
            // 进行去重判断的时候, 这里是一个公共的key, 因为在这个key中既有163的url, 也有腾讯娱乐的url
            Boolean flag = jedis.sismember("bigData:spider:itemUrl", itemUrl);//建议key的定义, 要定义的长一点, 一定的命名规范
            jedis.close();
            if(flag){
                continue;
            }

            // 获取到了没有被爬取过url, 将url保存到redis中即可
            jedis = JedisUtils.getJedis();
            jedis.lpush("bigData:spider:news163:itemUrl",itemUrl);
            jedis.close();


        }

    }

    // 专门用来处理json的数据, 将其转换成标准的json格式
    private static String parseJson(String newsJsonStr) {
        int first = newsJsonStr.indexOf("(");
        int last = newsJsonStr.lastIndexOf(")");

        return newsJsonStr.substring(first+1, last);


    }

}
