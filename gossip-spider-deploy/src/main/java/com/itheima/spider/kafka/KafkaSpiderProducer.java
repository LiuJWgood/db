package com.itheima.spider.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaSpiderProducer {

    //配置信息加载
    private static Properties props = null;

    //消息生产者
    private static KafkaProducer<String, String> kafkaProducer = null;

    //初始化工作
    static {
        props = new Properties();
        props.put("bootstrap.servers", "node01:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        // 1.创建KafkaProducer 也就kafka的生产者
        // 1.1 需要一个Properties对象--怎么连接kafka集群
        kafkaProducer = new KafkaProducer<String, String>(props);

    }

    /**
     * 将爬取的数据写入kafka
     * @param  : 新闻数据对象
     */
    public  void saveSpiderTokafka(String news){

        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("spider",news);
        // 2.1 发送ProducerRecord对象
        kafkaProducer.send(producerRecord);

    }
}