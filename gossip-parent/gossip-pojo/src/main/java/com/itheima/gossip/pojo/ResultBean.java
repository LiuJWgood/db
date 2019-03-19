package com.itheima.gossip.pojo;

import java.io.Serializable;

public class ResultBean implements Serializable {

    private String keywords; //关键词
    private String startDate; //起始时间
    private String endDate;  // 结束时间
    private String source; //来源
    private String editor; //编辑

    private PageBean pageBean;

    public PageBean getPageBean() {
        return pageBean;
    }

    public void setPageBean(PageBean pageBean) {
        this.pageBean = pageBean;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "keywords='" + keywords + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", source='" + source + '\'' +
                ", editor='" + editor + '\'' +
                '}';
    }
}
