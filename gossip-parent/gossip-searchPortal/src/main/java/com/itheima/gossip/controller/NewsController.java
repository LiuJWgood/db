package com.itheima.gossip.controller;

import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.gossip.service.NewsService;
import com.itheima.gossip.service.TopKeyWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class NewsController {
    @Autowired
    private NewsService newsService;
    @RequestMapping("/s")
    @ResponseBody // 保证返回json数据
    public List<News> findByKeywords(ResultBean resultBean){
        try {
            //1. 获取前端传递的参数

            //2. 判断前端的参数是否传递正确
            if(resultBean == null){
                //返回首页: 如果返回的数据是一个null值, 跳转首页
                return null;
            }
            if (resultBean.getKeywords() == null || "".equals(resultBean.getKeywords() )) {
                //返回首页: 如果返回的数据是一个null值, 跳转首页
                return null;
            }
            System.out.println(resultBean.getStartDate()+"---"+resultBean.getEndDate());
            //3. 调用service层, 查询数据
            List<News> newsList = newsService.findByKeywords(resultBean);

            //4. 返回给前端

            return newsList;

        }catch (Exception e){
            e.printStackTrace();
            return null; //如果后端有问题, 跳转首页
        }
    }


    @RequestMapping("/ps")
    @ResponseBody
    public PageBean findByPageQuery(ResultBean resultBean){
        try {
            String keywords = URLDecoder.decode(resultBean.getKeywords(), "utf-8");
            System.out.println("keywords：" + keywords);
            resultBean.setKeywords(keywords);

            //2. 判断前端的参数是否传递正确
            if(resultBean == null){
                //返回首页: 如果返回的数据是一个null值, 跳转首页
                return null;
            }
            if (resultBean.getKeywords() == null || "".equals(resultBean.getKeywords() )) {
                //返回首页: 如果返回的数据是一个null值, 跳转首页
                return null;
            }
            System.out.println(resultBean.getStartDate()+"---"+resultBean.getEndDate());

            //前端是否传递了分页参数 : 如果没有传递分页参数, 需要设置为默认值, 而不是不给于查询
            if(resultBean.getPageBean()==null){
                PageBean pageBean = new PageBean();
                resultBean.setPageBean(pageBean);
            }


            //3. 调用service层, 查询数据
           PageBean pageBean = newsService.findByPageQuery(resultBean);

            //4. 返回给前端

            return pageBean;

        }catch (Exception e){
            e.printStackTrace();
            return null; //如果后端有问题, 跳转首页
        }
    }

    @Autowired
    private TopKeyWordsService topKeyWordsService;
    @RequestMapping("/top")
    @ResponseBody
    public List<Map<String,Object>> findByTopKeywords(){
        try {
            List<Map<String, Object>> topKeywords = topKeyWordsService.findByTopKeywords();
            return topKeywords;
        }catch ( Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

}
