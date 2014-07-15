package inetintelliprocess.searchengine.events;
/**
 * events事件处理对象及其操作
 */
import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.util.LogWriter;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;
/**
 *
 * 事件信息管理的业务类
 * 主要负责事件对象信息的数据库存储和访问工作
 * eventHandler数据库操作的实际实现
 *
 */
public class EventHandlerDbo {

    /**
     * 加入事件表
     * @param info（包括事件ID、事件名称、事件地点等）
     * @return
     */
    public synchronized boolean InsertEventInfoTab(EventInfo info) {
        System.out.println("ID"+info.getEventID()+"空格");
        boolean flag = true;
        String sql = null;
        // 检查是否已存在记录
        sql = "SELECT count(*) FROM eventinfo WHERE eventID='"
                + info.getEventID() + "'";
        try {
            if (DBConnect.stat(sql)>0) {
                MDC.put("eventID",info.getEventID());
                LogWriter.logger.info("存在 eventID=" + info.getEventID()
                        + "的事件记录");
                // 删除记录
                sql = " DELETE FROM eventinfo WHERE eventID='"
                        + info.getEventID() + "'";
                if(DBConnect.excuteUpdate(sql)>=0){
                    MDC.put("eventID",info.getEventID());
                    LogWriter.logger.info("删除 eventID=" + info.getEventID()
                            + "的事件记录");
                }
                else{
                    MDC.put("eventID", info.getEventID());
                    LogWriter.logger.error("删除" + info.getEventID() + "事件记录错误");
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 插入新记录
        if(flag=InsertTab(info)){
            MDC.put("eventID",info.getEventID());
            LogWriter.logger.info("插入新的事件记录"+info.getEventID());
        }
        return flag;
    }

    /**
     *
     * @param ID
     * @return
     */
    public synchronized EventInfo searchEventinfoTab(String ID) {
        EventInfo Info = new EventInfo();
        String sql = null;
        try {
            //sql = "SELECT eventName,eventLocation,eventTime,magnitude,locx,locy FROM eventinfo WHERE eventID='"	+ ID + "'";
            sql = "SELECT * FROM eventinfo WHERE eventID='"	+ ID + "'";
            Info = DBConnect.excuteReadOneRow(EventInfo.class, sql);
        }catch (Exception e) {
            MDC.put("eventID", ID);
            LogWriter.logger.error("选取eventID=" + ID + "的事件记录错误");
            e.printStackTrace();
        }
        return Info;
    }

    /**
     *  插入新记录
     * @param info
     * @return
     */
    public synchronized boolean InsertTab(EventInfo info) {
        System.out.println("ID"+info.getEventID()+"空格");
        boolean flag = true;
        String sql = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strTime = null;
        if(info.getEventTime()!=null)
            strTime = df.format(info.getEventTime());
        try {
            sql = " INSERT INTO eventInfo(eventID,eventName,eventLocation,eventTime, magnitude,locx,locy)VALUES(?,?,?,?,?,?,?)";
            if(DBConnect.excuteUpdate(sql, info.getEventID(),info.getEventName(),info.getEventLocation(),strTime,info.getMagnitude(),
                    info.getLocx(),info.getLocy())>=0)
                flag = true;
        } catch (Exception e) {
            e.printStackTrace();
            MDC.put("eventID", info.getEventID());
            LogWriter.logger.error("插入eventID=" + info.getEventID() + "的事件记录错误");
        }
        return flag;
    }

    /**
     *  动态创建数据表
     * @param info
     * @return
     */
    public synchronized boolean createTable(EventInfo info) {
        String ID = info.getWebPageTbleName(info.getEventID());
        boolean flag = false;
        String sql = null;
        if (DbTools.isTableExist(ID)) {
            return true;
        }
        try {
            sql = "CREATE TABLE "
                    + ID
                    + " (pageId INT AUTO_INCREMENT,pageTitle VARCHAR(100),pageTime VARCHAR(100),pageContent TEXT,url VARCHAR(250),keyWords VARCHAR(600),Abstract VARCHAR(600),lastWriteTime DATETIME,webType VARCHAR(64),webName VARCHAR(64),sendTo int(8),PRIMARY KEY (pageId,pageTitle,url)  )ENGINE=InnoDB   DEFAULT   CHARSET=UTF8";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            e.printStackTrace();
            MDC.put("eventID", info.getEventID());
            LogWriter.logger.error("动态创建eventID=" + ID + "的事件记录错误");
        }
        return flag;
    }


    //////////////////////////////新加入的函数
    public synchronized static List<String> searchEventlogTab(String state){
        String sql = null;
        List<String> list = new ArrayList<String>();
        try {
            sql = "SELECT * FROM eventlog WHERE status='"
                    + state + "'";
            list = DBConnect.excuteReadOneColumn("eventID",sql);
        }catch (Exception e) {
            e.printStackTrace();
            MDC.put("eventID","日常搜索");
            LogWriter.logger.error("选取正在执行中的事件记录错误");
        }
        return list;
    }




    /**
     *
     * @param id
     * @param words
     * @return
     */
    public synchronized boolean writeKwords(String id,ArrayList<KeyWord> words){
        boolean flag = false;
        String sql = null;
        String word = "";
        int i = 0;
        for(; i< words.size()-1; i++){
            word = word + words.get(i).getWord()+",";

        }
        if(i == words.size()-1){
            word += words.get(i).getWord();
        }
        try {

            sql = "UPDATE eventinfo SET kwords ='"
                    + word + "' WHERE eventid='"
                    + id + "'";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }catch (Exception e) {
            MDC.put("eventID",id);
            LogWriter.logger.error(e+"写入关键字错误！");
            e.printStackTrace();
        }
        return flag;
    }

    public static void main(String[] args) throws Exception {
        //DatabaseConnection dbc = new DatabaseConnection();
        //	EventHandlerDbo ehD = new EventHandlerDbo(dbc.getConnection());
        //ehD.searchEqinfoTab("ev112231");
    }
}
