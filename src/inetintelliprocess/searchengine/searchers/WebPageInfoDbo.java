package inetintelliprocess.searchengine.searchers;
/**
 * searchers搜索核心包
 * 搜索实现
 */
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.util.LogWriter;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.MDC;
/**
 *
 * 搜索层的业务类
 * 主要负责网页web信息的数据库存储和访问工作
 * 事件数据表的操作
 */
public class WebPageInfoDbo {

    public synchronized static boolean write(String tname,WebPageInfo info){
        if(info == null )
            return false;
        boolean ret = false;
        int write = -1;
        String ptitle = info.getPageTitle();
        ptitle =  ptitle.replaceAll("[?]?", "");
        ptitle = ptitle.replaceAll("\"", "");
        ptitle = ptitle.replaceAll("\'", "");
        String sql = " INSERT INTO "+tname+" (pageId,pageTitle,pageTime,pageContent,url,keyWords,Abstract,lastWriteTime,webType,webName,sendTo)VALUES( " ;
        sql += "'"+info.getPageId()+"', ";
        if(ptitle.length()>=100)
            sql += "'"+ptitle.substring(0, 99)+"', ";
        else
            sql += "'"+ptitle+"', ";
        sql += "'"+info.getPageTime()+"', ";
        String content = info.getPageContent().replaceAll("[?]?", "");
        content = content.replace("\"", "");
        content = content.replace("[']", "");
        content = content.replace("\'", "");
        if(content.length()>2048) content = content.substring(0, 2048);
        sql += "'"+content +"', ";
        sql += "'"+info.getUrl() +"', ";
        String abstr = info.getAbstract().replaceAll("[?]?", "");
        abstr = abstr.replace("\"", "");
        abstr = abstr.replace("[']", "");
        abstr = abstr.replace("\'", "");
        if(info.getKeyWords()!=null)
            sql += "'"+info.getKeyWords().replaceAll("[?]", "") +"', ";
        else
            sql +="'',";
        if(abstr.length()>=600)
            sql += "'"+abstr.substring(0, 599) +"', ";
        else
            sql += "'"+abstr +"', " ;
        sql += "'"+new Timestamp(new Date().getTime()).toString() +"', ";
        sql += "'"+info.getWebType() +"', " ;
        sql += "'"+info.getWebName() +"', " ;
        sql += "'"+info.getSendTo() +"') " ;
        sql = sql.replace("\n", "");
        sql = sql.replace("\r","");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+sql);
        try {
            write = DBConnect.excuteUpdate(sql);
            if(write > 0){
                ret = true;
                if(tname=="generalWebInfoTbl")
                    MDC.put("eventID", "日常搜索");
                else
                    MDC.put("eventID", tname);
                LogWriter.logger.info("WebPageInfo 写入数据库成功");
            }
            else if(write == 0){
                ret = true;
                if(tname=="generalWebInfoTbl")
                    MDC.put("eventID", "日常搜索");
                else
                    MDC.put("eventID", tname);
                LogWriter.logger.info("数据库"+tname+"中已经存在此WebPageInfo");
            }
        } catch (SQLException e) {
            if(tname=="generalWebInfoTbl")
                MDC.put("eventID", "日常搜索");
            else
                MDC.put("eventID", tname);
            LogWriter.logger.info("WebPageInfo 写入数据库"+tname+"失败");
            e.printStackTrace();
        }
        return ret;
    }


    public synchronized static boolean write(String tname,List<WebPageInfo> infoList){
        if(infoList == null )
            return false;
        boolean ret = false;
        WebPageInfo info = null;
        String ptitle = null;
        String content = null;
        String abstr = null;
        String[][] params = new String[infoList.size()][11];
        String sql = " INSERT INTO "+tname+" (pageId,pageTitle,pageTime,pageContent,url,keyWords,Abstract,lastWriteTime,webType,webName,sendTo)VALUES(?,?,?,?,?,?,?,?,?,?,?) " ;
        for(int i = 0; i<infoList.size();i++){
            info = infoList.get(i);

            ptitle = info.getPageTitle();
            content = info.getPageContent();
            if(info.getAbstract()!=null&&!info.getAbstract().isEmpty())
                abstr = info.getAbstract();
            else
                abstr = info.getPageTitle();
            params[i][0] = info.getPageId();
            params[i][1] = ptitle.length()>=100?ptitle.substring(0,99):ptitle;
            params[i][2] = info.getPageTime();
            params[i][3] = content;
            params[i][4] = info.getUrl();
            params[i][5] = info.getKeyWords()!=null?info.getKeyWords():"";
            params[i][6] = abstr.length()>=600?abstr.substring(0,599):abstr;
            params[i][7] = new Timestamp(new Date().getTime()).toString();
            params[i][8] = info.getWebType();
            params[i][9] = info.getWebName();
            params[i][10] = String.valueOf(info.getSendTo());
        }
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+sql);
        try {
            DBConnect.batch(sql,params);
            ret = true;
            if(tname=="generalWebInfoTbl")
                MDC.put("eventID", "日常搜索");
            else
                MDC.put("eventID", tname);
            LogWriter.logger.info("WebPageInfo 写入数据库成功");
        } catch (SQLException e) {
//			if(tname=="generalWebInfoTbl")
//				MDC.put("eventID", "日常搜索");
//			else
//				MDC.put("eventID", tname);
//			LogWriter.logger.info("WebPageInfo 写入数据库"+tname+"失败");
        }
        return ret;
    }



    public synchronized boolean isExistUrl(String tname, String url, String pageTitle) {
        boolean flag = false;
        String sql = "SELECT count(*) FROM " + tname + " WHERE url='" + url
                + "'or pageTitle='"+ pageTitle +"'";
        //如果执行失败，需要处理异常
        try {
            if(DBConnect.stat(sql)>0)
                flag = true;
        } catch (SQLException e) {
            if(tname.equals("generalwebinfotbl"))
                MDC.put("eventID","日常搜索");
            else
                MDC.put("eventID",tname);
            LogWriter.logger.error("查询数据表"+tname+"中是否存在url"+url+"失败");
            e.printStackTrace();
        }
        return flag;
    }

    public static synchronized boolean setSent(String tname, String pageId){
        boolean flag = false;
        String sql = " UPDATE "+tname+" set sendTo='1' where pageId='"+pageId+"'";
        System.out.println("sql : "+sql);
        try {
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (SQLException e) {
            if(tname.equals("generalwebinfotbl"))
                MDC.put("eventID","日常搜索");
            else
                MDC.put("eventID",tname);
            LogWriter.logger.error("置sendTo位错误");
            e.printStackTrace();
        }
        return flag;
    }

}
