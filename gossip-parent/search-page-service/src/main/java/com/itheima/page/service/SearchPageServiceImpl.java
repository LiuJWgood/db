package com.itheima.page.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.page.util.JedisUtils;
import com.itheima.search.service.IndexSearcherService;
import redis.clients.jedis.Jedis;

/**
 * @author itheima
 * @Title: SearchPageServiceImpl
 * @ProjectName gossip-parent
 * @Description: 热词消费服务的实现类
 * @date 2018/12/1915:02
 */
@Service
public class SearchPageServiceImpl implements SearchPageService {

    /**
     * 导入索引搜索服务
     */
    @Reference(timeout = 5000)
    private IndexSearcherService indexSearcherService;

    /**
     * 根据对应的关键词生产缓存数据
     *
     * @param keyword ： 巩俐
     * @return
     */
    @Override
    public boolean genSearchHtml(String keyword) {
        try {
            //1. 读取关键词
            //2. 根据关键词，调用索引搜索服务，查询新闻数据
            ResultBean resultBean = new ResultBean();
            PageBean pb = new PageBean();
            resultBean.setPageBean(pb);
            resultBean.setKeywords(keyword);
            //根据关键词，查询总共多少页数据
            PageBean pageBean = indexSearcherService.findByPageQuery(resultBean);

            //默认取前5页结果数据
            int pageCount = 5;
            if(pageBean.getPageNumber() < 5){
                pageCount = pageBean.getPageNumber();
            }

            //3. 将前5页新闻数据写入redis缓存
            for(int i=1;i <= pageCount;i++){
                //获取第i页的数据
                resultBean.getPageBean().setPage(i);
                PageBean page = indexSearcherService.findByPageQuery(resultBean);
                resultBean.setPageBean(page);

                //写入redis缓存
                Jedis jedis = JedisUtils.getJedis();
                jedis.set(keyword+":"+i, JSON.toJSONString(page));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
