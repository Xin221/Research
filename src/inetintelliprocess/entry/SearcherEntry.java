package inetintelliprocess.entry;
/**
 * 搜索器入口
 */
import org.apache.log4j.MDC;

import inetintelliprocess.searchengine.frame.DailyManager;
import inetintelliprocess.searchengine.frame.SearcherManager;
import inetintelliprocess.searchengine.frame.ThreadPool;
import inetintelliprocess.searchengine.socket.Server;
import inetintelliprocess.util.LogDbo;
import inetintelliprocess.util.LogWriter;

/**
 * 搜索器入口
 * @author
 *
 */
public class SearcherEntry {
    private static final int poolSize = 2;
    private static ThreadPool tp;
    private DailyManager dMgr = null;
    private static SearcherEntry instance = null;
    public static final int GoogleCount = 100;
    public static int googleSum = 0;

    private SearcherEntry(){
        if(dMgr==null){
            dMgr = new DailyManager();
            dMgr.init();//解析test.xml文件，形成coreSearches、kwords、generalSearcher列表
            dMgr.startWork();//启动核心线程搜索以及普通线程搜索
        }
    }


    /**
     * 实例化
     * @return
     */
    public static synchronized SearcherEntry getInstance() {

        if (instance == null) {
            instance = new SearcherEntry();
            Server s = new Server();
            s.start();
            tp = new ThreadPool(poolSize);
            tp.init();
            new Thread(tp).start();
        }
        return instance;

    }

    public static void main(String[] args) {
        SearcherEntry.getInstance();

    }

    /**
     * 接收警告事件
     * @void
     * 判断log表中是否具有eventID相等且执行状态为正在执行1的数据项.
     * 初始化为SearcherManager对象
     */
    public void recieveAlarmID(final String ID){
        final LogDbo logDbo = new LogDbo();
        MDC.put("eventID", ID);
        LogWriter.logger.info("接收到事件触发，事件 eventID=" + ID + "的事件记录");
        if(!(ID.equals(null) || "".equals(ID))){
            if(!logDbo.isSameRecord(ID)){
                MDC.put("eventID",ID);
                LogWriter.logger.info("事件列表中不存在"+ID+"的事件，可以开始！");
                SearcherManager mgr = new SearcherManager(ID);
                if(mgr.getEvtHandler()!=null)
                    tp.commitTask(mgr);
            }
            else{
                MDC.put("eventID", ID);
                LogWriter.logger.info("事件"+ID+"已存在。");
            }
        }
    }

    /**
     * 接收用户事件
     * @void
     * 在readyList寻找
     * 修改优先级，priorityValue值越大，优先级越高
     */
    public void recieveUserID(String ID,String priority){
        if(!(ID.equals(null) || "".equals(ID))){
            MDC.put("eventID", ID);
            LogWriter.logger.info("接收到用户启动，事件 eventID=" + ID + "的事件记录");
            if(tp.existTask(ID)){
                //修改优先级
                MDC.put("eventID", ID);
                LogWriter.logger.info("存在事件eventID="+ID+"的事件记录");
                tp.priorTask(ID,priority);
                tp.commitTask(tp.getWaitingQueue().get(ID));
            }
            else{
                MDC.put("eventID", ID);
                LogWriter.logger.info("新建事件eventID="+ID+"的事件记录");
                SearcherManager mgr = new SearcherManager(ID);
                mgr.updatePriority(priority);
                tp.commitTask(mgr);
            }

        }
    }

    public void cancelUserID(String ID){
        if(!(ID.equals(null) || "".equals(ID))){
            MDC.put("eventID", ID);
            LogWriter.logger.info("接收到用户停止，事件 eventID=" + ID + "的事件记录");
            tp.cancelTask(ID);
        }
        System.out.println("Hello");
    }


}
