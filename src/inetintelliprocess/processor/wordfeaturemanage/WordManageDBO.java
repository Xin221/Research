package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.util.LogWriter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;


public class WordManageDBO {


    public synchronized List<WebPageInfo> loadtPageInfos(String tblName, String workTimeStartString) {
        String sql = null;
        List<WebPageInfo> result = new ArrayList<WebPageInfo> () ;
        try {
//            sql = "select pageId, pageTitle, pageTime, pageContent, url, lastWriteTime, Abstract, webName from "+tblName+" " +
//                         "where lastWriteTime > '" + workTimeStartString + "' order by lastWriteTime asc";
            if(workTimeStartString == null || workTimeStartString.isEmpty())
                sql = "select pageId, pageTitle, pageTime, pageContent, url, lastWriteTime, Abstract, webName from "+tblName+" " +
                        "order by lastWriteTime asc";
            else
                sql = "select pageId, pageTitle, pageTime, pageContent, url, lastWriteTime, Abstract, webName from "+tblName+" " +
                        "where lastWriteTime > '" + workTimeStartString + "' order by lastWriteTime asc";
            result = DBConnect.excuteQuery(WebPageInfo.class, sql);
        }  catch (Exception e) {
            if(!tblName.equals("generalwebinfotbl"))
                MDC.put("eventID", tblName);
            else
                MDC.put("eventID", "日常搜索");
            LogWriter.logger.warn("分析器:" + tblName + "文本对象加载异常");
            e.printStackTrace();
        }
        return result ;
    }

    public synchronized boolean createWordInfoTable(String tblName) {
        if( DbTools.isTableExist(tblName)) {
            if(DbTools.clearTable(tblName))
                return true;
        }
        boolean flag = false;
        String sql = null;
        try {
            sql="CREATE TABLE "+tblName+" " +
                    "(pageId VARCHAR(100) NOT NULL, word VARCHAR(30) NOT NULL,count float NOT NULL,inserttime datetime," +
                    "ID VARCHAR(300) NOT NULL, eventID VARCHAR(300),pagetitle VARCHAR(300) NOT NULL,pageurl VARCHAR(250) NOT NULL," +
                    "PRIMARY KEY (pageurl,word)) ENGINE=InnoDB charset utf8 collate utf8_general_ci";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID", tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:创建词频统计信息表" + tblName + "异常");
            e.printStackTrace();
        }
        return flag ;
    }

    public synchronized boolean createPageCountTable(String tblName) {
        if( DbTools.isTableExist(tblName)) {
            if(DbTools.clearTable(tblName))
                return true;
        }
        boolean flag = false;
        String sql = null;
        try {
            sql = "CREATE TABLE "+tblName+" (id VARCHAR(100) PRIMARY KEY, " +
                    "word VARCHAR(100) NOT NULL,pageCount float NOT NULL,createTime datetime," +
                    "wordType VARCHAR(20),validateTime datetime) charset utf8 collate utf8_general_ci";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID", tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:创建篇频统计信息表" + tblName + "异常");
            e.printStackTrace();
        }
        return flag ;
    }

    public synchronized boolean createSpaceInfoTable(String tblName) {
        if( DbTools.isTableExist(tblName)) {
            if(DbTools.clearTable(tblName))
                return true;
        }
        boolean flag = false;
        String sql = null;
        try {
            sql = "CREATE TABLE "+tblName+" (id VARCHAR(100) PRIMARY KEY, word VARCHAR(100) NOT NULL," +
                    "count float NOT NULL,createTime datetime,lastUpdateTime datetime,isThemeword VARCHAR(10)) charset utf8 collate utf8_general_ci";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID", tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:创建空间特征向量信息表" + tblName + "异常");
            e.printStackTrace();
        }
        return flag ;
    }

    public synchronized NonRegularWord loadWord(String tblName, String word) {
        String sql = null;
        NonRegularWord result = null;
        try {
            sql = "select id, word, pageCount, wordType from "+tblName+"" +
                    " where word ='"+word+"'";
            result = DBConnect.excuteReadOneRow(NonRegularWord.class,sql);
        }  catch (Exception e) {
            MDC.put("eventID",tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:加载" + tblName + "篇频统计信息异常");
            e.printStackTrace();
        }
        return result ;
    }

    public synchronized boolean insertwordInfos(String tblName, String pageID, String vecWord, float count, String ID,String evtID,Timestamp insertTime,String pageTitle, String pageURL) {
        boolean flag = false;
        String sql = null;
        try {
            sql = "insert into "+tblName+" values ('"+pageID+"','"+vecWord+"','"+count+"','"+insertTime+"','"+ID+"','"+evtID+"','"+pageTitle.trim()+"','"+pageURL+"')";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID", evtID);
            LogWriter.logger.warn("分析器:" + tblName + "插入词频统计信息异常");
            e.printStackTrace();
        }
        return flag;
    }

    public synchronized boolean insertpageCount(String tblName, String ID, String word, float pageCount, boolean wordType){
        boolean flag = false;
        String sql = null;
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = tempDate.format(new java.util.Date());
        try {
            sql = "insert into " +tblName+ " values ('"+ID+"','"+word+"','"+pageCount+"','"+datetime+"','"+wordType+"','"+datetime+"')";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID",tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:" + tblName + "插入篇频统计信息异常");
            e.printStackTrace();
        }
        return flag;
    }

    public synchronized boolean updatepageCount(String tblName, String word, float pageCount) {
        boolean flag = false;
        String sql = null;
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = tempDate.format(new java.util.Date());
        try {
            sql = "update " +tblName+ " set pageCount = '"+pageCount+"', validateTime = '"+datetime+"' where word = '" + word + "'";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID",tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:" + tblName + "更新篇频统计信息异常");
            e.printStackTrace();
        }
        return flag;
    }

    public synchronized void updatespaceInfo(String tblName, String ID, String kw, float frequency, String isThemeWord) {
        String sql = null;
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = tempDate.format(new java.util.Date());
        try {
            sql = "update "+tblName+" set count = '"+frequency+"',lastUpdateTime = '"+datetime+"' where word = '" + kw + "'";
            if(DBConnect.excuteUpdate(sql)<=0){
                sql = "insert into "+tblName+" values ('"+ID+"','"+kw+"','"+String.valueOf(frequency)+"','"+datetime+"','"+datetime+"', '"+isThemeWord+"')";
                DBConnect.excuteUpdate(sql);
            }
        }  catch (Exception e) {
            MDC.put("eventID",tblName.split("_")[0]);
            LogWriter.logger.warn("分析器:" + tblName + "更新或插入空间特征向量信息异常");
            e.printStackTrace();
        }
    }

    public synchronized static List<NonRegularWord> loadPageCountWords(EventInfo evtInfo) {
        //从pagecount表中读取regular以外通过文章发现的新词集合
        String  tblname = evtInfo.getAnaPageCountTbleName(evtInfo.getEventID()) ;
        String sql = null;
        List<NonRegularWord> result = new ArrayList<NonRegularWord> () ;
        try {
            sql = "select id, word, pageCount, wordType from " + tblname;
            result = DBConnect.excuteQuery(NonRegularWord.class, sql);
        }  catch (Exception e) {
            MDC.put("eventID",evtInfo.getEventID());
            LogWriter.logger.warn("分析器:" + tblname + "加载篇频统计信息异常");
            e.printStackTrace();
        }
        return result ;
    }

    public synchronized boolean updateDeathInfo(String deathInfo, String injureInfo, String buildingInfo, String url, String webName, String eventID) {
        boolean flag = false;
        String sql = null;
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = tempDate.format(new java.util.Date());
        try {
            sql = "update eventinfo set deathinfo = '"+deathInfo+"', injureInfo = '"+injureInfo+"', buildingInfo = '"+buildingInfo+"',url = '"+url+"', webName = '"+webName+"', deathUpdateTime = '"+datetime+"' where eventID = '" + eventID + "'";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID",eventID);
            LogWriter.logger.warn("分析器:更新事件" + eventID + "灾情信息异常");
            e.printStackTrace();
        }
        return flag;
    }

    public synchronized static List<inetintelliprocess.bean.KeyWord> loadSpaceInfoWords(EventInfo evtInfo){
        String  tblname = evtInfo.getAnaSpaceInfoTbleName(evtInfo.getEventID()) ;
        String sql = null;
        List<inetintelliprocess.bean.KeyWord> result = new ArrayList<inetintelliprocess.bean.KeyWord> () ;
        try {
            sql = "select word , count from " + tblname + " where count > 0 and isThemeword = 'true' ";  //是否需要添加条件；例如词性 
            result = DBConnect.excuteQuery(inetintelliprocess.bean.KeyWord.class, sql);
        }  catch (Exception e) {
            MDC.put("eventID",evtInfo.getEventID());
            LogWriter.logger.warn("分析器:" + tblname + "加载主题词异常");
            e.printStackTrace();
        }
        return result ;
    }


    //统计当前spaceinfo中的所有count值
    public synchronized long getSpaceInfoCount(EventInfo evtInfo){
        String  tblname = evtInfo.getAnaSpaceInfoTbleName(evtInfo.getEventID()) ;
        String sql = null;
        long countNum = (long) 0.0;
        try {
            sql = "select sum(count) as countNum from " + tblname ;  //是否需要添加条件；例如词性 
            countNum = DBConnect.stat(sql);
        } catch (Exception e) {
            MDC.put("eventID",evtInfo.getEventID());
            LogWriter.logger.warn("分析器:" + tblname + "加载主题词频率异常");
            e.printStackTrace();
        }
        return countNum;
    }


    public static void main(String[] args){
//		WordManageDBO loader = new WordManageDBO();
//		List<WebPageInfo> pageCache = loader.loadtPageInfos("N30670E10406020140305181541", null);
    }
}