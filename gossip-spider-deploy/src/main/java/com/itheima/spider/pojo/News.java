package com.itheima.spider.pojo;

/**
 * 封装想要的新闻数据
 */
public class News {
    private Integer id;
    private String title;
    private String time;
    private String source;
    private String content;
    private String editor;
    private String docurl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getDocurl() {
        return docurl;
    }

    public void setDocurl(String docurl) {
        this.docurl = docurl;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", time='" + time + '\'' +
                ", source='" + source + '\'' +
                ", content='" + content + '\'' +
                ", editor='" + editor + '\'' +
                ", docurl='" + docurl + '\'' +
                '}';
    }
}
