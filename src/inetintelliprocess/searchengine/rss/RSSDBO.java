package inetintelliprocess.searchengine.rss;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.util.LogWriter;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.MDC;

public class RSSDBO {

    public synchronized List<RSSLinkInfo> loadRSSInfos() {
        String sql = null;
        List<RSSLinkInfo> result = new ArrayList<RSSLinkInfo> () ;
        try {
            sql = "select * from rsslinkinfo order by ID";
            result = DBConnect.excuteQuery(RSSLinkInfo.class, sql);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result ;
    }

    public synchronized static boolean setRssExtractTypeDB(RSSLinkInfo link,boolean tag){

        String sql = null;
        sql = "update rsslinkinfo set rssExtractType="+String.valueOf(tag).toUpperCase()+" where ID="+link.getID()+";";
        System.out.println(sql);
        try {
            if(DBConnect.excuteUpdate(sql)>=0){
                link.setRssExtractType(String.valueOf(tag));
                return true;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }

    public synchronized static boolean insertRSSPageInfo (String tname, RSSPageInfo info){
        boolean flag = false;
        String sql = null;
        if(info == null )
            return false;
        try {
            sql = " INSERT INTO "+tname+" (pageId,pageTitle,pageTime,pageContent,url,keyWords,Abstract,lastWriteTime,webType,webName,sendTo)VALUES( " ;
            sql += "'"+info.getPageId()+"', " ;
            String ptitle = info.getPageTitle()  ;
            ptitle =  ptitle.replaceAll("[?]", "") ;
            ptitle = ptitle.replaceAll("\"", "") ;
            ptitle = ptitle.replaceAll("\'", "") ;
            if(ptitle.length()>=100)
                ptitle = ptitle.substring(0, 99) ;
            sql += "'"+ptitle+"', " ;
            sql += "'"+info.getPageTime().toString()+"', " ;
            String content = info.getPageContent().replaceAll("[?]", "") ;
            content = content.replace("\"", "") ;
            content = content.replace("[']", "") ;
            content = content.replace("\'", "") ;
            sql += "'"+content +"', " ;
            sql += "'"+info.getUrl() +"', " ;
            sql += "'"+info.getKeyWords().replaceAll("[?]", "")+"', " ;
            String abstr = info.getAbstract().replaceAll("[?]", "");
            abstr = abstr.replace("\"", "") ;
            abstr = abstr.replace("[']", "") ;
            abstr = abstr.replace("\'", "") ;
            if(abstr.length()>=600)
                abstr = abstr.substring(0, 599);
            sql += "'"+abstr+"', " ;
            sql += "'"+new Timestamp(new Date().getTime()).toString() +"', " ;
            //修改rss源
            sql += "'"+info.getWebType() +"', " ;
            sql += "'"+info.getWebName() +"','0') " ;
            sql = sql.replaceAll("\n", "");
            sql = sql.replaceAll("\r","");
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID",tname);
            LogWriter.logger.warn("RSS信息插入文本对象信息表异常");
            e.printStackTrace();
        }
        return flag;
    }

    public static synchronized boolean isExistUrl(String tname, String url) {
        boolean flag = false;
        String sql = null;
        try {
            sql = "SELECT count(*) FROM " + tname + " WHERE url='" + url + "' and webName like '%RSS%'";
            if(DBConnect.stat(sql)>0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID",tname);
            LogWriter.logger.warn("RSS全文链接存在判断异常");
            e.printStackTrace();
        }
        return flag;
    }


    @SuppressWarnings("null")
    public synchronized List<String> getKeyWords(EventInfo eventInfo){
        String sql = null;
        List<String> result = null;
        String name = null;
        try {
            if(eventInfo.getEventID() != ""){
                sql = "SELECT eventLocation FROM eventinfo where eventID = '"+eventInfo.getEventID()+"'";
                name = DBConnect.excuteReadOne(sql);
                KeyWord KWord = new KeyWord();
                List<String> word = KWord.multiRegionInfoString(name);
                result.addAll(word);
            }
        }  catch (Exception e) {
            MDC.put("eventID",eventInfo.getEventID());
            LogWriter.logger.warn("eventinfo加载异常");
            e.printStackTrace();
        }
        return result ;
    }

    public static void main(String[] args){
        List<RSSLinkInfo> rssinfos = new ArrayList<RSSLinkInfo>();
        RSSDBO rssdbo = new RSSDBO();
        rssinfos = rssdbo.loadRSSInfos();
        for(RSSLinkInfo rssinfo : rssinfos) {
            System.out.println(rssinfo.getID());
            System.out.println(rssinfo.getRssLinkURL());
            System.out.println(rssinfo.getRssWebType());
            System.out.println(rssinfo.getRssWebName());
            System.out.println(rssinfo.getRssExtractType());
            System.out.println("***************************************************");
            RSSDBO.setRssExtractTypeDB(rssinfo,false);
        }
    }

}
