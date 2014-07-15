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

public class BaiduQuery extends abasicQuery{

    private String queryPath = "http://www.baidu.com/s?tn=ichuner&lm=-1&rn=100&word=";
    //获得百度的搜索页面，前100个搜索结果
    public String getHTML(String queryPath,List<KeyWord> keyList) throws IOException
    {
        return super.getHTML(queryPath, keyList);
    }
    //对HTML进行析取，析取出100个URL、标题和摘要
    public List<WebPageInfo> parseHTML(String tname,List<KeyWord> keyList)
    {
        List<WebPageInfo> list = new ArrayList<WebPageInfo>();
        String page=null;
        try
        {
            page=getHTML(queryPath,keyList);
            //System.out.println("page is :\n"+page);
        }
        catch(Exception ex)
        {
            MDC.put("eventID", tname);
            LogWriter.logger.warn("百度搜索服务访问失败。");
            return null;
        }
        Parser parser = Parser.createParser(page,"utf-8");

        AndFilter filter_div = new AndFilter(new TagNameFilter("div"),
                //TagNameFilter接受所有tag标签
                new HasAttributeFilter("id", "content_left"));
        //HasAttributeFilter接受值为tag_value的attName标签
        NodeList nodes,cnodes,nodes_abstract,nodes_time;
        try {
            nodes = parser.parse(filter_div);
            //System.out.println("nodes size is : "+nodes.size());
            for(int i=0;i<nodes.size();i++ ){
                cnodes = nodes.elementAt(i).getChildren();
                //System.out.println("cnodes size is : "+cnodes.size());
                String result;
                for(int j=1;j<cnodes.size();j++){
                    WebPageInfo info = new WebPageInfo();
                    result = cnodes.elementAt(j).toHtml();
                    //System.out.println("result is \n"+result);
                    if(result==null||result.isEmpty())
                        continue;
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
                    AndFilter filter_abstract = new AndFilter(new TagNameFilter("div"),
                            //TagNameFilter接受所有tag标签
                            new HasAttributeFilter("class", "c-abstract"));
                    nodes_abstract = parser.parse(filter_abstract);
                    for(int k=0;k<nodes_abstract.size();k++){
                        String nod = nodes_abstract.elementAt(k).toHtml();
                        info.setAbstract(info.filterText(nod, "<", ">"));
                        if(info.getAbstract()==null||info.getAbstract().isEmpty())
                            info.setAbstract(info.getPageTitle());
                        //System.out.println("page_abstract is "+info.getAbstract());
                    }
                    info.setPageContent(info.getPageTitle()+":"+info.getAbstract());
                    info.setWebName(info.getUrl());
                    info.setWebType("generalweb");
                    parser.reset();
                    nodes_time = parser.parse(new AndFilter(new TagNameFilter("span"),new HasAttributeFilter("class","g")));
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

    public static void main(String[] args) {
//		
//			BaiduQuery test = new BaiduQuery();
//			//List<WebPageInfo> str = test.parseHTML("N34400E07360020051008104847","巴基斯坦地震");
//		   try {
//			WebPageInfo.write("N34400E07360020051008104847", str);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		   for(int i=0;i<str.size();i++) {
//			   WebPageInfo info = str.get(i);
//			   System.out.println("第"+(i+1)+"条结果：");
//			   System.out.println("URL:"+info.getUrl());
//			   System.out.println("标题:"+info.getPageTitle());
//			   System.out.println("摘要:"+info.getAbstract());
//		   }
    }


}


