package inetintelliprocess.searchengine.rss;

import inetintelliprocess.util.LogWriter;

import java.util.Random;

public class RSSPageInfo {
    private String pageId = String.valueOf(new Random().nextInt());
    private String pageTitle = null;
    private String pageContent = null;
    private String url = null;
    private String pageTime = null;
    private String keyWords = null;
    private String Abstract = null;
    private String webType = null;
    private String webName = null;
    private int sendTo = 0;

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPageTime() {
        return pageTime;
    }

    public void setPageTime(String pageTime) {
        this.pageTime = pageTime;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getAbstract() {
        return Abstract;
    }

    public void setAbstract(String abstract1) {
        Abstract = abstract1;
    }

    public String getWebType() {
        return webType;
    }

    public void setWebType(String webType) {
        this.webType = webType;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public int getSendTo(){
        return this.sendTo;
    }

    public void setSendTo(int sendTo){
        this.sendTo = sendTo;
    }

    public boolean writetoDB (String tname, RSSPageInfo info) throws Exception {
        boolean flag = false;
        try {
            if (urlExist(tname, info.url)) {
                flag = true;
                return flag;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
        // 写入之前首先看url是否存在当前数据库表（tname）中。如果存在返回true；
        // 把当前信息写入对应的当前数据库表（tname）中
        // 执行数据库写操作
        RSSDBO.insertRSSPageInfo(tname, info);
        return flag;
    }

    public boolean urlExist(String tname, String url) throws Exception {
        // 在当前数据的tname的表url索引上查找是否有对应的u
        // 有一样值的url返回true；否则false；
        boolean flag = false;
        if (RSSDBO.isExistUrl(tname, url))
            flag = true;
        return flag;
    }
}
