package inetintelliprocess.searchengine.searchers.otherQuery;

import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.searchengine.searchers.Time;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.util.LogWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.MDC;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class SoQuery extends abasicQuery{

    //private String queryPath = "https://www.google.com.hk/#ewwindow=1&safe=strict&key=AIzaSyBmIvpKyk-krwgSLzzBIvQo8i06QPy9KeE&cx=010269875954050322502:5c_6sgxczmi&q=";
    private String queryPath = "http://www.so.com/s?ie=utf-8&src=360sou_home&q=";

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }

    public String getHTML(List<KeyWord> keyList) throws IOException
    {
        return super.getHTML(queryPath, keyList);
    }

    public List<WebPageInfo> parseHTML(String tname,List<KeyWord> key){
        List<WebPageInfo> result = new ArrayList<WebPageInfo>();
        List<WebPageInfo> temp = new ArrayList<WebPageInfo>();
        for(int i=1;i<=5;i++){
            setQueryPath("http://www.so.com/s?ie=utf-8&src=360sou_home&pn="+i+"&q=");
            temp = toParseHTML(tname,key);
            if(temp!=null)
                result.addAll(temp);
        }
        return result;
    }

    public List<WebPageInfo> toParseHTML(String tname,List<KeyWord> key)
    {
        List<WebPageInfo> list = new ArrayList<WebPageInfo>();
        String page=null;
        try
        {
            page=getHTML(key);
            //System.out.println("page is :\n"+page);
        }
        catch(Exception ex)
        {
            MDC.put("eventID", tname);
            LogWriter.logger.warn("360搜索服务访问失败。");
            return null;
        }
        Parser parser = Parser.createParser(page,"utf-8");

        AndFilter filter_div = new AndFilter(new TagNameFilter("ul"),
                //TagNameFilter接受所有tag标签
                new HasAttributeFilter("class", "result"));
        //HasAttributeFilter接受值为tag_value的attName标签
        NodeList nodes,cnodes,nodes_abstract,nodes_time;
        try {
            nodes = parser.parse(filter_div);
            //System.out.println("nodes size is : "+nodes.size());
            for(int i=0;i<nodes.size();i++ ){
                //到达所有的<ul class="result">
                cnodes = nodes.elementAt(i).getChildren();
                //System.out.println("cnodes size is : "+cnodes.size());
                String result;
                for(int j=1;j<cnodes.size();j++){
                    //到达<li class="res-list">（每一个搜索结果条目）
                    WebPageInfo info = new WebPageInfo();
                    result = cnodes.elementAt(j).toHtml();
                    if(result==null||result.isEmpty())
                        continue;
                    //System.out.println("result is \n"+result);
                    String reg_URL="href ?= ?\"(.*?)\"";
                    Pattern pattern_URL=Pattern.compile(reg_URL);
                    Matcher matcher_URL=pattern_URL.matcher(result);
                    String page_URL=null;
                    if(matcher_URL.find())
                    {
                        page_URL=matcher_URL.group().toString().replaceAll("href ?= ?\"", "").replaceAll("\"", "");
                        info.setUrl(page_URL);
                        //System.out.println("page_URL is "+info.getUrl());
                    }
                    String reg_title="<a.+?href\\s*=\\s*[\"]?(.+?)[\"|\\s].+?>(.+?)</a>";
                    Pattern patter_title=Pattern.compile(reg_title);
                    Matcher matcher_title=patter_title.matcher(result);
                    String page_title=null;
                    if(matcher_title.find())
                    {
                        //得到了标题
                        page_title=matcher_title.group().toString();
                        info.setPageTitle(info.filterText(page_title, "<", ">"));
                        //System.out.println("page_title is "+info.getPageTitle());
                    }

                    parser = Parser.createParser(result, "utf-8");
                    AndFilter filter_abstract = new AndFilter(new TagNameFilter("p"),
                            //TagNameFilter接受所有tag标签
                            new HasAttributeFilter("class", "res-desc"));
                    nodes_abstract = parser.parse(filter_abstract);
                    for(int k=0;k<nodes_abstract.size();k++){
                        String nod = nodes_abstract.elementAt(k).toHtml();
                        info.setAbstract(info.filterText(nod, "<", ">"));
                    }
                    if(info.getAbstract()==null||info.getAbstract().isEmpty())
                        info.setAbstract(info.getPageTitle());
                    //System.out.println("page_abstract is "+info.getAbstract());
                    info.setPageContent(info.getPageTitle()+":"+info.getAbstract());
                    info.setWebName(info.getUrl());
                    info.setWebType("generalweb");
                    parser = Parser.createParser(result, "utf-8");
                    nodes_time = parser.parse(new AndFilter(new TagNameFilter("p"),new HasAttributeFilter("class","res-linkinfo ")));
                    //System.out.println("nodes_time size is "+nodes_time.size());
                    for(int t=0;t<nodes_time.size();t++){
                        String str;
                        ArrayList<String> strList = new ArrayList<String>();
                        Time time = new Time();
                        //System.out.println("nodes_time is "+nodes_time.elementAt(t).toHtml());
                        strList = time.getAllTime(info, nodes_time.elementAt(t).toHtml());
                        if (strList != null) {
                            //System.out.println("strList size is "+strList.size());
                            str = time.getTime(strList);
                            //System.out.println("str is "+str);
                            if (str == null || str.equals("")
                                    || str.equals(" ")) {
                                info = null;
                            } else {
                                info.setPageTime(str);//pageTime
                                //System.out.println("page_time is "+str);
                            }
                        }
                    }
                    if(info!=null&&info.getUrl()!=null&&info.getPageTitle()!=null&&info.getPageTime()!=null){
                        list.add(info);

                    }
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return list;
    }


    public boolean search(String tname, List<KeyWord> keyList){
        List<WebPageInfo> str = parseHTML(tname, keyList);
        boolean flag = true;
        try {
            flag = WebPageInfo.write(tname, str);
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SoQuery test = new SoQuery();
        List<KeyWord> keyList = new ArrayList<KeyWord>();
        keyList.add(new KeyWord("巴基斯坦"));
        List<WebPageInfo> str = test.parseHTML("n34400e07360020051008104847",keyList);

        for(int i=0;i<str.size();i++) {
            WebPageInfo info = str.get(i);
            System.out.println("第"+(i+1)+"条结果：");
            System.out.println("URL:"+info.getUrl());
            System.out.println("标题:"+info.getPageTitle());
            System.out.println("摘要:"+info.getAbstract());
        }
    }

}
