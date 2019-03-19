package com.itheima.gossip.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.page.service.SearchPageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;

/**
 * @author itheima
 * @Title: KeywordsCacheController
 * @ProjectName gossip-parent
 * @Description: 热搜关键词建立缓存的controller
 * @date 2018/12/1915:40
 */
@Controller
public class KeywordsCacheController {


    @Reference(timeout = 5000)
    private SearchPageService searchPageService;


    /**
     * 根据关键词建立缓存
     * @return
     */
    @RequestMapping("/genKeywordsCache")
    @ResponseBody
    public String genKeywordsCache(String keywords){

        try {
            //1. 中文关键词处理
            System.out.println(keywords);
            keywords = new String(keywords.getBytes("ISO8859-1"),"utf-8");


            //2. 调用热搜关键词服务，建立 缓存数据
            searchPageService.genSearchHtml(keywords);

            return "success";

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "fail";
        }
    }
}
