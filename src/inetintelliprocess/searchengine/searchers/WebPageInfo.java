package inetintelliprocess.searchengine.searchers;
/**
 * searchers搜索核心包
 * 搜索实现
 */

import inetintelliprocess.util.LogWriter;

import java.util.List;
import java.util.Random;

import org.apache.log4j.MDC;

/**
 *
 * 文本信息载入管理的业务类
 * 封装了通过读取文本对象信息表载入文本信息的相关业务功能。
 * 搜出来的网页的数据结构
 *
 */
public class WebPageInfo {
    private String pageId = String.valueOf(new Random().nextInt());
    private String pageTitle = null;
    private String pageTime = null;
    private String pageContent = null;
    private String url = null;
    private String keyWords = null;
    private String lastWriteTime = null;
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

    public int getSendTo() {
        return sendTo;
    }

    public void setSendTo(int flag) {
        this.sendTo = flag;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        pageTitle = pageTitle.replace("\n", "");
        pageTitle = pageTitle.replace("\r", "");
        pageTitle = pageTitle.replace("  ", " ").trim();
        this.pageTitle = pageTitle;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        pageContent = pageContent.replace("  ", " ").trim();
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
        abstract1 = abstract1.replace("\n", "");
        abstract1 = abstract1.replace("\r", "");
        abstract1 = abstract1.replace("  ", " ").trim();
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

    public void setWebName(String url) {
        if(url!=null&&url.contains("Google-")){
            if(url.contains("ifeng.com")){
                this.webName = "Google-凤凰网";
            }else if(url.contains("xinhuanet.com")){
                this.webName = "Google-新华网";
            }else if(url.contains("people.com")){
                this.webName = "Google-人民网";
            }else if(url.contains("nerss.cn")){
                this.webName = "Google-中国地震应急搜救中心";
            }else if(url.contains("sina.com")){
                this.webName = "Google-新浪网";
            }else if(url.contains("163.com")){
                this.webName = "Google-网易";
            }else if(url.contains("cea.gov.cn")){
                this.webName = "Google-中国地震局";
            }else if(url.contains("bbc.com")){
                this.webName = "Google-BBC";
            }else if(url.contains("usgs.gov")){
                this.webName = "Google-美国地质调查局";
            }else if(url.contains("bgs.ac.uk")){
                this.webName = "Google-英国地质调查局";
            }
            else if(url.contains("qq.com")){
                this.webName = "Google-腾讯网";
            }
            else{
                this.webName = "Google-其他网站";
            }
        }else{
            if(url==null){
                this.webName = "";
            }else if(url.contains("ifeng.com")){
                this.webName = "凤凰网";
            }else if(url.contains("xinhuanet.com")){
                this.webName = "新华网";
            }else if(url.contains("people.com")){
                this.webName = "人民网";
            }else if(url.contains("nerss.cn")){
                this.webName = "中国地震应急搜救中心";
            }else if(url.contains("sina.com")){
                this.webName = "新浪网";
            }else if(url.contains("163.com")){
                this.webName = "网易";
            }else if(url.contains("cea.gov.cn")){
                this.webName = "中国地震局";
            }else if(url.contains("bbc.com")){
                this.webName = "BBC";
            }else if(url.contains("usgs.gov")){
                this.webName = "美国地质调查局";
            }else if(url.contains("bgs.ac.uk")){
                this.webName = "英国地质调查局";
            }
            else if(url.contains("qq.com")){
                this.webName = "腾讯网";
            }
            else{
                this.webName = "其他网站";
            }
        }

    }

    //写入数据表
    public synchronized boolean write(String tname, WebPageInfo info) throws Exception {
        boolean flag = false;
        try {
            if (urlExist(tname, info.url, info.getPageTitle())) {//是否已存在
                flag = true;
                return flag;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 写入之前首先看url是否存在当前数据库表（tname）中。如果存在返回true；
        // 把当前信息写入对应的当前数据库表（tname）中
        info = filterText(info,"<",">");
        try{
            if (WebPageInfoDbo.write(tname, info))//写入
                flag = true;
        }catch(Exception e){
            if(tname.equals("generalwebinfotbl"))
                MDC.put("eventID","日常搜索");
            else
                MDC.put("eventID",tname);
            LogWriter.logger.error("写入数据库"+tname+"失败！");
            e.printStackTrace();
        }
        return flag;
    }

    public synchronized static boolean write(String tname, List<WebPageInfo> infoList) throws Exception {
        boolean flag = false;
        try{
            if (WebPageInfoDbo.write(tname, infoList))//写入
                flag = true;
        }catch(Exception e){
            if(tname.equals("generalwebinfotbl"))
                MDC.put("eventID","日常搜索");
            else
                MDC.put("eventID",tname);
            LogWriter.logger.error("写入数据库"+tname+"失败！");
            e.printStackTrace();
        }
        return flag;
    }


    /**
     * 得到抽象的html视图
     * 暂时没有用到
     * @return
     */
    public String getAbstractHtmlView() {
        String html = "";
        String abs = this.Abstract;
        if (abs.length() < 20)
            abs = this.pageContent;
        String title = this.pageTitle;
        if (title.equals(""))
            title = abs;

        if (abs.length() > 200)
            abs = abs.substring(0, 199) + "......";
        if (title.length() > 30)
            title = title.substring(0, 29) + "......";
        html = "<br/><h3 class=\"t\"><a target='blank' href=\"" + this.url
                + "\" <em>" + title + "</em></a>";
        html += "</h3>";
        html += "<font size=\"-1\">" + abs;
        html += "<br/>";
        html += "<font color=\"#008000\">" + url + "</font></font><br/>";

        return html;
    }

    /**
     * 判断url是否存在
     * @param tname
     * @param u
     * @return
     * @throws Exception
     */
    public boolean urlExist(String tname, String u, String title) throws Exception {
        boolean flag = false;
        WebPageInfoDbo dbo = new WebPageInfoDbo();
        try{
            if (dbo != null){
                if (dbo.isExistUrl(tname, u, title))
                    flag = true;
            }
        }catch(Exception e){
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
        return flag;
    }


    public WebPageInfo filterText(WebPageInfo info, String content, String tag) {
        info.setAbstract(info.filterText(info.getAbstract(), "<",">"));
        info.setPageContent(info.filterText(info.getPageContent(), "<",">"));
        info.setPageTitle(info.filterText(info.getPageTitle(), "<",">"));
        return info;
    }


    /**
     * 过滤“tag1”以及“tag2”中间的文本内容
     * @param content
     * @param tag1
     * @param tag2
     * @return
     */
    public String filterText(String content, String tag1, String tag2) {
        String result = content;
        Boolean hit = false;
        while (true) {
            int idx = result.indexOf(tag1);
            if (idx < 0)
                break;
            String ss = result.substring(0, idx);
            result = result.substring(idx + 1);
            idx = result.indexOf(tag2);
            if (idx < 0)
                break;
            result = result.substring(idx + tag2.length());
            result = ss + result;
            hit = true;
        }
        if (hit)
            return getText(result);
        return getText(content);
    }

    /**
     * 获得文本
     * @param s
     * @return
     */
    public String getText(String s) {
        String res = s;
        res = res.replaceAll("</?[^>]+>", "");
        res = res.replaceAll("&nbsp;", "");
        res = res.replaceAll("&ldquo;", "");
        res = res.replaceAll("&rdquo;", "");

        res = res.replaceAll("|", "");
        res = res.replaceAll("&lt;", "");
        res = res.replaceAll("&gt;", "");
        res = res.replaceAll("&mdash;", "");
        res = res.replaceAll("&deg;", "");
        res = res.replaceAll("&ldquo;", "");
        res = res.replaceAll("&rdquo;", "");
        res = res.replaceAll("&middot;", "");
        res = res.replaceAll("&lsquo;", "‘");
        res = res.replaceAll("&rsquo;", "’");
        res = res.replaceAll("&hellip;", "");
        res = res.replaceAll("&#039;", "'");
        res = res.replaceAll("&quot;","'");

        res = res.replaceAll("&ensp;", "");
        res = res.replaceAll("&ensp;", "");
        res = res.replaceAll("&thinsp;", "");
        res = res.replaceAll("&zwnj;", "");
        res = res.replaceAll("&zwj;", "");
        res = res.replaceAll("&lrm;", "");
        res = res.replaceAll("&rlm;", "");
        res = res.replaceAll("&mdash;", "");
        res = res.replaceAll("&raquo;", "");
        res = res.replaceAll("\n", "");
        res = res.replaceAll("\r", "");
        res = res.replaceAll("\t", "");
        res = res.replaceAll("  ", " ");
        return res;
    }

    public String getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(String lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }
}
