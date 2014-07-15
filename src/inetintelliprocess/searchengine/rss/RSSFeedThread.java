package inetintelliprocess.searchengine.rss;

import org.apache.log4j.MDC;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.util.LogWriter;
/**
 * 线程入口
 * @author Wuxing
 *
 */
public class RSSFeedThread extends Thread {
    private EventInfo rssEvent = null;
    private FeedReaderFrame feedRead = null;
    private static Object lock = new Object();

    protected boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void startWork() {
        synchronized (lock) {
            if (!running) {
                running = true;
                if(this.getState().toString()!="NEW")
                    run();
                else
                    start();
            }
        }
    }

    public void stopWork() {
        synchronized (lock) {
            running = false;
        }
    }
    public EventInfo getRssEvent() {
        return rssEvent;
    }

    public void setRssEvent(EventInfo rssEvent) {
        this.rssEvent = rssEvent;
    }

    public RSSFeedThread(FeedReaderFrame feedRead){
        this.feedRead = feedRead;
    }

    public void run(){
        String tname = rssEvent.getWebPageTbleName(rssEvent.getEventID());
        boolean hasNext = true;
        //hasNext = feedRead.runRSSFeed(rssEvent, tname);
        for(int i = 0; i< 1; i++){
            while(running) {
                hasNext = feedRead.runRSSFeed(rssEvent, tname);
                if(!hasNext){
                    break;
                }
            }
            try {
                Thread.sleep(1* 1000);
            } catch(Exception exp) {
                MDC.put("eventID", rssEvent.getEventID());
                LogWriter.logger.warn("rss订阅发生异常:重新开始工作");
            }
        }
        running = false;
        MDC.put("eventID",rssEvent.getEventID());
        LogWriter.logger.info("RSS:结束当前RSS搜索活动");
    }
}
