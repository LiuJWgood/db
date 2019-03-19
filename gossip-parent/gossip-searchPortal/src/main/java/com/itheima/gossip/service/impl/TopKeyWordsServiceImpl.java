package com.itheima.gossip.service.impl;

import com.itheima.gossip.service.TopKeyWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.*;

@Service
public class TopKeyWordsServiceImpl implements TopKeyWordsService {
    @Autowired
    private JedisPool jedisPool;

    @Override
    public List<Map<String ,Object>> findByTopKeywords() throws Exception {
        //1. 获取jeids对象
        Jedis jedis = jedisPool.getResource();
        //2. 获取数据: 前五个
        Set<Tuple> tupleSet = jedis.zrevrangeWithScores("bigData:gossip:topKeywords", 0, 4);
        List<Map<String ,Object>> topList = new ArrayList<>();
        for (Tuple tuple : tupleSet) {
            String element = tuple.getElement(); //元素的内容
            double score = tuple.getScore(); //点击量
            Map<String ,Object> hashMap = new HashMap();
            hashMap.put("topKeyWords",element);
            hashMap.put("score",score);
            topList.add(hashMap);
        }
        jedis.close();
        //3. 返回controller
        return topList;
    }
}
