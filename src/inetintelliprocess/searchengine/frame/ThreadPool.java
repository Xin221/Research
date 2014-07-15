package inetintelliprocess.searchengine.frame;
/**
 * frame搜索器的框架
 */
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.searchengine.events.EventHandlerDbo;
import inetintelliprocess.util.LogDbo;
import inetintelliprocess.util.LogWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.MDC;

/**
 * 线程池
 * 管理等待队列和执行队列
 * 队列里放SearcherManager
 * @author
 *
 */
public class ThreadPool implements Runnable{
    private int poolSize;//最多正在执行的事件
    private ConcurrentHashMap<String,SearcherManager> waitingQueue;
    private ConcurrentHashMap<String,SearcherManager> executeQueue;



    public int getPoolSize() {
        return poolSize;
    }



    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }



    public ConcurrentHashMap<String, SearcherManager> getWaitingQueue() {
        return waitingQueue;
    }



    public void setWaitingQueue(
            ConcurrentHashMap<String, SearcherManager> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }



    public ConcurrentHashMap<String, SearcherManager> getExecuteQueue() {
        return executeQueue;
    }



    public void setExecuteQueue(
            ConcurrentHashMap<String, SearcherManager> executeQueue) {
        this.executeQueue = executeQueue;
    }



    public ThreadPool(int poolSize){

        this.poolSize = poolSize;
        waitingQueue= new ConcurrentHashMap<String,SearcherManager>();
        executeQueue = new ConcurrentHashMap<String,SearcherManager>();
    }

    /////////////////////////////////////////////////////////////////start
    public void init(){
        List<String> list = null;
        list = EventHandlerDbo.searchEventlogTab("1");
        if(list != null){
            for(int i = 0; i<list.size(); i++){
                SearcherManager mgr = new SearcherManager(list.get(i));
                DbTools.dropETables(mgr.getID());
                this.executeTask(mgr);
            }
        }
        list = EventHandlerDbo.searchEventlogTab("0");
        if(list != null){
            for(int j = 0; j<list.size(); j++){
                SearcherManager mgr = new SearcherManager(list.get(j));
                DbTools.dropETables(mgr.getID());
                this.commitTaskinWait(mgr);
            }
        }
//    	list = EventHandlerDbo.searchEventlogTab("2");
//    	if(list != null){
//    		for(int j = 0; j<list.size(); j++){
//    			SearcherManager mgr = new SearcherManager(list.get(j));
//    			mgr.setSuspendState();
//    			this.commitTaskinWait(mgr);
//    		}
//    	}
    }
/////////////////////////////////////////////////////////////////end

    /**
     * 提交事件任务
     * @param task
     * @return
     */
    public boolean commitTask(SearcherManager task){
        //根据事件ID得到对应的搜索管理器
///		GisServiceInv gis = new GisServiceInv();
        if(task != null){
            SearcherManager rec = waitingQueue.get(task.getID());
            if (rec == null) {
                synchronized (this) {
                    rec = waitingQueue.get(task.getID());
                    if (rec == null) {
                        // record does not yet exist
                        rec = waitingQueue.putIfAbsent(task.getID(), task);
                        System.out.println("将事件"+task.getID()+"放入了等待队列！");
                    }
                }
            }
            if(executeQueue.size()==poolSize){
                //找到一个震级最小的
                System.out.println("正在执行队列已满！");
                SearcherManager minMgr=minMagnitudeTaskinExe();
                if(minMgr!=null){
                    double min = minMgr.getEvtHandler().getEvtInfo().getMagnitude();
                    double taskMag = task.getEvtHandler().getEvtInfo().getMagnitude();
                    if(min<taskMag){//比较震级，min的震级比task震级小，则删去minMgr，放入task
                        if(cancelTask(minMgr.getID())){
                            executeTask(task);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean commitTaskinWait(SearcherManager task){
        SearcherManager rec = waitingQueue.get(task.getID());
        if (rec == null) {
            synchronized (this) {
                rec = waitingQueue.get(task.getID());
                if (rec == null) {
                    rec = waitingQueue.putIfAbsent(task.getID(), task);
                    return true;
                }
            }
        }
        return false;
    }
    public boolean cancelTask(String ID){
        boolean canceled = false;
        SearcherManager rec = executeQueue.get(ID);
        if (rec != null) {
            System.out.println("SearchManager在executeQueue中");
            synchronized (this) {
                rec = executeQueue.get(ID);
                if (rec != null) {
                    rec.stopWork();
                    rec = executeQueue.remove(ID);
                    //waitingQueue.put(ID, rec);
                    canceled = true;
                    System.out.println("remove");
                }
            }
        }
        else{
            //System.out.println("buzai zhengzai");
            LogDbo dbo = new LogDbo();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String stime = df.format(new Date());
            String status = String.valueOf(2);
            dbo.endState(ID, stime, status);
        }

        return canceled ;
    }
    public boolean existTask(String ID){
        SearcherManager rec = waitingQueue.get(ID);
        return (null != rec);
    }
    public boolean priorTask(String ID,String prior){
        SearcherManager rec = waitingQueue.get(ID);
        if (rec != null) {
            synchronized (this) {
                rec = waitingQueue.get(ID);
                if (rec != null) {
                    rec.updatePriority(prior);
                    rec = waitingQueue.put(rec.getID(), rec);
                    //executeTask(rec);
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 执行任务
     * 先从等待队列中移除，判断执行队列里是否已经有此事件ID
     * 若没有，则放入正在执行队列中，然后开始执行
     * @param task
     * @return
     */
    public boolean executeTask(SearcherManager task){
        SearcherManager rec = waitingQueue.get(task.getID());//根据事件ID从等待队列中取出其搜索器
        if (rec != null) {
            synchronized (this) {
                rec = waitingQueue.get(task.getID());
                if (rec != null) {

                    rec = waitingQueue.remove(task.getID());
//	                System.out.println("移除掉waitingQueue中事件"+task.getID());
                    MDC.put("eventID", task.getID());
                    LogWriter.logger.info("移除掉waitingQueue中事件"+task.getID());
                }
            }
        }
        rec = executeQueue.get(task.getID());
        //判断执行队列里是否已经有此ID
        if (rec == null) {
            synchronized (this) {
                rec = executeQueue.get(task.getID());
                if (rec == null) {
                    System.out.println("正在执行队列中无事件"+task.getID());
                    LogWriter.logger.info("正在执行队列中无事件"+task.getID());
                    rec = executeQueue.putIfAbsent(task.getID(), task);
                    System.out.println("事件"+task.getID()+"加入正在执行队列！");
                    LogWriter.logger.info("事件"+task.getID()+"加入正在执行队列！");
                    task.startWork();
                    return true;
                }
            }
        } else{
            System.out.println("正在执行队列中存在事件"+task.getID());
            LogWriter.logger.info("正在执行队列中存在事件"+task.getID());
        }
        return false;
    }

    /**
     * 事件调度
     * 取出等待队列的事件ID集合，然后根据事件ID比较各事件的优先级，返回优先级最大的事件的搜索器
     * @return
     */
    public SearcherManager scheduleTask(){

        Enumeration<SearcherManager> mgr = waitingQueue.elements();
        float maxpri = 0;//记录最大优先级
        SearcherManager maxMgr = null;
        while(mgr.hasMoreElements()) {
            SearcherManager sm = mgr.nextElement();
            float pri = sm.getPriorityValue();
            if(pri >= maxpri)
                maxMgr = sm;
        }
        return maxMgr;
    }




    /**
     * 找到正在执行队列中震级最小的事件
     */
    public SearcherManager minMagnitudeTaskinExe(){
        Set<String> key = executeQueue.keySet();//等待队列的事件ID集合
        float minmagni = 0;//记录最小震级
        SearcherManager minMgr = null;
        for (Iterator<String> it = key.iterator(); it.hasNext();) {
            String id = (String)it.next();
            SearcherManager sm = (SearcherManager)executeQueue.get(id);
            float pri = sm.getEvtHandler().getEvtInfo().getMagnitude();
            if(pri < minmagni)
                minMgr = sm;

        }
        return minMgr;
    }
    /**
     *
     */
    @Override
    public void run() {
        // TODO Auto-generated method stub
        while(true){
            Enumeration<SearcherManager> smgr = executeQueue.elements();
            //维护执行队列，若执行完就请出去
            while (smgr.hasMoreElements()) {
                //得到取出的ID的搜索器
                SearcherManager it = smgr.nextElement();
                if(!it.isRunning()){//执行完
                    executeQueue.remove(it.getID());
                }
            }
            if(executeQueue.size() < poolSize){
                //System.out.println("开始调度喽~~~");
                SearcherManager task = scheduleTask();//调度出来等待队列中优先级最大的事件的搜索器
                if(null != task){
                    System.out.println("调度处理的是"+task.getID());
                    executeTask(task);
                }
            }

        }
    }

}
