package com.itheima.spider.dao;


import com.itheima.spider.pojo.News;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

public class NewsDao extends JdbcTemplate {

    public NewsDao() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();

        try {
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setJdbcUrl("jdbc:mysql://192.168.233.141:3306/gossip?characterEncoding=utf-8");
            dataSource.setUser("root");
            dataSource.setPassword("123456");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        super.setDataSource(dataSource);
    }

    //保存数据方法
    public void saveNews(News news){
        String[] parms = {news.getTitle(),news.getTime(),news.getSource(),news.getContent(),news.getEditor(),news.getDocurl()};
        update("INSERT  INTO  news VALUES (NULL ,?,?,?,?,?,?)",parms);
    }
}