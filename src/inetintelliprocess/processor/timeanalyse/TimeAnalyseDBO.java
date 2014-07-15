package inetintelliprocess.processor.timeanalyse;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.processor.wordfeaturemanage.KeyWord;
import inetintelliprocess.processor.wordfeaturemanage.NonRegularWord;
import inetintelliprocess.util.LogWriter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.MDC;

public class TimeAnalyseDBO {

    public Date getTaskStartTime(String eventID) {
        String sql = null;
        Date result = null;
        try {
            sql =  "select pageTime from " + eventID + " order by pageTime asc limit 0, 1";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = dateFormat.parse((String)DBConnect.excuteReadOne(sql));
        } catch (Exception exp) {
            MDC.put("eventID",eventID);
            LogWriter.logger.warn("加载事件" + eventID + "时序统计分析开始时间异常");
        }
        return result;
    }

    public List<TTAnaInfo> loadtWordInfos(String tblName, Date t1, Date t2, String Word){
        String sql = null;
        List<TTAnaInfo> result = new ArrayList<TTAnaInfo>();
        try {
            DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTimeString = format1.format(t1);
            String endTimeString = format1.format(t2);

            sql = " select pageId, pageTitle, pageTime, pageContent, url, webName from " +tblName+ " where pageTime >= '" +startTimeString+"' and pageTime < '"+endTimeString+"' " +
                    " and pageContent like '%" +Word+ "%' order by pageTime asc";
            result = DBConnect.excuteQuery(TTAnaInfo.class,sql);
        } catch (Exception e) {
            if(tblName!="generalwebinfotbl")
                MDC.put("eventID",tblName);
            else
                MDC.put("eventID","日常搜索");
            LogWriter.logger.warn("加载" + tblName + "时间范围内的文本对象信息异常");
            e.printStackTrace();
        }
        for(TTAnaInfo info : result){
            info.setWord(Word);
            info.setCount(info.getWordCount(info.getPageContent(), Word));
        }
        return result ;
    }

    public boolean createWordTimeStat(String tblName) {
        if(DbTools.isTableExist(tblName)) {
            if (DbTools.clearTable(tblName))
                return true;
        }
        boolean flag = false;
        String sql = null;
        try {
            sql = "CREATE TABLE "+tblName+" (timestatid INT AUTO_INCREMENT PRIMARY KEY, word VARCHAR(100) NOT NULL, xmltxt TEXT NOT NULL)  charset utf8 collate utf8_general_ci";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID", tblName.split("_")[0]);
            LogWriter.logger.warn("创建事件时序统计分析表" + tblName + "异常");
            e.printStackTrace();
        }
        return flag;
    }

    public boolean insertWordTimeStat(String tblName, String XmlTxt,String word){
        boolean flag = false;
        String sql = null;
        try {
            int id = new Random().nextInt();
            sql = "insert into " +tblName+ "(timestatid, word, xmltxt) values ('"+id+"','"+word+"','"+XmlTxt+"')";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID",tblName.split("_")[0]);
            LogWriter.logger.warn("" + tblName + "插入事件时序统计分析表异常");
            e.printStackTrace();
        }
        return flag;
    }

    public List<NonRegularWord> loadTopNonRegWords(EventInfo myEvent, int topNum) {
        String tname = myEvent.getAnaPageCountTbleName(myEvent.getEventID());
        String sql = null;
        List<NonRegularWord> result = new ArrayList<NonRegularWord>();
        try {
            if(DbTools.isTableExist(tname)){
                sql = "select id, word, pageCount, wordType from " + tname + " where pageCount != 0 order by pageCount desc limit 0, "
                        + String.valueOf(topNum);
                result = DBConnect.excuteQuery(NonRegularWord.class,sql);
            }
            else
                result = null;
        } catch (Exception exp) {
            MDC.put("eventID",myEvent.getEventID());
            LogWriter.logger.warn("" + myEvent.getEventID() + "加载篇频统计信息异常");
        }
        return result;
    }

    public List<KeyWord> loadEventKeyWords(EventInfo myEvent,int topNum) {
        String tname = myEvent.getAnaSpaceInfoTbleName(myEvent.getEventID());
        String sql = null;
        List<KeyWord> result = new ArrayList<KeyWord>();
        try {
            if(DbTools.isTableExist(tname)){
                sql = "select id as wID, word as mainWord, count as frequency from " + tname + " where count != 0 order by count desc limit 0," + String.valueOf(topNum);
                result = DBConnect.excuteQuery(KeyWord.class, sql);
            }
            else
                result = null;
        } catch (Exception exp) {
            MDC.put("eventID",myEvent.getEventID());
            LogWriter.logger.warn("" + myEvent.getEventID() + "加载词频统计信息异常");
        }
        return result;
    }

    public static void main(String args[]){
        TimeAnalyseDBO ana = new TimeAnalyseDBO();
        List<KeyWord> list = ana.loadEventKeyWords(new EventInfo("s14100w07620020140315165920"), 3);
        int i=0;
        for(KeyWord key : list){
            System.out.println("第"+ i++ +"个："+key.getMainWord());
        }
    }

}
