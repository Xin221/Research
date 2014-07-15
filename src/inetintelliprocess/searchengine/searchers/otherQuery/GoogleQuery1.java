package inetintelliprocess.searchengine.searchers.otherQuery;

import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.util.Config;
import inetintelliprocess.util.LogWriter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GoogleQuery1 {

    private final static String LICENCES_KEY = Config.read("LICENCES_KEY");//Google许可证键
    private final static String Project_ID = Config.read("Project_ID");
    private ArrayList<String> queryXml;//查询字符串
    private ArrayList<String> queryAddress;
    private int queryTimes = 5;
    private final static int GOOGLESUM = 100;

    public GoogleQuery1(ArrayList<String> strXml, ArrayList<String> strAddress){
        queryXml = strXml;
        queryAddress = strAddress;
    }

    public GoogleQuery1(){
        queryXml = new ArrayList<String>();
        queryAddress = new ArrayList<String>();
    }



    public synchronized int getTimes(){
        return queryTimes;
    }

    public synchronized void setTimes(int t){
        this.queryTimes = t;
    }

    public void addQueryXml(String str){
        this.queryXml.add(str);
    }

    public void addQueryAddress(String str){
        this.queryAddress.add(str);
    }

    public void search(String tname){
        String query = null;
        try {
            String str = null;
            if(queryXml!=null&&!queryXml.isEmpty()){
                for(int i=0;i<queryXml.size()-1;i++){
                    if(queryXml.get(i)!=null&&!queryXml.get(i).isEmpty())
                        if(str==null||str.isEmpty())
                            str = queryXml.get(i)+"|";
                        else
                            str = str+queryXml.get(i)+"|";
                }
                if(queryXml.get(queryXml.size()-1)!=null&&!queryXml.get(queryXml.size()-1).isEmpty())
                    if(str==null||str.isEmpty())
                        str = queryXml.get(queryXml.size()-1)+" ";
                    else
                        str = str+queryXml.get(queryXml.size()-1)+" ";
            }
            if(queryAddress!=null&&!queryAddress.isEmpty()){
                for(int i=0;i<queryAddress.size()-1;i++){
                    if(queryAddress.get(i)!=null&&!queryAddress.get(i).isEmpty())
                        if(str==null||str.isEmpty())
                            str = queryAddress.get(i) + "|";
                        else
                            str = str + queryAddress.get(i) + "|";
                }
                if(queryAddress.get(queryAddress.size()-1)!=null&&!queryAddress.get(queryAddress.size()-1).isEmpty())
                    if(str==null||str.isEmpty())
                        str = queryAddress.get(queryAddress.size()-1);
                    else
                        str = str+queryAddress.get(queryAddress.size()-1);
            }
            System.out.println("要查询谷歌的关键语句是："+str);
            query = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(query!=null){
            for (int i = 0; i < 2&&this.getTimes()<=GOOGLESUM; i++) {
                //            makeQuery("http://ajax.googleapis.com/ajax/services/search/web?start=" + i * 3 + "&rsz=large&v=1.0&alt=atom&q=" + query,tname);
                makeQuery("https://www.googleapis.com/customsearch/v1?q=" + query+"&cx="+Project_ID+"&alt=atom&dateRestrict=y1&start="+(i*10+1)+"&key="+LICENCES_KEY,tname);

            }
            this.setTimes(this.getTimes()+5);
        }
    }

    private synchronized void makeQuery(String query,String tname) {
        try {
            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            System.out.println("query is : "+query);
            GetMethod getMethod = new GetMethod(query);

            getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
            getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            //httpClient.executeMethod(getMethod); 
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod.getStatusLine());
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(getMethod.getResponseBodyAsStream());
            NodeList nlist = doc.getElementsByTagName("entry");//doc.getChildNodes() ;
            for(int i = 0 ; i < nlist.getLength() ; i ++) {
                Element cn =(Element) (nlist.item(i));
                if(cn == null)
                    continue;
                insertInfoFromAtom(cn,tname);
            }

        } catch (Exception e) {
            System.out.println("Google搜索服务访问失败。");
            MDC.put("eventID", tname);
            LogWriter.logger.warn("Google搜索服务访问失败。");

        }
    }

    //得到xml中node中的元素
    public boolean insertInfoFromAtom(Element xmlNode,String tname) {
        if(!xmlNode.getNodeName().equals("entry"))
            return false ;
        WebPageInfo info = new WebPageInfo();
        NodeList nlist = xmlNode.getChildNodes();
        String time = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(nlist == null)
            return true ;
        for(int i = 0 ; i < nlist.getLength() ; i ++) {
            Node cn = nlist.item(i) ;
            if(cn == null ) continue ;
            if(cn.getNodeName().equals("id")) {
                info.setUrl(cn.getTextContent()) ;
                info.setWebName("Google-"+cn.getTextContent());
                if(info.getWebName().equals("Google-腾讯网")||info.getWebName().equals("Google-其他网站")||info.getWebName().contains("RSS"))
                    info.setWebType("generalWeb");
                else
                    info.setWebType("coreWeb");
            }else if(cn.getNodeName().equals("title")) {
                info.setPageTitle(cn.getTextContent());

            }else if(cn.getNodeName().equals("summary")) {
                info.setAbstract(cn.getTextContent());
                info.setPageContent(cn.getTextContent());
            }else if(cn.getNodeName().equals("updated")) {
                time = df.format(new inetintelliprocess.searchengine.searchers.Time().formateDate(cn.getTextContent()));
            }
            String time1 = new inetintelliprocess.searchengine.searchers.Time().getTime(
                    new inetintelliprocess.searchengine.searchers.Time().getAllTime(info, xmlNode.getNodeValue()));
            if(time!=null&&!time.equalsIgnoreCase("null"))
                info.setPageTime(time);
            else if(time1!=null&&!time1.equalsIgnoreCase("null"))
                info.setPageTime(time1);
            else
                info.setPageTime(df.format(new Date()));
        }
        try {
            info.write(tname, info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true ;
    }



    public static void main(String[] args){
        ArrayList<String> str = new ArrayList<String>();
        ArrayList<String> strl = new ArrayList<String>();
        str.add("地震");
        str.add("earthquake");
        strl.add("海地");
        GoogleQuery1 google = new GoogleQuery1(str,strl);
        google.search("generalwebinfotbl");

    }



}
