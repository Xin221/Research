package inetintelliprocess.searchengine.searchers.eventsearcher;
/**
 * eventsearcher事件搜索核心包
 */
import inetintelliprocess.searchengine.frame.SearcherManager;
import inetintelliprocess.searchengine.searchers.TargetLink;
import inetintelliprocess.searchengine.searchers.Time;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.searchengine.templates.SearcherTemplate;
import inetintelliprocess.util.LogWriter;

import java.io.IOException;
import java.util.ArrayList;
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
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TextExtractingVisitor;
/**
 * 普通网站的搜索
 * 爬取网页内容的核心业务类
 * 主要负责针对目标URL网页提取网页标题、
 * 网页发布时间、网页摘要、网页正文内容、
 * 网页中相关URL等信息
 *
 */
public class Searcher extends Thread {

    public LinkedList<TargetLink> cache = new LinkedList<TargetLink>();//链接列表
    protected static final int MAX_DEPTH = 100;//
    private SearcherTemplate myTemplate = null;//搜索模板
    private ArrayList<String> timeList = new ArrayList<String>();
    protected int upperDepth = 3;//搜索深度
    private SearcherManager parentMgr = null;//当前搜索线程所属的SearcherManager

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

    public int getUpperDepth() {
        return upperDepth;
    }

    public void setUpperDepth(int upperDepth) {
        this.upperDepth = upperDepth;
    }

    public SearcherManager getParentMgr() {
        return parentMgr;
    }

    public void setParentMgr(SearcherManager parentMgr) {
        this.parentMgr = parentMgr;
    }

    public Searcher(SearcherManager p) {
        parentMgr = p;
    }

    public Searcher() {

    }

    public void init() {
        TargetLink tlink = new TargetLink();
        tlink.setLinkURL(tlink.getDefLinkURL());
        tlink.setDepth(1);
        cache.add(tlink);

    }

    public String getTblName() {
        if (this.parentMgr == null)
            return "generalWebInfoTbl";// 返回缺省通用数据表名字
        if (this.parentMgr.getEvtHandler() == null)
            return "generalWebInfoTbl";// 返回缺省通用数据表名字
        return this.parentMgr.getEvtHandler().getTableName();
    }

    /**
     * 获得meta标签中attname属性值
     * <meta tag="name" attname="习近平：反腐败是实现“中国梦”前提">
     * <meta name="Description" content="习近平：反腐败是实现“中国梦”前提">
     * @param tag
     * @param name
     * @param attName
     * @param parser
     * @return
     */
    public String getMetaValue(String tag, String name, String attName,
                               Parser parser) {
        String result = "";
        try {
            //HasAttributeFilter对属性进行过滤
            //接受有给定tag属性以及name值的tag
            HasAttributeFilter metafilter = new HasAttributeFilter(tag, name);
            NodeList metaList = parser.parse(metafilter);
            if (metaList != null) {
                Node node = metaList.elementAt(0);
                if (node instanceof MetaTag) {
                    MetaTag meta = (MetaTag) node;
                    result = meta.getAttribute(attName);//获得attName属性值
                }
            }
        } catch (Exception exp) {
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return result;
    }

    /**
     * 得到title内容
     * <title>习近平:把权力关进制度的笼子里</title>
     * @param parser
     * @return
     */
    public String getTitle(Parser parser) {
        String s = "";
        try {
            NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
            NodeList list = parser.parse(titleFilter);
            for (int i = 0; i < list.size(); i++) {
                Node n = list.elementAt(i);
                if (n instanceof TitleTag) {
                    TitleTag title = (TitleTag) n;
                    s = title.getStringText();
                    break;
                }
            }
        } catch (Exception exp) {
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return s;
    }

    /**
     * 得到链接
     * <link href="http://www.xinhuanet.com/xilan/xl_master120621.css" rel="stylesheet" type="text/css" />
     * @param parser
     * @return
     */
    public NodeList getLinks(Parser parser) {

        try {
            //NodeClassFilter对结点类进行过滤，参数为要匹配的类
            NodeFilter lkFilter = new NodeClassFilter(LinkTag.class);
            NodeList list = parser.parse(lkFilter);
            return list;
        } catch (Exception exp) {
            LogWriter.logger.warn("HtmlParser jar包解析异常");
            //exp.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param parser
     * @return
     */
    public String getBody(Parser parser) {
        String s = "";
        try {
            NodeFilter titleFilter = new NodeClassFilter(BodyTag.class);
            NodeList list = parser.parse(titleFilter);
            for (int i = 0; i < list.size(); i++) {
                Node n = list.elementAt(i);
                if (n instanceof BodyTag) {
                    BodyTag body = (BodyTag) n;
                    s += body.getStringText();
                }
            }
        } catch (Exception exp) {
            LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return s;
    }

    public void run() {// 搜索线程循环
        run_search();
    }

    /**
     * 从搜索队列中获取URL，判断搜索深度，调用getContent、getLink等方法
     */
    public void run_search() {
        int count = 0;
        init();//把初始URL即DefLinkURL加入到cache队列中


        if (!running)
            System.out.println(this.getName() + "Search处于非执行状态");
        while (running && cache.size() > 0 && count < MAX_DEPTH) {//while1线程正在执行且cache队列非空且搜索条数<MAX_DEPTH
            while(cache.size()>0){//while2遍历搜索整个cache队列中的链接
                TargetLink tlink = cache.removeFirst();//取出一个链接
                if(tlink != null && tlink.getDepth() < this.upperDepth){//if1链接非空且搜索深度<upperDepth
                    if (tlink.getSearched())//判断是否已搜索
                        continue;
                    tlink.setSearched(true);//置为已搜索
                    // 获取当前页面中的内容,从body中提取
                    WebPageInfo info = getContent(tlink.getLinkURL());
                    if (info != null) {//if2
                        // 判断信息是否有效------------------------------------
                        //标题和内容中有一半以上的关键字，则为有效页面
                        if (isValidInfo(info)) {
                            String tname = getTblName();
                            info.setWebName(tlink.getLinkURL());
                            if(info.getWebName().equals("腾讯网")||info.getWebName().equals("其他网站")||info.getWebName().contains("RSS"))
                                info.setWebType("generalWeb");
                            else
                                info.setWebType("coreWeb");
                            try {
                                info.write(tname, info);//写入数据表，先查看数据表中是否已存在，然后再写
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }//endif2
                    //http://www.w3.org/1999/xhtml
                    //获得http://www.w3.org/1999/
                    String currentPageURI = getParentPath(tlink.getLinkURL());
                    // 寻找当前页面上的有效连接
                    String validuri = validUri(tlink.getLinkURL());
                    List<TargetLink> currentPageLinkResult = new ArrayList<TargetLink>();
                    try {
                        currentPageLinkResult = getLink(tlink.getLinkURL(), validuri);//提取当前网页中与name相关的链接
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (currentPageLinkResult != null) {//if3有相关链接
                        for (int k = 0; k < currentPageLinkResult.size(); k++) {//for2遍历找到的相关链接
                            TargetLink newLink = currentPageLinkResult.get(k);
                            if (newLink.getLinkURL().startsWith(".")//以“.”或“/”开始或者不以“http:”开始
                                    || newLink.getLinkURL().startsWith("/")
                                    || !newLink.getLinkURL()
                                    .startsWith("http:")) {
                                //置为有效链接，currentPageURI加上去掉开头“./”或开头“/”后的URL
                                newLink.setLinkURL(getValidURL(currentPageURI,newLink.getLinkURL()));
                            }
                            newLink.setDepth(tlink.getDepth() + 1);//搜索深度
                            if(newLink.getDepth() < this.upperDepth && count < MAX_DEPTH){//if4
                                cache.add(newLink);
                                count++;
                            }//endif4
                        }//endfor2
                        currentPageLinkResult.clear();
                        currentPageLinkResult = null;
                    }//endif3
                }//endif1
            }//endwhile2再进行下一次循环遍历cache
        }//endwhile1
        System.out.println(this.getName() + "结束任务");
        MDC.put("eventID", this.getParentMgr().getID());
        LogWriter.logger.info(this.getName() + "结束任务");
        running = false;
    }


    /**
     * 获得父节点路径
     * http://www.w3.org/1999/xhtml
     * 获得http://www.w3.org/1999/
     * @param url
     * @return
     */
    public String getParentPath(String url) {

        String res = url;
        int idx = url.lastIndexOf("/");
        if (idx <= "http://".length())//与http://比较长度
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

    /**
     * 获得有效URL
     * @param uri
     * @param url
     * @return
     */
    public String getValidURL(String uri, String url) {
        String u = url;
        if (u.startsWith("./"))//以“./”开头时候，去掉“./”
            u = u.substring("./".length());
        if (u.startsWith("/"))//以“/”开头时候，去掉“/”
            u = u.substring("/".length());
        return uri + u;//有效链接为uri加上去掉开头“./”或开头“/”后的url
    }

    /**
     * 判断是否为有效页面信息
     * @param info
     * @return
     */
    public Boolean isValidInfo(WebPageInfo info) {
        Boolean hit = false;
        int count = 0;
        for (int j = 0; j < this.parentMgr.getAddrwords().size(); j++) {//遍历此线程所在SearcherManager的关键字列表
            String name = this.parentMgr.getAddrwords().get(j).getWord();
            if (info.getPageContent().toLowerCase().contains(name.toLowerCase())
                    || info.getPageTitle().toLowerCase().contains(name.toLowerCase())) {//标题和内容中有关键字
                hit = true;
                count++;
            }
        }
        if (hit == true) {
            if (this.parentMgr.getEvtHandler() != null) {
                if (!this.parentMgr.getEvtHandler().catchEvtInfo(//判断是否与事件相关，比较name、location和关键字
                        info.getPageContent())
                        && !this.parentMgr.getEvtHandler().catchEvtInfo(
                        info.getPageTitle()))
                    hit = false;// 如果当前搜索器是事件对应搜索器 ，未在当前链接名称中找到事件相关词，忽略
            }
        }
        if (count < this.parentMgr.getAddrwords().size() / 3||info.getPageTitle()==null||info.getPageTitle().length()<18)//是否有一半以上的关键字，匹配一半以上的关键字则为有效
            return false;
        if(info.getPageTitle() != null && !info.getPageTitle().isEmpty())
            if (count - 1 < this.parentMgr.getAddrwords().size() / 3)
                hit = false;
            else if(count >= 1){
                for(int i = 0; i < this.getParentMgr().getKwords().size(); i++) {
                    String key = this.getParentMgr().getKwords().get(i).getWord();
                    if(info.getPageContent().toLowerCase().contains(key.toLowerCase())){
                        hit = true;
                    }
                }
            }
            else
                hit = false;
        return hit;
    }

    /**
     * 提取网页中与事件name相关的URL
     * @param tlink
     * @return 一个存放URL的List
     * @throws IOException
     */
    public List<TargetLink> getLink(TargetLink tlink) throws IOException {// 搜索与事件name相关的超链接
        try {
            String uri = validUri(tlink.getLinkURL());//提取有效URI

            List<TargetLink> list = getLink(tlink.getLinkURL(), uri);//提取页面中与事件name相关的URL
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setDepth(tlink.getDepth() + 1);//搜索深度
                }
            }
        } catch (Exception exp) {
            LogWriter.logger.info("getLink函数抛出IO异常");
        }
        return null;
    }

    /**
     * 提取有效URI
     * http://www.w3.org/1999/xhtml
     * 获得http://www.w3.org/1999/
     * @param url
     * @return
     */
    public String validUri(String url) {
        int idx = url.lastIndexOf("/");
        if (idx <= 0)//url中无“/”，提取父节点URI中是比较idx与”http://“的长度
            return url;
        String uri = url.substring(0, idx);
        if (!uri.endsWith("/"))
            uri = uri + "/";
        return uri;
    }

    /**
     * 检测URL指定的网页的字符集
     * @param url
     * @return 网页的实际编码方式
     */
    public String dectedEncode(String url) {
        String oriEncode = "utf-8,gb2312,gbk,iso-8859-1";//字符编码集合
        String[] encodes = oriEncode.split(",");
        for (int i = 0; i < encodes.length; i++) {
            if (dectedCode(url, encodes[i]))
                return encodes[i];

        }
        return "gbk";
    }

    /**
     * 编码匹配试探器
     * 不断去试探编码集合中的编码方式，直到得到正确结果
     * @param url
     * @param encode
     * @return
     */
    public static boolean dectedCode(String url, String encode) {
        try {
            Parser parser = new Parser(url);
            parser.setEncoding(encode);//设置编码格式
            for (org.htmlparser.util.NodeIterator e = parser.elements(); e.hasMoreNodes();) {
                Node node = (Node) e.nextNode();
                if (node instanceof Html || node instanceof BodyTag)
                    return true;
            }
        } catch (Exception e) {
            // LogWriter.logger.warn("HtmlParser jar包解析异常");
        }
        return false;
    }

    /**
     * 提取当前网页中的与name相关的URL
     * @param url
     * @param uri
     * @return
     * @throws IOException
     */
    public List<TargetLink> getLink(String url, String uri) throws IOException {// 搜索与name相关的超链接
        //String url1 = url;
        List<TargetLink> list = new ArrayList<TargetLink>();
        Parser parser = null;
        //NodeFilter filter = null;
        //调整url
        if (!url.startsWith("http")) {
            //如果以.或/开始，则取uri加上url中/后面的地址
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
        int num = 0;
        while (num<=10&&(nodelist == null || nodelist.size() == 0)) {
            parser.reset();
            nodelist = getLinks(parser);//得到链接列表
            num++;
        }
        if(nodelist!=null){
            for (int i = 0; i < nodelist.size(); i++) {//遍历得到的链接列表

                LinkTag tag = (LinkTag) nodelist.elementAt(i);
                //String tagInfo = tag.getLinkText();//得到link标签中的文本内容

                String hrefLink = tag.getAttribute("href");
                //System.out.println(i+" : "+tagInfo+"  "+hrefLink);
                if (hrefLink == null || hrefLink.equals(""))//链接地址为空
                    continue;
                if (hrefLink.contains("javascript:"))//链接地址为javascript
                    continue;
                Boolean hit = false;
                // 从当前管理器中检索词
                if (tag.getAttribute("href").indexOf("video") > 0)//链接为视频
                    continue;
                int count = 0;
                for (int j = 0; j < this.parentMgr.getAddrwords().size(); j++) {
                    String name = this.parentMgr.getAddrwords().get(j).getWord();
                    String tagName = tag.getLinkText();
                    byte[] bs = tagName.getBytes();
                    //String encode = dectedEncode(hrefLink);
                    //System.out.println(i+" : "+tagInfo+"  "+hrefLink+" encode is : "+encode);
                    //tagName = new String(bs, "GB2312");//tagName字符串编码转换为GB2312编码格式
//					if(encode != "none")
//						tagName = new String(bs, encode);
//					else
                    String encode = getCharSet(getMetaValue("http-equiv", "Content-Type", "content", parser),url);
                    tagName = new String(bs, encode);
                    if (tagName.contains(name)) {//包含关键词
                        hit = true;
                        count++;
                    }
                }

                if (hit == true) {// 在当前链接名称中找到了灾害相关词
//					if (this.parentMgr.getEvtHandler() != null) {
//						if (this.parentMgr.getEvtHandler().catchEvtInfo(tagInfo))//判断是否与事件相关
//							hit = true;// 如果当前搜索器是事件对应搜索器 ，未在当前链接名称中找到事件 相关词，忽略
//						else
//							continue;
//					}
                    TargetLink newTarget = new TargetLink();
                    //String str = tag.getStringText();
                    ////for test
                    int start = tag.getEndPosition ();
                    int end = tag.getEndTag().getStartPosition ();
                    if(end>start)
                        newTarget.setTitle(tag.getPage().getText(start,end));
                    newTarget.setLinkURL(tag.getAttribute("href"));

                    //System.out.println("list.add("+newTarget.getLinkURL()+" and "+newTarget.getTitle()+")");
                    list.add(newTarget);//将相关的链接放到队列中
                } else
                    continue;
            }
        }
        return list;
    }


    /**
     * 提取指定url的网页信息
     * 包括pageId,pageTitle,pageContent,url,pageTime,keyWords,Abstracts,webType,webName
     * 即WebPageInfo
     * @param url
     * @return 一个webPageInfo对象
     */
    public WebPageInfo getContent(String url) {

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
            parser.reset();
            Title = getTitle(parser);//得到title标签内容
            parser.reset();
            //获得meta标签中name=description的content值
            //<meta name="description" content="习近平:把权力关进制度的笼子里" />
            description = getMetaValue("name", "description", "content", parser);
            if (description == null || description.equals("")) {//没得到，再来一次
                parser.reset();
                description = getMetaValue("name", "Description", "content",
                        parser);
            }
            parser.reset();
            //获得meta标签中name=keywords的content值
            //<meta name="keywords" content="习近平" />
            keywords = getMetaValue("name", "keywords", "content", parser);
            if (keywords == null || keywords.equals("")) {
                parser.reset();
                keywords = getMetaValue("name", "Keywords", "content", parser);
            }
            parser.reset();
            newHtml = getBody(parser);
            String filterHtml = null;
            if (!newHtml.equals("")) {
                if (isValidContent(newHtml) || isValidContent(Title)) {
                    //为有效内容，则将各值赋给WebPageInfo对象相应的属性
                    result = new WebPageInfo();
                    result.setKeyWords(keywords);//keyWords
                    result.setAbstract(description);//Abstract
                    filterHtml = filterText(newHtml);//过滤文本
                    result.setPageContent(filterHtml);//pageContent
                    if (!isValidContent(result.getPageContent()))
                        result.setPageContent(Title + description + keywords);
                    result.setUrl(url);//url
                    result.setPageTitle(Title);//pageTitle
                    String strTime = null;
//					System.out.println("-------------------------------------"
//							+ filterHtml);
                    strTime = time.getTime(time.getAllTime(result, newHtml));
                    if (strTime == null || strTime.equals("")
                            || strTime.equals(" ")) {
                        result = null;
                    } else {
                        result.setPageTime(strTime);//pageTime
                    }
                } else
                    System.out.println("放弃:" + Title + "::" + url);
            } else
                System.out.println("放弃:" + Title + "::" + url);
            parser = null;
        } catch (ParserException e) {
            result = null;
        }
        return result;
    }

    /**
     * 是否为有效内容
     * @param content
     * @return
     */
    //2014年1月15日修改版本
    public boolean isValidContent(String info) {
        if (info == null)
            return false;
        int hitAddrCount = 0;
        //判断是否与事件相关，比较地点关键字
        for (int j = 0; j < this.getParentMgr().getAddrwords().size(); j++) {
            String name = this.getParentMgr().getAddrwords().get(j).getWord();
            if (info.toLowerCase().contains(name.toLowerCase())) {
                hitAddrCount++;
            }
        }
        //是否有一半以上的地点关键字匹配，匹配一半以上的地点关键字则为有效，否则如果地点关键字和预设关键字各至少有一个匹配则为有效
        int hitcount = this.getParentMgr().getAddrwords().size() / 2;
        if (hitAddrCount - 1 >= hitcount)
            return true;
        else if(hitAddrCount >= 1){
            for(int i = 0; i < this.getParentMgr().getKeys().size(); i++) {
                String key = this.getParentMgr().getKeys().get(i).getWord();
                if(info.toLowerCase().contains(key.toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 过滤标签“<tag>”以及“</tag>”中间的文本内容
     * <a href="http://www.news.cn/" target="_blank">新华网首页</a>
     * @param content
     * @param tag
     * @return
     */
    public String filterText(String content, String tag) {
        String result = content;
        while (true) {
            int idx = result.indexOf("<" + tag);
            if (idx < 0)
                break;
            String ss = result.substring(0, idx);
            result = result.substring(idx + 1);
            idx = result.indexOf(">");
            if (idx < 0)
                break;
            result = result.substring(idx + 1);
            idx = result.indexOf("</" + tag + ">");
            if (idx < 0)
                break;
            String tagEnd = "</" + tag + ">";
            result = result.substring(idx + tagEnd.length());

            result = ss + result;
        }
        return result;
    }

    public String filterText(Parser parser) throws ParserException{
        parser.reset();
        String result = new String();
        TextExtractingVisitor visitor = new TextExtractingVisitor();
        NodeFilter textFilter = new NodeClassFilter(TextNode.class);
        NodeList nodes = parser.extractAllNodesThatMatch(textFilter);
        for(int i = 0; i <nodes.size(); i++){
            TextNode textnode = (TextNode)nodes.elementAt(i);
            String line = textnode.toPlainTextString().trim();
            if(line.equals("")) continue;
            result = result + line;
        }
        parser.visitAllNodesWith(visitor);
        return getText(result);
    }

    /**
     * 过滤“<tag1>”以及“<tag2>”中间的文本内容
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
            return result;
        return content;
    }

    /**
     * 过滤给定字符串中的文本内容
     * 过滤掉style、a、A标签属性，提取出其中间文本内容及注释内容
     * @param content
     * @return
     */
    public String filterText(String content) {
        String result = content;
        result = result.replaceAll("SCRIPT", "script");
        result = result.replaceAll("Script", "script");
        result = filterText(result, "script");
        result = result.replaceAll("STYLE", "style");
        result = result.replaceAll("Style", "style");
        result = filterText(result, "style");//过滤<style></style>中间的内容
        result = filterText(result, "a");//过滤<a></a>中间的内容
        result = filterText(result, "A");//过滤<A></A>中间的内容
        result = filterText(result, "<!--", "-->");//过滤<<!--><-->>中间的内容
        result = filterText(result,"<",">");
        return getText(result);
    }

    /**
     * 提取指定url中的tag标签网页信息以及值为tag_value的attName标签网页信息
     * 包括pageId,pageTitle,pageContent,url,pageTime,keyWords,Abstracts,webType,webName
     * 即WebPageInfo
     * @param url
     * @return 一个webPageInfo对象
     */
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
            parser.reset();
            Title = getTitle(parser);// visitor.getTitle() ;
            parser.reset();
            //<meta name="Description" content="习近平：反腐败是实现“中国梦”前提">
            //找出content具体内容
            description = getMetaValue("name", "description", "content", parser);
            if (description == null || description.equals("")) {
                parser.reset();
                description = getMetaValue("name", "Description", "content",
                        parser);
            }
            parser.reset();
            //获得meta标签中name=keywords的content值
            //<meta name="keywords" content="习近平" />
            keywords = getMetaValue("name", "keywords", "content", parser);
            if (keywords == null || keywords.equals("")) {
                parser.reset();
                keywords = getMetaValue("name", "Keywords", "content", parser);
            }
            parser.reset();
            AndFilter filter_div = new AndFilter(new TagNameFilter(tag),
                    //TagNameFilter接受所有tag标签
                    new HasAttributeFilter(attName, tag_value));
            //HasAttributeFilter接受值为tag_value的attName标签
            NodeList nodes = parser.parse(filter_div);
            if (nodes != null) {
                NodeFilter filter_p = new TagNameFilter("p");//p标签
                nodes = nodes.extractAllNodesThatMatch(filter_p, true);
                //抽取出所有符合filter_p过滤器的结点
                //用filter_p过滤器来过滤nodes列表
                newHtml = nodes.toHtml();//将结点列表转换为对应的html
                String filterHtml = null;
                if (!newHtml.equals("")) {
                    result = new WebPageInfo();
                    result.setKeyWords(keywords);//keyWords
                    result.setAbstract(description);//Abstract
                    filterHtml = getText(newHtml);
                    result.setPageContent(filterHtml);//pageContent
                    if (!isValidContent(result.getPageContent()))
                        result.setPageContent(Title + description + keywords);
                    result.setUrl(url);//url
                    result.setPageTitle(Title);//pageTitle
                    result.setKeyWords(result.getKeyWords().replaceAll(
                            this.myTemplate.getName(), ""));
                    result.setAbstract(result.getAbstract().replaceAll(
                            this.myTemplate.getName(), ""));
                    result.setPageContent(result.getPageContent().replaceAll(
                            this.myTemplate.getName(), ""));
                    result.setPageTitle(result.getPageTitle().replaceAll(
                            this.myTemplate.getName(), ""));

                    String strTime = null;
                    parser.reset();
                    ArrayList<String> strList = new ArrayList<String>();
                    strList = time.getAllTime(result, parser.parse(new NodeClassFilter()).toHtml());
                    if (strList != null) {
                        strTime = time.getTime(strList);
                        if (strTime == null || strTime.equals("")
                                || strTime.equals(" ")) {
                            result = null;
                        } else {
                            result.setPageTime(strTime);//pageTime
                        }
                    }
                    //System.out.println("page:" + Title);
                } else {
                    result = getContent(url);
                }// System.out.println("放弃:" +Title + "::" + url ) ;
            } else {
                result = getContent(url);
            }// System.out.println("放弃:" +Title + "::" + url ) ;
            nodes = null;
            filter_div = null;
            parser = null;
        } catch (ParserException e) {
            result = null;
        }
        return result;
    }

    /**
     * 获得url中tag标签主题
     * @param url
     * @param tag
     * @return
     */
    public String getTitle(String url, String tag) {
        NodeFilter filter_t = new TagNameFilter(tag);//接受所有tag标签
        String title = null;
        Parser parser;
        try {
            parser = new Parser(url);
            NodeList nodes = parser.parse(filter_t);
            title = nodes.toString();
        } catch (ParserException e) {
            MDC.put("eventID", this.getParentMgr().getID());
            LogWriter.logger.warn("HtmlParser jar包解析异常");
            e.printStackTrace();
        }
        return title;
    }

    /**
     * 计算s中有几个c子串
     * @param s
     * @param c
     * @return
     */
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

        res.replaceAll("&ensp;", "");
        res.replaceAll("&ensp;", "");
        res.replaceAll("&thinsp;", "");
        res.replaceAll("&zwnj;", "");
        res.replaceAll("&zwj;", "");
        res.replaceAll("&lrm;", "");
        res.replaceAll("&rlm;", "");
        res.replaceAll("&mdash;", "");
        res.replaceAll("&raquo;", "");

        res = res.replace("\n", "");
        res = res.replace("\r", "");
        res = res.replace("\t", "");
        res = res.replace("  ", " ");

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
}
