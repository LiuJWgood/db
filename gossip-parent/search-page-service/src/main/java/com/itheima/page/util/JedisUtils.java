package com.itheima.page.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtils {
    private static JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100); //最大连接数
        config.setMaxIdle(50); //最大的闲时的数量
        config.setMinIdle(20);//最小的闲时的数量
        jedisPool  = new JedisPool(config,"192.168.12.142",6379);
    }


    //返回连接
    public static Jedis getJedis() {

        return jedisPool.getResource();
    }
}
