package inetintelliprocess.processor.newspubopinion;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.util.LogWriter;
/**
 * 测试类
 * @author Wuxing
 *
 */
public class PubOpiFrame {
    public static void main(String[] args){
        EventInfo myEvent = new EventInfo("eqe095.9n22.720121111185442");
        LogWriter.logger.info("开始"+myEvent.getEventID()+"的舆情简报信息统计工作");
        inetintelliprocess.util.LogWriter.writelogo("开始"+myEvent.getEventID()+"的舆情简报信息统计工作") ;
        PubOpinionThread poi = new PubOpinionThread();
        poi.startWork();
        poi.setPubEvent(myEvent);
    }
}
