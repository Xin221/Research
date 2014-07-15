package inetintelliprocess.processor.newpubopinion;

import java.util.Date;

/**
 * NewsPubLately获取事件最近一天新闻发布情况
 * @author WQH
 *
 */

public class NewsPubLately {
    private String pageTitle;
    private Date day;
    private String url;
    private String webName;

    public String getPageTitle() {
        return pageTitle;
    }
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getWebName() {
        return webName;
    }
    public void setWebName(String webName) {
        this.webName = webName;
    }
    public Date getDay() {
        return day;
    }
    public void setDay(Date day) {
        this.day = day;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
