package inetintelliprocess.processor.timeanalyse;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.util.LogWriter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/**
 * 入口类
 * @author Wuxing
 *
 */
public class AnalyTimeStat {
    private final int minSpanSetting = 1000000;
    private final int minSpreadSetting = 20;
    private Date t1 = null;
    private Date t2 = null;

    public void TTAnalysis (String word, String tblNameWord, String tblNameStat) {
        TimeAnalyseDBO loader = new TimeAnalyseDBO() ;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            t1 = loader.getTaskStartTime(tblNameWord);
//			System.out.println("t1:" + t1.toString());
            t2 = new Timestamp(new Date().getTime()) ;
            t2 = format.parse(t2.toString());
        } catch (Exception e) {
            LogWriter.logger.warn("加载时序统计分析开始时间异常");
            e.printStackTrace();
        }
        List<TTAnaInfo> pages =  loader.loadtWordInfos(tblNameWord, t1, t2, word); //按照升序从数据库中提取含有word的全部wordinfo
        TimeTrace result = doTimeTrace(pages, t1, t2, word) ;
//		System.out.println(result.toString());
        result.save(tblNameStat) ;
    }

    public TimeTrace doTimeTrace(List<TTAnaInfo> pages, Date t1, Date t2,String word) {
        Date midTime = null;
        TimeTrace timeTrace = new TimeTrace(word, t1, t2, pages);
        long span = t2.getTime() - t1.getTime();
        if(span <= minSpanSetting ) // 小于最小时间间隔（1小时），结束切分，返回结果
            return timeTrace;
        if(timeTrace.getWordsCount(pages) <= minSpreadSetting )//小于最小密度（5），结束切分，返回结果
            return new TimeTrace(word, t1, t2, pages);
        long tj = (t1.getTime()+t2.getTime())/2 ;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        midTime = new Date(tj);
        String TimeString = format.format(midTime);
        try {
            midTime = format.parse(TimeString);
        } catch (ParseException e) {

            LogWriter.logger.warn("分析器:java.text.ParseException");
            e.printStackTrace();
        }
        TTAnaInfo pageInfos = new TTAnaInfo();
        List<TTAnaInfo>  p1 = pageInfos.getPagesFromList(pages,t1,midTime);//从列表中获取ti——tj时段的对象；
        List<TTAnaInfo>  p2 = pageInfos.getPagesFromList(pages,midTime,t2);//从列表中获取tj——tk时段的对象；
        TimeTrace tt1 = doTimeTrace(p1, t1, midTime,word); //获取 分布分析结果
        TimeTrace tt2 = doTimeTrace(p2, midTime, t2,word);//获取分布分析结果
        TimeTrace result = (TimeTrace) timeTrace.merge(tt1, tt2);
        return  result;
    }

    public void runTimeStatAnalysis(EventInfo myEvent) {
        AnalyTimeStat analyTimeStat = new AnalyTimeStat();
        TimeAnalyseDBO loader = new TimeAnalyseDBO() ;
        TTAnaInfo ttAnaInfo = new TTAnaInfo();
        List<String> words = null;
        words = ttAnaInfo.getEventWords(myEvent) ;
        String tblNameStat = myEvent.getAnaTimeStatTbleName(myEvent.getEventID());
        if(words != null && myEvent.getEventID()!="") {
            boolean createtable = loader.createWordTimeStat(tblNameStat);
            if(!createtable)
                //添加建立时序统计分析表不成功处理
                return ;
        }
        if (DbTools.clearTable(tblNameStat)) {
            for(String word : words) {
//				System.out.println("----------------------------------------------------------------------------------------------------------------");
                if(word!=null&&!word.isEmpty()&&!word.equalsIgnoreCase("null")){
                    System.out.println("word:"+word);
                    analyTimeStat.TTAnalysis(word, myEvent.getWebPageTbleName(myEvent.getEventID()), tblNameStat);
                }
            }
        }
    }

    public static void main(String[] args) {
        EventInfo myEvent = new EventInfo("ev112231");
        AnalyTimeStat analyTimeStat = new AnalyTimeStat();
        analyTimeStat.runTimeStatAnalysis(myEvent);
    }

}
