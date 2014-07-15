package inetintelliprocess.searchengine.events;
/**
 * events事件处理对象及其操作
 */
import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.util.LogDbo;
import inetintelliprocess.util.LogWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.MDC;


/**
 *
 * 事件处理对象管理的业务类
 * 主要负责定义、修改事件处理对象，其中包括事件信息、事件状态
 * 创建事件处理对象，动态创建事件表
 *
 */
public class EventHandler {
    public enum eventState {
        START, RUN, SUSPEND, END, ABORT;
    }
    private EventInfo evtInfo = null;
    private Date arrivalTime = new Date();

    private Date startTime = null;//------------------------------------
    private Date endTime = null;
    private eventState evtState = eventState.END;
    public static EventHandlerDbo dbo = null;

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    //5月30日
    public EventHandler(String ID, String name, String Location, Date time, float magniValue, double x, double y) {
        evtInfo = new EventInfo(ID, name, Location, time, magniValue, x, y);
    }

    //5月30日
//	public EventHandler(String ID, String name, String Location, Date time, float magniValue) {
//
//		evtInfo = new EventInfo(ID, name, Location, time, magniValue);
//	}
    public EventInfo getEvtInfo() {
        return evtInfo;
    }

    public void setEvtInfo(EventInfo evtInfo) {
        this.evtInfo = evtInfo;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     *
     * @return
     */
    public eventState getEvtState() {
        return evtState;
    }

    /**
     *
     * @param evtState
     */
    public void setEvtState(eventState evtState) {
        this.evtState = evtState;
    }

    /**
     *
     * @param ID
     * @return
     * @throws Exception
     */
    public static EventInfo getEventFrom(String ID) throws Exception {
        EventInfo Info = new EventInfo();
        dbo = new EventHandlerDbo();
        if (dbo == null){
            System.out.println("dbo is null");
            return null;
        }
        Info = dbo.searchEventinfoTab(ID);
        return Info;
    }

    /**
     * 创建事件处理对象EventHandler
     * @param info(包括事件ID、事件名称、事件地点等)
     * @return 事件处理对象
     * @throws Exception
     */
    public static EventHandler createHandler(EventInfo info) throws Exception {
        System.out.println("建立事件：" +info.getEventID() +"空格"+ info.getEventName()+ "_" + "<" + info.getEventLocation() + ">");
        if (info.getEventID() == null || info.getEventID().equals(""))
            return null;
        dbo = new EventHandlerDbo();
        if (dbo == null)
            return null;
        if (!dbo.createTable(info))
            return null;

        System.out.println("为事件：" + info.getEventName()+ "_" + "<" + info.getEventLocation() + ">" + "创建数据表"
                + info.getEventID());
        //下面一句，5月30日改
        EventHandler handler = new EventHandler(info.getEventID(),info.getEventName(),info.getEventLocation(),info.getEventTime(),info.getMagnitude(),info.getLocx(),info.getLocy());
        //EventHandler handler = new EventHandler(info.getEvenID(),info.getEventName(),info.getEventLocation(),info.getEventTime(),info.getMagnitude(),info.getLocxd(),info.getLocyd());
        int state = 0;//尚未执行
        String status = String.valueOf(state);
        LogDbo logDbo = new LogDbo();
        //插入事件记录
        if (!logDbo.InsertLogTab(info.getEventID(), info.getEventName(), status))
            return null;

        System.out.println("为事件：" + info.getEventID()+ "_" + "<" +info.getEventName() + ">" + "创建日志记录" + info.getEventID());
        return handler;

    }
    /**
     * 设置状态，未开始0，正在1，中止2，已完成3.
     * @state
     * 正在执行1
     */
    public void setStartState() {
        if (getEvtState() != eventState.RUN) {
            setEvtState(eventState.RUN);
        }
        // 启动时间写入数据库log表，并修改状态
        setStartTime(new Date());
        int state = 1;
        try {
            startRecord(getEvtInfo().getEventID(),getStartTime(), state);
        } catch (Exception e) {
            LogWriter.logger.error(getEvtInfo().getEventID()
                    + "执行状态修改异常");
            e.printStackTrace();
        }
    }
    /**
     * 设置状态，未开始0，正在1，中止2，已完成3.
     * @state
     * 中止2
     */
    public void setSuspendState() {
        setEvtState(eventState.SUSPEND);
        // 启动时间写入数据库log表，并修改状态
        setEndTime(new Date());
        int state = 2;
        try {
            endRecord(getEvtInfo().getEventID(),getEndTime(), state);
        } catch (Exception e) {
            LogWriter.logger.error(getEvtInfo()
                    .getEventID()
                    + "执行状态修改异常");
            e.printStackTrace();
        }
    }
    /**
     * 设置状态，未开始0，正在1，中止2，已完成3.
     * @state
     * 结束3
     */
    public void setEndState(){
        setEvtState(eventState.END);
        // 启动时间写入数据库log表，并修改状态
        setEndTime(new Date());
        int state = 3;
        try {
            endRecord(getEvtInfo().getEventID(),getEndTime(), state);
        } catch (Exception e) {
            LogWriter.logger.error(getEvtInfo()
                    .getEventID()
                    + "执行状态修改异常");
            e.printStackTrace();
        }
    }
    /**
     *  将事件开始执行时间写入数据库
     * @param ID
     * @param time
     * @param state
     * @throws Exception
     */
    public void startRecord(String ID, Date time, int state)
            throws Exception {
        if (ID!= null) {
            LogDbo dbo = new LogDbo();
            // 处理日期,转换为字符串类型
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String stime = df.format(time);
            String status = String.valueOf(state);
            if (!dbo.startState(ID, stime, status))
                return;
        }
    }

    /**
     *
     * @param ID
     * @param time
     * @param state
     * @throws Exception
     */
    public void endRecord(String ID, Date time, int state)
            throws Exception {
        if (ID != null) {
            LogDbo dbo = new LogDbo();
            // 处理日期,转换为字符串类型
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String stime = df.format(time);
            String status = String.valueOf(state);
            if (!dbo.endState(ID, stime, status))
                return;


        }
    }

    /**
     * 判断是否与事件相关
     * @param info
     * @return
     */
    public boolean catchEvtInfo(String info) {
        if (evtInfo == null)
            return false;
        if (info.toLowerCase().indexOf(evtInfo.getEventName().toLowerCase()) >= 0
                && info.toLowerCase().indexOf(evtInfo.getEventLocation().toLowerCase()) >= 0)//有name和location
            return true;
        if (info.toLowerCase().indexOf(evtInfo.getEventLocation().toLowerCase()) >= 0)//有location
            return true;
        if (evtInfo.getAddrKeyWords()!= null) {
            for (int i = 0; i < evtInfo.getAddrKeyWords().size(); i++) {
                if (info.toLowerCase().indexOf(evtInfo.getAddrKeyWords().get(i).getWord().toLowerCase()) >= 0)//有关键字
                    return true;
            }
        }
        return false;
    }

    /**
     *  判断事件是否相同，根据ID等
     * @param evt
     * @return
     */
    public boolean isSameEvent(EventHandler evt) {
        if (evt == null)
            return false;
        if (evt.evtInfo.getEventID().equals(evtInfo.getEventID()))
            return true;

        return false;

    }

    /**
     * 获取事件表名
     * @return
     */
    public String getTableName() {
        if (evtInfo == null)
            return "generalWebInfoTbl";
        if (evtInfo.getEventID() == null)
            return "generalWebInfoTbl";
        return evtInfo.getWebPageTbleName(evtInfo.getEventID());
    }

    /**
     *
     * @param id
     * @param words
     */
    public void writeKwordsToEvtinfo(String id, ArrayList<KeyWord> words){
        if(!"".equals(id)&& !id.equals(null)){
            dbo = new EventHandlerDbo();
            if (dbo != null){
                if(!dbo.writeKwords(id, words)){
                    //数据库写入失败
                    MDC.put("eventID",id);
                    LogWriter.logger.warn("事件"+id+",关键词更新写入失败");
                }
            }else{
                //dbo创建失败
                MDC.put("eventID",id);
                LogWriter.logger.info("EventHandlerDbo对象创建失败");
            }
        }
    }
}
