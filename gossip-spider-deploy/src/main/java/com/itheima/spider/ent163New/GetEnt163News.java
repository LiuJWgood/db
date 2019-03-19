package com.itheima.spider.ent163New;

import com.google.gson.Gson;
import com.itheima.spider.dao.NewsDao;
import com.itheima.spider.pojo.News;
import com.itheima.spider.utils.HttpClientUtils;
import com.itheima.spider.utils.JedisUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class GetEnt163News {

    private static NewsDao dao = new NewsDao();

    public static void main(String[] args) throws Exception {

        /*//1. 确定首页url
        String  indexUrl = "http://ent.163.com/special/000380VU/newsdata_index.js";

        //2. 发送请求  获取数据
        String jsonStr = HttpClientUtils.doGet(indexUrl);

        //2.1 处理json数据 : 将()中间的数据提取出来
        String json = parseJsonStr(jsonStr);
        System.out.println(json);
        */

        page();

    }



    //专门用来处理json中()的
    private static String parseJsonStr(String jsonStr) {

        int first = jsonStr.indexOf("(");
        int last = jsonStr.lastIndexOf(")");

        String json = jsonStr.substring(first + 1, last);
        return json;
    }


    //解析新闻的内容
    private static void parseNewsJson(String json) throws Exception { // 集合 或者  数组
        //1. 应该将这个json字符串转换成一个对象:
        //Gson是谷歌公司提供的一种json的转换工具, 可以将json的字符串转换为对象, 同时也可以将对象转换为json字符串

        Gson gson = new Gson();
        List<Map<String, Object>> list = gson.fromJson(json, List.class);

        //2. 变量这个list. 拿到每一个map集合, 获取里面的新闻数据
        for (Map<String, Object> map : list) {

            //2.1 获取新闻的详情页的url,根据这个url获取新闻的详细数据
            String docurl = (String) map.get("docurl");
            if (docurl.contains("photoview")) {
                continue;
            }
            //判断是否已经爬取过这个页面数据 redis的set集合中判断
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:gossip:163spider:docurl", docurl);
            if (flag){
                continue;
            }


            News news = parseNewsItem(docurl);
            //4. 保存数据
            dao.saveNews(news);

           jedis = JedisUtils.getJedis();
           jedis.sadd("bigData:gossip:163spider:docurl",docurl);
           jedis.close();

        }

    }

    //根据新闻的详情页解析新闻的数据
    private static News parseNewsItem(String docurl) throws Exception {
        News news = new News();
        //1. 发送请求, 获取数据
        String html = HttpClientUtils.doGet(docurl);

        //2. 解析数据
        Document document = Jsoup.parse(html);
        //2.1 解析新闻的标题
        Elements titleEl = document.select("#epContentLeft h1");
        String title = titleEl.text();
        news.setTitle(title);

        //2.2 解析新闻的时间
        Elements timeAndSouceEl = document.select(".post_time_source");
        String timeAndSouce = timeAndSouceEl.text();
        String[] split = timeAndSouce.split("　来源: ");//建议直接复制, 不要手动书写, 否则会切割失败的
        news.setTime(split[0]);
        //2.3 解析新闻的来源
        news.setSource(split[1]);
        //2.4 解析新闻的正文
        Elements contentEl = document.select("#endText p");
        String content = contentEl.text();
        news.setContent(content);
        //2.5 解析新闻的编辑
        Elements editorEl = document.select(".ep-editor");
        //责任编辑：陈少杰_b6952
        String editor = editorEl.text();
        editor = editor.substring(editor.indexOf("：") + 1, editor.lastIndexOf("_"));

        news.setEditor(editor);
        //2.6 将详情页的url添加进来
        news.setDocurl(docurl);

        System.out.println(news);

        return news;
    }

    //执行分业
    public static void page() throws Exception {

        //1. 确定首页url
        String nextUrl = "http://ent.163.com/special/000380VU/newsdata_index.js";
        String page = "02";
        while (true) {
            //2. 发送请求, 获取数据
            String jsonStr = HttpClientUtils.doGet(nextUrl);
            if (jsonStr == null) {
                break;
            }
            //2.1 处理json数据 : 将()中间的数据提取出来
            String json = parseJsonStr(jsonStr);
            //3. 解析数据
            parseNewsJson(json);

            //4. 获取下一页的url
            nextUrl = "http://ent.163.com/special/000380VU/newsdata_index_" + page + ".js";

            int i = Integer.parseInt(page);
            i++;
            //转换成字符串的时候, 需要分两种情况讨论:  1~9  10以上
            if (i < 10) {
                page = "0" + i;
            } else {
                page = i + "";
            }
        }


    }
}

