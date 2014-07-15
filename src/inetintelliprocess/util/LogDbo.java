package inetintelliprocess.util;
/**
 * util辅助工具类
 */
import inetintelliprocess.dbc.DBConnect;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.MDC;
/**
 * 事件对象eventLog的数据库操作
 * 事件日志管理的业务类
 * 主要负责事件日志数据表的存读写操作，记录以及修改线程的运行状态
 * 设置状态
 * @state
 * 未开始0，正在1，中止2，完成3.
 */
public class LogDbo {
    public synchronized boolean isSameRecord(String ID){
        boolean flag = true;
        String sql = null;
        try {
            sql = " SELECT count(*) from eventLog WHERE eventID='" +ID+ "' and status=1";
            if(DBConnect.stat(sql)<=0){
                MDC.put("eventID",ID);
                LogWriter.logger.info("事件日志表中无"+ID+"的事件，可以插入事件日志。");
                flag = false;
            }
        } catch (Exception e) {
            MDC.put("eventID",ID);
            LogWriter.logger.error("查询事件日志错误");
            e.printStackTrace();
        }
        return flag;
    }
    public synchronized boolean InsertLogTab(String ID, String description, String status) {
        boolean flag = true;
        String sql = null;
        try {
            sql = "SELECT count(*) FROM eventLog WHERE eventID='" + ID
                    + "'";
            if(DBConnect.stat(sql)>0)
                if(!updateInitState(ID, status))
                    flag = false;
                else
                    flag = true;
            else
            if(!InsertLogtab(ID,description,status))
                flag = false;
            else
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID",ID);
            LogWriter.logger.error("插入事件日志错误");
            e.printStackTrace();
        }
        return flag;
    }

    public synchronized boolean InsertLogtab(String ID, String description, String status) throws SQLException{
        boolean flag = false;
        String sql = " INSERT INTO eventLog(UUID,eventID,description,status)VALUES(?,?,?,?)";
        if(DBConnect.excuteUpdate(sql,UUID.randomUUID().toString(),ID,description,status)>0)
            flag = true;
        return flag;
    }
    //读取事件状态
    public synchronized String getState(String evtID) {
        boolean flag = true;
        String sql = null;
        String state = new String("中止");
        int stat = 0;
        try {
            sql = "SELECT status FROM eventlog WHERE eventID='" + evtID
                    + "'";
            stat = DBConnect.excuteReadOne(sql);
            if(flag == false){
                state = "中止";
                return state;
            }
            if (stat == 1) {
                state = "正在执行";
            } else if (stat == 0) {
                state = "准备开始";
            } else if (stat == 3){
                state = "完成";
            }
        } catch (Exception e) {
            MDC.put("eventID",evtID);
            LogWriter.logger.error("读取事件状态错误");
            e.printStackTrace();
        }
        return state;
    }
    public synchronized boolean updateInitState(String ID, String status){
        boolean flag = false;
        String sql = null;
        try {
            sql = " UPDATE eventLog set startTime=\\N,endTime=\\N,status='" + status + "' WHERE eventID='" + ID + "'";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID",ID);
            LogWriter.logger.error("改变事件执行状态错误");
            e.printStackTrace();
        }
        return flag;
    }
    //改变事件状态，设为执行
    public synchronized boolean startState(String ID, String time, String state) {
        //首先检查是否存在相同的ID，如果存在则替换其中的信息，如果不存在则创建一条记录
        boolean flag = false;
        String sql = null;
        try {

            sql = " UPDATE eventLog set startTime='" + time
                    + "',endTime=\\N,status='" + state + "' WHERE eventID='" + ID + "'";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID",ID);
            LogWriter.logger.error("改变事件执行状态错误");
            e.printStackTrace();
        }
        return flag;
    }

    //结束事件状态，改为结束
    public synchronized boolean endState(String ID, String time, String state) {
        boolean flag = false;
        String sql = null;
        try {
            sql = " UPDATE eventLog set endTime='" + time + "',status='"
                    + state + "' WHERE eventID='" + ID + "'";
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID", ID);
            LogWriter.logger.error("改变事件执行状态错误");
            e.printStackTrace();
        }
        return flag;
    }

    public static void main(String[] args){
        LogDbo dbo = new LogDbo();
        dbo.InsertLogTab("ev11", "test", "0");
        System.out.println(dbo.isSameRecord("ev112"));
    }
}
