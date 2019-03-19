package com.itheima.page.service;

public interface SearchPageService {

    /**
     * 根据热搜关键词生成redis缓存数据
     */
    public boolean genSearchHtml(String keyword);

}
