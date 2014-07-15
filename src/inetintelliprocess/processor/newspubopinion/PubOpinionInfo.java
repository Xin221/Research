package inetintelliprocess.processor.newspubopinion;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 警告事件舆情简报组织形式
 * 舆情信息归纳整理业务类，负责归纳整理用户需要的舆情信息
 * @author WQH
 */
public class PubOpinionInfo {

    /**
     * 警告事件舆情简报嵌套内部类，用以统计特定一天新闻文章发布量和rss信息订阅文章发布量
     */
    private class DayCount{
        private String strDayInfo = null;
        private int newsArticleCount = 0;
        private int rssArticleCount = 0;
    }

    private Date pubOpinionDay = null;
    private String mediaName = null;
    private String mediaArticleCount = null;
    private String mediaRepresentArticle = null;
    private String mediaRepresentArticleURL = null;
    private String mediaRepresentArticleAbstract = null;

    public Date getPubOpinionDay() {
        return pubOpinionDay;
    }

    public void setPubOpinionDay(Date pubOpinionDay) {
        this.pubOpinionDay = pubOpinionDay;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getMediaArticleCount() {
        return mediaArticleCount;
    }

    public void setMediaArticleCount(String mediaArticleCount) {
        this.mediaArticleCount = mediaArticleCount;
    }

    public String getMediaRepresentArticle() {
        return mediaRepresentArticle;
    }

    public void setMediaRepresentArticle(String mediaRepresentArticle) {
        this.mediaRepresentArticle = mediaRepresentArticle;
    }

    public String getMediaRepresentArticleURL() {
        return mediaRepresentArticleURL;
    }

    public void setMediaRepresentArticleURL(String mepresentArticleURL) {
        this.mediaRepresentArticleURL = mepresentArticleURL;
    }

    public String getMediaRepresentArticleAbstract() {
        return mediaRepresentArticleAbstract;
    }

    public void setMediaRepresentArticleAbstract(String mepresentArticleAbstract) {
        this.mediaRepresentArticleAbstract = mepresentArticleAbstract;
    }

    /**
     * 将统计信息以及描述信息拼成XML文件格式（警告事件舆情简报按照天的形式拼成XML文件形式）
     * 警告事件舆情简报按照天的形式映射成键值，
     * 每一键值对应一XML文件格式描述信息（包括发布网站名称，发布量统计，代表文本，代表文本链接，代表文本摘要）
     * @ param pubOpinionInfos 警告事件舆情信息对象列表（警告事件舆情信息对象为：发布网站名称，发布量统计，代表文本，代表文本链接，代表文本摘要）
     */
    public HashMap<Date,DayCount> generatePubOpinionDay(List<PubOpinionInfo> pubOpinionInfos){
        HashMap<Date,DayCount> resultDayInfo = new HashMap<Date,DayCount>();
        for(PubOpinionInfo pubopiInfo : pubOpinionInfos){
            Date resultDay = pubopiInfo.getPubOpinionDay();
            //System.out.println("resultDay "+resultDay);
            if(pubopiInfo != null && pubopiInfo.getPubOpinionDay() != null){
                DayCount info = resultDayInfo.get(resultDay);
                if(null == info)
                    info = new DayCount();
                String dayInfo = info.strDayInfo;
                if(pubopiInfo.mediaName.contains("RSS") )
                    info.rssArticleCount += Integer.parseInt(pubopiInfo.mediaArticleCount);
                else if (!pubopiInfo.mediaName.contains("RSS") )
                    info.newsArticleCount += Integer.parseInt(pubopiInfo.mediaArticleCount);
                if(dayInfo == null)
                    dayInfo = "<publicOpinion MediaName=\"" + pubopiInfo.mediaName + "\"  MediaArticleCount=\"" + pubopiInfo.mediaArticleCount + "\" MediaRepresentArticle=\"" + pubopiInfo.mediaRepresentArticle + "\" MediaRepresentArticleURL=\"" + pubopiInfo.mediaRepresentArticleURL + "\" MediaRepresentArticleAbstract=\"" + pubopiInfo.mediaRepresentArticleAbstract + "\"" + "/>";
                else
                    dayInfo += "<publicOpinion MediaName=\"" + pubopiInfo.mediaName + "\"  MediaArticleCount=\"" + pubopiInfo.mediaArticleCount + "\" MediaRepresentArticle=\"" + pubopiInfo.mediaRepresentArticle + "\" MediaRepresentArticleURL=\"" + pubopiInfo.mediaRepresentArticleURL + "\" MediaRepresentArticleAbstract=\"" + pubopiInfo.mediaRepresentArticleAbstract + "\"" + "/>";
                info.strDayInfo = dayInfo;
                //System.out.println(dayInfo);
                resultDayInfo.put(resultDay, info);
            }
        }
        return resultDayInfo;
    }

    /**
     * 警告事件舆情简报按照天的形式存储于数据表
     * @ param eventID 警告事件标识，PubOpinionDay yyyy-mm-dd形式的天，pubOpinionDes 该天XML文件格式描述信息，newsArticleCount 新闻文章发布量，rssArticleCount rss信息订阅文章发布量
     */
    public boolean writeToDB(String eventID, Date PubOpinionDay, String pubOpinionDes, int newsArticleCount, int rssArticleCount) {
        if(StringUtil.isNull(eventID))
            eventID = "generalSearch";
        PubOpinionDBO pubOpinion = new PubOpinionDBO();
        if(pubOpinion.insertPubOpinionInfo(eventID, PubOpinionDay, pubOpinionDes, newsArticleCount, rssArticleCount))
            return true;
        else
            return false;
    }

    /**
     * 1.警告事件舆情简报启动新闻文章和rss订阅文章发布量情况统计过程
     * 2.将统计信息以及描述信息拼成XML文件格式
     * 3.警告事件舆情简报按照天的形式存储于数据表
     * @ param eventID 警告事件标识
     */
    @SuppressWarnings("unchecked")
    public void runStatPubOpinion(EventInfo eventID) {
        PubOpinionDBO pubOpinion = new PubOpinionDBO();
        List<PubOpinionInfo> pubOpinionInfos = new ArrayList<PubOpinionInfo>();
        pubOpinionInfos = pubOpinion.loadPubOpinionInfos(eventID.getWebPageTbleName(eventID.getEventID()));
        PubOpinionInfo pubOpinionInfo = new PubOpinionInfo();
        HashMap<Date,DayCount> pubOdays = pubOpinionInfo.generatePubOpinionDay(pubOpinionInfos);
        //System.out.println("pubOdays length is :"+pubOdays.size());
        Iterator<?> iterator = pubOdays.entrySet().iterator();
        int k = 0;
        while(iterator.hasNext()){
            Map.Entry<Date,DayCount> entry = (Map.Entry<Date,DayCount>) iterator.next();
            Date day = (Date)entry.getKey();
            DayCount info = (DayCount) entry.getValue();
//			System.out.println(day + " "+ ++k+":" + info.strDayInfo);
//			System.out.println("newsArticleCount:"+info.newsArticleCount);
//			System.out.println("rssArticleCount:"+info.rssArticleCount);
            //加写入库

            if (k<=30&&pubOpinionInfo.writeToDB(eventID.getEventID(), day, "<root>"+info.strDayInfo+"</root>", info.newsArticleCount, info.rssArticleCount))
            {
//				System.out.println("continue");
                continue;
            }
//			else{
//				System.out.println("break");
//				break;
//			}
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args){
        PubOpinionDBO pubOpinion = new PubOpinionDBO();
        List<PubOpinionInfo> pubOpinionInfos = new ArrayList<PubOpinionInfo>();
        pubOpinionInfos = pubOpinion.loadPubOpinionInfos("ev112231");
        PubOpinionInfo pubOpinionInfo = new PubOpinionInfo();
        HashMap<Date,DayCount> pubOdays = pubOpinionInfo.generatePubOpinionDay(pubOpinionInfos);
        if(DbTools.clearPubOpiTable("ev112231")) {
            Iterator<?> iterator = pubOdays.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<Date,DayCount> entry = (Map.Entry<Date,DayCount>) iterator.next();
                Date day = (Date)entry.getKey();
                DayCount info = (DayCount) entry.getValue();
//				System.out.println(day + ":" + info.strDayInfo);
//				System.out.println(info.newsArticleCount);
//				System.out.println(info.rssArticleCount);
//				//加写入库
                if (pubOpinionInfo.writeToDB("ev112231", day, "<root>"+info+"</root>", info.newsArticleCount, info.rssArticleCount))
                    continue;
                else
                    break;
            }
        }
    }
}
