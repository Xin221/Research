package inetintelliprocess.processor.newspubopinion;

import org.apache.log4j.MDC;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.util.LogWriter;
/**
 * 警告事件舆情简报组织线程入口
 * @author WQH
 */
public class PubOpinionThread extends Thread {
    private EventInfo pubEvent = null;
    private static Object lock = new Object();

    protected boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * 警告事件舆情简报组织线程启动
     */
    public void startWork() {
        synchronized (lock) {
            if (!running) {
                running = true;
                start();
            }
        }
    }

    /**
     * 警告事件舆情简报组织线程停止
     */
    public void stopWork() {
        synchronized (lock) {
            running = false;
        }
    }
    public EventInfo getPubEvent() {
        return pubEvent;
    }

    public void setPubEvent(EventInfo pubEvent) {
        this.pubEvent = pubEvent;
    }

    /**
     * 警告事件舆情简报组织线程启动
     */
    public void run(){
        while(running) {
            DbTools.clearPubOpiTable(pubEvent.getEventID());
            PubOpinionInfo pubOpiInfo = new PubOpinionInfo();
            pubOpiInfo.runStatPubOpinion(pubEvent);
            try {
                Thread.sleep(10 * 1000);
//				System.out.println("**********************");
            } catch(Exception exp) {
                MDC.put("eventID", pubEvent.getEventID());
                LogWriter.logger.warn("舆情信息计算发生异常");
            }
        }
    }
}
