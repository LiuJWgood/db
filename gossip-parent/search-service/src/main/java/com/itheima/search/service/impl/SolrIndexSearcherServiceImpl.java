package com.itheima.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.gossip.pojo.News;
import com.itheima.gossip.pojo.PageBean;
import com.itheima.gossip.pojo.ResultBean;
import com.itheima.search.service.IndexSearcherService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SolrIndexSearcherServiceImpl implements IndexSearcherService {
    @Autowired
    private SolrServer solrServer;

    @Override
    public PageBean findByPageQuery(ResultBean resultBean)throws Exception{

        Integer page = resultBean.getPageBean().getPage();
        Integer pageSize = resultBean.getPageBean().getPageSize();
        //1. 封装查询条件
        //1.1 封装基本查询
        SolrQuery solrQuery = new SolrQuery("text:"+resultBean.getKeywords());

        //1.2.高亮展示数据的设置
        solrQuery.setHighlight(true); //打开了高亮

        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("content");

        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");


        //1.3. 搜索工具的实现
        //1.3.1 编辑 和 来源
        String edit = resultBean.getEditor();
        if(edit!=null && !"".equals(edit)){
            // 说明前端传递了编辑了
            solrQuery.addFilterQuery("editor:"+edit);
        }

        String src = resultBean.getSource();
        if(src!=null && !"".equals(src)){
            // 说明前端传递了编辑了
            solrQuery.addFilterQuery("source:"+src);
        }

        //1.3.2 日期范围条件封装
        String startDate = resultBean.getStartDate(); //起始值
        String endDate = resultBean.getEndDate();//结束值
        System.out.println("222222");
        if(startDate!=null && !"".equals(startDate) && endDate !=null && !"".equals(endDate)){
            System.out.println("333333");
            //日期范围查询:  UTC格式:  yyyy-MM-dd'T'HH:mm:ss'Z'
            //  12/10/2018 16:18:25 :  dd/MM/yyyy HH:mm:ss
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            Date start = format2.parse(startDate);
            Date end = format2.parse(endDate);

            startDate = format1.format(start);
            endDate = format1.format(end);

            solrQuery.addFilterQuery("time:["+startDate+" TO "+endDate+"]");
        }

        //1.4. 日期进行排序: 倒序
        solrQuery.setSort("time", SolrQuery.ORDER.desc);

        //1.5 封装分页参数: start  rows

        solrQuery.setStart((page-1)*pageSize);
        solrQuery.setRows(pageSize);

        //2. 执行查询
        QueryResponse response = solrServer.query(solrQuery);
        //2.1 获取高亮展示内容
        Map<String, Map<String, List<String>>> map = response.getHighlighting();

        //List<News> newsList = response.getBeans(News.class); //此处是有问题的
        //3. 获取当前页数据
        SolrDocumentList documentList = response.getResults();
        List<News> newsList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SolrDocument document : documentList) {
            News news = new News();
            String id = (String) document.get("id");
            news.setId(Integer.parseInt(id));

            String title = (String) document.get("title");
            //获取title的高亮内容
            Map<String, List<String>> listMap = map.get(id);
            List<String> list = listMap.get("title");
            if(list!=null && list.size()>0 ){
                // 说明title是有高亮的
                title = list.get(0);
            }

            news.setTitle(title);

            Date time = (Date) document.get("time"); // 在solr中存储的是Date类型
            //getTime: 获取某一个时间的毫秒值
            time.setTime(time.getTime()-(1000*60*60*8));

            news.setTime(format.format(time));

            String source = (String) document.get("source");
            news.setSource(source);

            String content = (String) document.get("content");

            //获取content的高亮内容
            list = listMap.get("content");
            if(list!=null && list.size()>0 ){
                // 说明content是有高亮的
                content = list.get(0);
            }


            news.setContent(content);

            String editor = (String) document.get("editor");
            news.setEditor(editor);

            String docurl = (String) document.get("docurl");
            news.setDocurl(docurl);

            newsList.add(news);
        }


        //4. 封装pageBean: newsList,pageCount,pageNumber
        PageBean pageBean = resultBean.getPageBean();
        //4.1 封装了当前页的数据
        pageBean.setNewsList(newsList);

        //4.2 封装总条数
        Long pageCount = documentList.getNumFound();//总条数
        pageBean.setPageCount(pageCount.intValue());

        //4.3 封装总页数: 向上取整
        Double pageNumber = Math.ceil((double) pageCount / pageSize);
        pageBean.setPageNumber(pageNumber.intValue());

        return pageBean;


    }



    @Override
    public List<News> findByKeywords(ResultBean resultBean) throws Exception {
        //1. 基本查询
        SolrQuery solrQuery = new SolrQuery("text:" + resultBean.getKeywords());

        //2.高亮显示数据的设置
        solrQuery.setHighlight(true); //打开了高亮

        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("content");

        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        //3. 搜索工具的实现
        //3.1 编辑 和 来源
        String edit = resultBean.getEditor();
        if(edit!=null && !"".equals(edit)){
            // 说明前端传递了编辑了
            solrQuery.addFilterQuery("editor:"+edit);
        }

        String src = resultBean.getSource();
        if(src!=null && !"".equals(src)){
            // 说明前端传递了编辑了
            solrQuery.addFilterQuery("source:"+src);
        }

        //3.2 日期范围条件封装
        String startDate = resultBean.getStartDate(); //起始值
        String endDate = resultBean.getEndDate();//结束值
        System.out.println("222222");
        if(startDate!=null && !"".equals(startDate) && endDate !=null && !"".equals(endDate)){
            System.out.println("333333");
            //日期范围查询:  UTC格式:  yyyy-MM-dd'T'HH:mm:ss'Z'
            //  12/10/2018 16:18:25 :  dd/MM/yyyy HH:mm:ss
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            Date start = format2.parse(startDate);
            Date end = format2.parse(endDate);

            startDate = format1.format(start);
            endDate = format1.format(end);

            solrQuery.addFilterQuery("time:["+startDate+" TO "+endDate+"]");
        }

        //4. 日期进行排序: 倒序
        solrQuery.setSort("time", SolrQuery.ORDER.desc);

        QueryResponse response = solrServer.query(solrQuery);

        //高亮显示内容
        Map<String, Map<String, List<String>>> map = response.getHighlighting();

        SolrDocumentList documentList = response.getResults();
        List<News> newsList = new ArrayList<News>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SolrDocument document : documentList) {
            News news = new News();
            String id = (String) document.get("id");
            news.setId(Integer.parseInt(id));

            String title = (String) document.get("title");
            //获取title的高亮内容
            Map<String, List<String>> listMap = map.get(id);
            List<String> list = listMap.get("title");
            if (list != null && list.size() > 0) {
                //有高亮的title
                title = list.get(0);
            }
            news.setTitle(title);


            Date time = (Date) document.get("time"); // 在solr中存储的是Date类型
            //getTime: 获取某一个时间的毫秒值
            time.setTime(time.getTime() - (1000 * 60 * 60 * 8));

            news.setTime(format.format(time));

            String source = (String) document.get("source");
            news.setSource(source);

            String content = (String) document.get("content");
            //获取content的高亮内容

            list = listMap.get("content");
            if (list != null && list.size() > 0) {
                //有高亮的content
                content = list.get(0);
            }
            news.setContent(content);
            ;
            String editor = (String) document.get("editor");
            news.setEditor(editor);

            String docurl = (String) document.get("docurl");
            news.setDocurl(docurl);

            newsList.add(news);
        }
        return newsList;
    }
}
