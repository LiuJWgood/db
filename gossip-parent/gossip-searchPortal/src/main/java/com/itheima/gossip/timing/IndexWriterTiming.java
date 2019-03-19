package com.itheima.gossip.timing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

@Controller
public class IndexWriterTiming {
    /*@Autowired
    private NewsService newsService;
    //编写定时执行方法
    @Scheduled(cron = "0 0/1 * * * ?") // cron: 定时执行表达式
    public void indexWriter(){
        try {
            System.out.println(new Date().toLocaleString());
            newsService.indexWriter();
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    @Autowired
    private JedisPool jedisPool;
    @PostConstruct // 当前类创建完实例后, 执行的初始化的方法
    public  void initRedisTopkeywords(){
        System.out.println("初始化的方法执行了.....");
        Jedis jedis = jedisPool.getResource();

        jedis.zadd("bigData:gossip:topKeywords",1768689,"裸考太冷了");
        jedis.zadd("bigData:gossip:topKeywords",1244586,"四级考试翻车");
        jedis.zadd("bigData:gossip:topKeywords",1164802,"2018蔚来日");
        jedis.zadd("bigData:gossip:topKeywords",845670,"四级翻译 移动支付");
        jedis.zadd("bigData:gossip:topKeywords",767658,"星爵宣布恋情");
        jedis.zadd("bigData:gossip:topKeywords",742795,"武汉被捅医生转到重症室");
        jedis.zadd("bigData:gossip:topKeywords",624723,"南大教授梁莹被取消导师资格");
        jedis.zadd("bigData:gossip:topKeywords",555909,"泰妍被舞台设备砸到头部");
        jedis.zadd("bigData:gossip:topKeywords",513054,"章莹颖案新进展");
        jedis.zadd("bigData:gossip:topKeywords",477204,"网红隐藏菜单");
        jedis.zadd("bigData:gossip:topKeywords",372920,"四级答案");
        jedis.zadd("bigData:gossip:topKeywords",343329,"王珂 刘涛");
        jedis.zadd("bigData:gossip:topKeywords",320681,"永辉超市创始人两兄弟分歧");
        jedis.zadd("bigData:gossip:topKeywords",270864,"二月河去世");
        jedis.zadd("bigData:gossip:topKeywords",269192,"地铁公司回应市民嫌地铁票价低");

        jedis.close();
    }
}
