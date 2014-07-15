package inetintelliprocess.searchengine.searchers.dailysearcher;
/**
 * dailysearcher日常搜索核心包
 */

import inetintelliprocess.searchengine.frame.DailyManager;
import inetintelliprocess.searchengine.searchers.TargetLink;
import inetintelliprocess.searchengine.searchers.Time;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.searchengine.templates.SearcherTemplate;
import inetintelliprocess.util.LogWriter;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.MDC;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.*;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class DailySearcher extends Thread {

    protected LinkedList<TargetLink> cache = new LinkedList<TargetLink>();
    protected static final int MAX_DEPTH = 100;
    ///////////////////////////////////////////////////start
    protected int MAX_ITEM = 3;//日常搜索结果的最多条数，结果大于此值时，清空generalwebinfotbl数据表
    protected int counting = 0;
    ///////////////////////////////////////////////////end
    private SearcherTemplate myTemplate = null;
    private ArrayList<String> timeList = new ArrayList<String>();
    //	protected int upperDepth = 8;
    private DailyManager parentMgr = null;

    private static Object lock = new Object();

    protected boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void startWork() {
        synchronized (lock) {
            if (!running) {
                running = true;
                start();
            }
        }
    }

    public void stopWork() {
        synchronized (lock) {
            running = false;
        }
    }

    public SearcherTemplate getMyTemplate() {
        return myTemplate;
    }

    public void setMyTemplate(SearcherTemplate myTemplate) {
        this.myTemplate = myTemplate;
    }

    public ArrayList<String> getTimeList() {
        return timeList;
    }

    public void setTimeList(ArrayList<String> timeList) {
        this.timeList = timeList;
    }

    public DailyManager getParentMgr() {
        return parentMgr;
    }

    public void setParentMgr(DailyManager parentMgr) {
        this.parentMgr = parentMgr;
    }

    public DailySearcher(DailyManager p) {
        parentMgr = p;
    }

    public DailySearcher() {

    }

    /**
     * 初始化工作
     * 主要是设置初始搜索的url起点为DefLinkURL
     * 然后添加到搜索线程列表中
     */
    public void init() {
        TargetLink tlink = new TargetLink();
        tlink.setLinkURL(tlink.getDefLinkURL());//初始化设url为初始url： "http://news.qq.com/"
        cache.add(tlink);
    }

    public String getTblName() {
        return "generalWebInfoTbl";// 返回缺省通用数据表名字
    }
    public String getMetaValue(String tag, String name, String attName,
                               Parser parser) {
        String result = "";
        try {

            HasAttributeFilter metafilter = new HasAttributeFilter(tag, name);
            NodeList metaList = parser.parse(metafilter);
            if (metaList != null) {
                Node node = metaList.elementAt(0);
                if (node instanceof MetaTag) {
                    MetaTag meta = (MetaTag) node;
                    result = meta.getAttribute(attName);
                }
            }
        } catch (Exception exp) {
            MDC.put("eventID", "日常搜索");
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return result;
    }

    public String getTitle(Parser parser) {
        StringBuilder s = new StringBuilder();
        try {
            NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
            NodeList list = parser.parse(titleFilter);
            for (int i = 0; i < list.size(); i++) {
                Node n = list.elementAt(i);
                if (n instanceof TitleTag) {
                    TitleTag title = (TitleTag) n;
                    s.append(title.getStringText());
                    break;
                }
            }
        } catch (Exception exp) {
            MDC.put("eventID", "日常搜索");
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        String ss = s.toString();
        s.delete(0,s.length());
        return ss;
    }

    public NodeList getLinks(Parser parser) {

        try {
            NodeFilter lkFilter = new NodeClassFilter(LinkTag.class);
            NodeList list = parser.parse(lkFilter);
            return list;
        } catch (Exception exp) {
            MDC.put("eventID", "日常搜索");
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return null;
    }

    public StringBuffer getBody(Parser parser) {
        StringBuffer s = new StringBuffer();
        try {
            NodeFilter titleFilter = new NodeClassFilter(BodyTag.class);
            NodeList list = parser.parse(titleFilter);
            for (int i = 0; i < list.size(); i++) {
                Node n = list.elementAt(i);
                if (n instanceof BodyTag) {
                    BodyTag body = (BodyTag) n;
                    s.append(body.getStringText());
                }
            }

        } catch (Exception exp) {
            MDC.put("eventID", "日常搜索");
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return s;
    }

    public void run() {// 搜索线程循环
        run_search();
    }

    public void run_search() {
        //int count = 0;
        init();
        if (!running)
            System.out.println(this.getName() + "Search处于非执行状态");
        while (true) {
            init();
            while(!cache.isEmpty()){
                TargetLink tlink = cache.removeFirst();
                if(tlink != null ){
                    if (tlink.getSearched())
                        continue;
                    tlink.setSearched(true);

                    // 获取当前页面中的内容,从body中提取
                    WebPageInfo info = getContent(tlink.getLinkURL());
                    if (info != null) {
                        // 判断信息是否有效------------------------------------
                        if (isValidInfo(info)) {
                            String tname = getTblName();
                            info.setWebName(tlink.getLinkURL());
                            if(info.getWebName().equals("腾讯网")||info.getWebName().equals("其他网站")||info.getWebName().contains("RSS"))
                                info.setWebType("generalWeb");
                            else
                                info.setWebType("coreWeb");

                            try {

                                ///////////////////////////////////////////////////start
                                //int num = DbTools.querySumItem("generalwebinfotbl");
//									if(num >= MAX_ITEM)
//										DbTools.clearPartInfo("generalwebinfotbl","pageTime",num-MAX_ITEM);
                                info.write(tname, info);
                                //////////////////////////////////////////////end
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    String currentPageURI = getParentPath(tlink.getLinkURL());

                    // 寻找当前页面上的有效连接
                    String validuri = validUri(tlink.getLinkURL());

                    List<TargetLink> currentPageLinkResult = new ArrayList<TargetLink>();
                    try {
                        currentPageLinkResult = getLink(
                                tlink.getLinkURL(), validuri);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (currentPageLinkResult != null) {

                        for (int k = 0; k < currentPageLinkResult.size(); k++) {
                            TargetLink newLink = currentPageLinkResult.get(k);
                            if (newLink.getLinkURL().startsWith(".")
                                    || newLink.getLinkURL().startsWith("/")
                                    || !newLink.getLinkURL()
                                    .startsWith("http:")) {
                                newLink.setLinkURL(getValidURL(currentPageURI,
                                        newLink.getLinkURL()));
                            }
                            newLink.setDepth(tlink.getDepth() + 1);
                            if(!cache.contains(newLink))
                                cache.add(newLink);
                            //count++;
                        }
                        currentPageLinkResult.clear();
                        currentPageLinkResult = null;
                    }
                }
                //////////////////////////////////start
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    MDC.put("eventID", "日常搜索");
                    LogWriter.logger.warn(this.getName()+this.getState().toString()+"出错啦！");
                    e.printStackTrace();
                }	//
                /////////////////////////////////end
            }
        }
    }




    public String getParentPath(String url) {

        String res = url;
        int idx = url.lastIndexOf("/");
        if (idx <= "http://".length())
            return url;
        if (idx > 0) {
            res = res.substring(0, idx);
            if (!res.endsWith("/")) {
                res = res + "/";
                return res;
            }
        }
        return "";
    }

    public String getValidURL(String uri, String url) {
        String u = url;
        if (u.startsWith("./"))
            u = u.substring("./".length());
        if (u.startsWith("/"))
            u = u.substring("/".length());
        return uri + u;
    }

    // ---------------------------------------------------
    public Boolean isValidInfo(WebPageInfo info) {
        Boolean hit = false;
        int count = 0;
        for (int j = 0; j < this.parentMgr.getKwords().size(); j++) {
            String name = this.parentMgr.getKwords().get(j).getWord();
            if (info.getPageContent().toLowerCase().contains(name.toLowerCase())
                    || info.getPageTitle().toLowerCase().contains(name.toLowerCase())) {
                hit = true;
                count++;
            }
        }
        if (count < this.parentMgr.getKwords().size() / 3||info.getPageTitle()==null||info.getPageTitle().length()<18)
            return false;
        return hit;
    }

    public List<TargetLink> getLink(TargetLink tlink) throws IOException {// 搜索与name相关的超链接
        try {
            String uri = validUri(tlink.getLinkURL());
            List<TargetLink> list = getLink(tlink.getLinkURL(), uri);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setDepth(tlink.getDepth() + 1);
                }
            }
        } catch (Exception exp) {
            LogWriter.logger.info("getLink函数抛出IO异常");
        }
        return null;
    }

    public String validUri(String url) {
        int idx = url.lastIndexOf("/");
        if (idx <= 0)
            return url;
        String uri = url.substring(0, idx);
        if (!uri.endsWith("/"))
            uri = uri + "/";
        return uri;
    }

    public String dectedEncode(String url) {
        String oriEncode = "utf-8,gb2312,gbk,iso-8859-1";
        String[] encodes = oriEncode.split(",");
        for (int i = 0; i < encodes.length; i++) {
            if (dectedCode(url, encodes[i]))
                return encodes[i];

        }
        return "gbk";
    }

    public static boolean dectedCode(String url, String encode) {
        try {
            Parser parser = new Parser(url);
            parser.setEncoding(encode);
            for (org.htmlparser.util.NodeIterator e = parser.elements(); e
                    .hasMoreNodes();) {
                Node node = (Node) e.nextNode();

                if (node instanceof Html || node instanceof BodyTag)
                    return true;
            }
        } catch (Exception e) {
            // LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return false;
    }

    public List<TargetLink> getLink(String url, String uri) throws IOException {// 搜索与name相关的超链接
        List<TargetLink> list = new ArrayList<TargetLink>();
        Parser parser = null;
        if (!url.startsWith("http")) {
            if (url.startsWith(".") || url.startsWith("/")) {
                int idx = url.indexOf("/");
                url = url.substring(idx + 1);

                url = uri + url;
            } else if (!url.startsWith("http://www"))
                url = "http://www." + url;
        }
        try {
            parser = new Parser(url);
        } catch (ParserException e) {
            return null;
        }
        NodeList nodelist = null;

        if (nodelist == null || nodelist.size() == 0) {
            parser.reset();
            nodelist = getLinks(parser);
        }
        if(nodelist != null && nodelist.size() != 0)
            for (int i = 0; i < nodelist.size(); i++) {

                LinkTag tag = (LinkTag) nodelist.elementAt(i);
                String hrefLink = tag.getAttribute("href");
                if (hrefLink == null || hrefLink.equals(""))
                    continue;
                if (hrefLink.contains("javascript:"))
                    continue;
                // 从当前管理器中检索词
                if (tag.getAttribute("href").indexOf("video") > 0)
                    continue;
                int count = 0;
                for (int j = 0; j < this.parentMgr.getKwords().size(); j++) {
                    String name = this.parentMgr.getKwords().get(j).getWord();
                    String tagName = tag.getLinkText();
                    byte[] bs = tagName.getBytes();
                    String encode = getCharSet(getMetaValue("http-equiv", "Content-Type", "content", parser),url);
                    tagName = new String(bs, encode);

                    if (tagName.contains(name)||tag.getAttribute("href").contains(name)) {
                        count++;
                    }
                }

                TargetLink newTarget = new TargetLink();
                newTarget.setTitle(this.filterText(tag.getLinkText(), "<", ">"));
                newTarget.setLinkURL(tag.getAttribute("href"));
                if(newTarget.getTitle()!=null&&!newTarget.getTitle().isEmpty())
                    list.add(newTarget);
            }
        return list;
    }

    public WebPageInfo getContent(String url) {
        StringBuffer newHtml = new StringBuffer();
        String encode = "";
        String description = "";
        String keywords = "";
        String Title = "";
        Time time = new Time();
        WebPageInfo result = null; // new webPageInfo();
        Parser parser;
        try {
            if(url==null || url.isEmpty()) return null;
            parser = new Parser(url);
            encode = getCharSet(getMetaValue("http-equiv", "Content-Type", "content", parser),url);
            parser.setEncoding(encode);
            parser.reset();
            Title = getTitle(parser);// visitor.getTitle() ;
            parser.reset();
            description = getMetaValue("name", "description", "content", parser);
            if (description == null || description.equals("")) {
                parser.reset();
                description = getMetaValue("name", "Description", "content",
                        parser);
            }
            parser.reset();
            keywords = getMetaValue("name", "keywords", "content", parser);
            if (keywords == null || keywords.equals("")) {
                parser.reset();
                keywords = getMetaValue("name", "Keywords", "content", parser);
            }
            parser.reset();
            newHtml = getBody(parser);
            String filterHtml = null;
            if (!newHtml.equals("")) {
                if (isValidContent(newHtml.toString()) || isValidContent(Title)) {
                    result = new WebPageInfo();
                    result.setKeyWords(keywords);
                    result.setAbstract(description);
                    filterHtml = filterText(newHtml.toString());/////////////
                    result.setPageContent(filterHtml);
                    if (!isValidContent(result.getPageContent()))
                        result.setPageContent(Title + description + keywords);
                    result.setUrl(url);
                    result.setPageTitle(Title);
                    String strTime = null;
                    strTime = time.getTime(time.getAllTime(result, newHtml.toString()));
                    if (strTime == null || strTime.equals("")
                            || strTime.equals(" ")) {
                        result.setPageTime(new Timestamp(new Date().getTime()).toString());
                    } else {
                        result.setPageTime(strTime);
                    }

                } else
                    System.out.println("放弃:" + Title + "::" + url);
            } else
                System.out.println("放弃:" + Title + "::" + url);
        } catch (ParserException e) {
            result = null;
        }
        parser = null;
        return result;
    }

    public Boolean isValidContent(String content) {
        Boolean hit = false;
        for (int j = 0; j < this.parentMgr.getKwords().size(); j++) {
            String name = this.parentMgr.getKwords().get(j).getWord();
            if (content.toLowerCase().contains(name.toLowerCase())) {
                hit = true;
                break;
            }
        }
        return hit;
    }


    public String filterText(String content) throws ParserException{
        String result = content;
        result = result.replace("SCRIPT", "script");
        result = result.replace("Script", "script");
        result = filterText(result, "script");
        // result = filterText(result , "SCRIPT" ,"script") ;
        result = result.replace("STYLE", "style");
        result = result.replace("Style", "style");
        result = filterText(result, "style");//过滤<style></style>中间的内容
        result = filterText(result, "a");//过滤<a></a>中间的内容
        result = filterText(result, "A");//过滤<A></A>中间的内容
        result = filterText(result, "<!--", "-->");//过滤<<!--><-->>中间的内容
        result = filterText(result,"<",">");
        return getText(result);
    }


    public String filterText(String content, String tag) {
        StringBuilder result = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        result.append(content);
        //System.out.println("content is :\n"+content+"\n tag is :\n"+tag);
        while (true) {
            temp.delete(0, temp.length());
            temp.append("<");
            temp.append(tag);
            //System.out.println("temp is "+temp.toString());
            int idx01 = result.indexOf(temp.toString());
            //System.out.println(this.getName()+"idx01 is "+idx01);
            if (idx01 < 0)
                break;
            int idx02 = result.indexOf(">", idx01);
            //System.out.println(this.getName()+"idx02 is "+idx02);
            if (idx02 < 0)
                break;
            //System.out.println(this.getName()+"  "+result.substring(idx01, idx02+">".length()));
            result.delete(idx01, idx02+">".length());
            temp.delete(0, temp.length());
            temp.append("</");
            temp.append(tag);
            temp.append(">");
            int idx2 = result.indexOf(temp.toString());
            if (idx2 < 0)
                break;
            result.delete(idx2,idx2+temp.length());
            content = result.toString();
            result.delete(0, result.length());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public String filterText(String content, String tag1, String tag2) {
        StringBuilder result = new StringBuilder();
        result.append(content);
        Boolean hit = false;
        while (true) {
            int idx1 = result.indexOf(tag1);

            if (idx1 < 0)
                break;
            int idx2 = result.indexOf(tag2,idx1);
            if (idx2 < 0)
                break;
            result.delete(idx1, idx2+tag2.length());
            hit = true;
        }
        content = result.toString();
        result.delete(0, result.length());
        if (hit)
            return this.getText(content);
        return getText(content);
    }




    public WebPageInfo getContent(String url, String tag, String attName,
                                  String tag_value) throws IOException {
        String newHtml = "";
        String encode = "";
        String description = "";
        String keywords = "";
        String Title = "";
        Time time = new Time();
        WebPageInfo result = null; // new webPageInfo();
        Parser parser;
        try {
            parser = new Parser(url);
            encode = getMetaValue("http-equiv", "Content-Type", "content", parser);
            //System.out.println("encode is : "+encode);
            parser.setEncoding(this.getCharSet(encode, url));
            // HtmlPage visitor = new HtmlPage(parser);
            parser.reset();
            Title = getTitle(parser);// visitor.getTitle() ;
            parser.reset();
            description = getMetaValue("name", "description", "content", parser);
            if (description == null || description.equals("")) {
                parser.reset();
                description = getMetaValue("name", "Description", "content",
                        parser);
            }
            parser.reset();
            keywords = getMetaValue("name", "keywords", "content", parser);
            if (keywords == null || keywords.equals("")) {
                parser.reset();
                keywords = getMetaValue("name", "Keywords", "content", parser);
            }
            parser.reset();
            // parser.setEncoding(encoding.)
            AndFilter filter_div = new AndFilter(new TagNameFilter(tag),
                    new HasAttributeFilter(attName, tag_value));
            NodeList nodes = parser.parse(filter_div);
            //System.out.println("取得"+tag+"标签中"+attName+"="+tag_value+"的结点列表"+nodes.size()+"个！");
            if (nodes != null) {
                NodeFilter filter_p = new TagNameFilter("p");
                nodes = nodes.extractAllNodesThatMatch(filter_p, true);
                newHtml = nodes.toHtml();
                String filterHtml = null;
                if (!newHtml.equals("")) {
                    result = new WebPageInfo();
                    result.setKeyWords(keywords);
                    result.setAbstract(description);
                    filterHtml = filterText(newHtml);
                    result.setPageContent(filterHtml);
                    if (!isValidContent(result.getPageContent()))
                        result.setPageContent(Title + description + keywords);
                    result.setUrl(url);
                    result.setPageTitle(Title);

                    result.setKeyWords(result.getKeyWords().replaceAll(
                            this.myTemplate.getName(), ""));
                    //System.out.println("keywords is :"+result.getKeyWords());
                    result.setAbstract(result.getAbstract().replaceAll(
                            this.myTemplate.getName(), ""));
                    //System.out.println("abstract is :"+result.getAbstract());
                    result.setPageContent(result.getPageContent().replaceAll(
                            this.myTemplate.getName(), ""));
                    //System.out.println("content is :"+result.getPageContent());
                    result.setPageTitle(result.getPageTitle().replaceAll(
                            this.myTemplate.getName(), ""));
                    //System.out.println("title is :"+result.getPageTitle());
                    parser.reset();
                    String strTime = null;
                    ArrayList<String> strList = new ArrayList<String>();
                    strList = time.getAllTime(result, newHtml);
//					strList = time.getAllTime(result, parser.parse(new NodeClassFilter(BodyTag.class)).toHtml());
                    if (strList != null&&!strList.isEmpty()) {
                        //System.out.println("time list is not null");
                        strTime = time.getTime(strList);
                        if (strTime == null || strTime.equals("")
                                || strTime.equals(" ")) {
                            //System.out.println("time is null");
                            //result = null;
                            result.setPageTime(DateFormat.getDateTimeInstance().format(new Date()));
                        } else {
                            result.setPageTime(strTime);
                        }
                    }
                    else
                        result.setPageTime(DateFormat.getDateTimeInstance().format(new Date()));
                    nodes = null;
                    filter_div = null;
                    parser = null;
                } else {
                    nodes = null;
                    filter_div = null;
                    parser = null;
                    result = getContent(url);
                }
            } else {
                nodes = null;
                filter_div = null;
                parser = null;
                result = getContent(url);
            }

        } catch (ParserException e) {
            result = null;
        }
        parser = null;
        return result;
    }

    public String getTitle(String url, String tag) {
        NodeFilter filter_t = new TagNameFilter(tag);
        String title = null;
        Parser parser;
        try {
            parser = new Parser(url);
            NodeList nodes = parser.parse(filter_t);
            title = nodes.toString();
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return title;
    }

    public int countChar(String s, String c) {
        int count = 0;
        String ss = s;
        while (true) {
            int idx = ss.indexOf(c);
            if (idx < 0)
                return count;
            count++;
            ss = ss.substring(idx + 1);
        }
    }

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

        res.replaceAll("&ensp;", "");
        res.replaceAll("&ensp;", "");
        res.replaceAll("&thinsp;", "");
        res.replaceAll("&zwnj;", "");
        res.replaceAll("&zwj;", "");
        res.replaceAll("&lrm;", "");
        res.replaceAll("&rlm;", "");
        res.replaceAll("&mdash;", "");
        res.replaceAll("&raquo;", "");
        res = res.replaceAll("&#039;", "'");
        res = res.replaceAll("&quot;","'");

        res = res.replace("\n", "");
        res = res.replace("\r", "");
        res = res.replace("\t", "");
        res = res.replace("  ", " ");
        res = res.replaceAll("[\\s]{2,}", " ");

        return res;
    }

    public String getCharSet(String content_type, String url){
        String encode = null;
        if(content_type == null||content_type.isEmpty())
            encode = dectedEncode(url);
        else{
            int idx = content_type.indexOf("charset=");
            if(idx >= 0){
                idx = idx + "charset=".length();
                encode = content_type.substring(idx);
            }
            else
                encode = dectedEncode(url);
        }
        //System.out.println("get the charset is : "+encode);
        return encode;
    }

    public void main(){
        DailySearcher generalSearcher = new DailySearcher();
        generalSearcher.init();
        generalSearcher.startWork();
    }
}
