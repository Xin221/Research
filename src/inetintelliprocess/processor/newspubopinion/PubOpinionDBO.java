package inetintelliprocess.processor.newspubopinion;

import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.util.LogWriter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.MDC;
/**
 * 警告事件舆情简报数据库操作类
 * @author WQH
 */
public class PubOpinionDBO {

    /**
     * 获取警告事件舆情信息对象列表（以天形式进行组织）（警告事件舆情信息对象为：发布网站名称，发布量统计，代表文本，代表文本链接，代表文本摘要）
     * @ param evtID 警告事件标识
     */
    public synchronized List<PubOpinionInfo> loadPubOpinionInfos(String evtID) {
        String sql = null;
        List<PubOpinionInfo> result = new ArrayList<PubOpinionInfo> () ;
        try {
            sql = "select pageTitle as mediaRepresentArticle, DATE(pageTime) as pubOpinionDay , url as mediaRepresentArticleURL, count(url) as mediaArticleCount, Abstract as mediaRepresentArticleAbstract, webName as mediaName from (select * from " + evtID + " as t1 inner join (select distinct DATE(pageTime) ss from "+evtID+" where pageTime is not null and pageTime<>'null' order by pageTime desc)as t2 on DATE(t1.pageTime) =t2.ss) as orderTable group by DATE(pageTime),webName";
            result = DBConnect.excuteQuery(PubOpinionInfo.class, sql);
        } catch (Exception e) {
            MDC.put("eventID", evtID);
            LogWriter.logger.warn("加载舆情信息异常");
            e.printStackTrace();
        }
        return result ;
    }

    /**
     * 警告事件舆情简报按照天的形式存储于数据表
     * @ param eventID 警告事件标识，
     *         PubOpinionDay yyyy-mm-dd形式的天，
     *         pubOpinionDes 该天XML文件格式描述信息，
     *         newsArticleCount 新闻文章发布量，
     *         rssArticleCount rss信息订阅文章发布量
     */
    public synchronized boolean insertPubOpinionInfo(String eventID, Date pubOpinionDay, String pubOpinionDes, int newsArticleCount, int rssArticleCount) {
        boolean flag = false;
        String sql = null;
        try {
            String ID = String.valueOf(new Random().nextInt());
            pubOpinionDes = pubOpinionDes.replace("\\xE2\\x80\\x93", "");
            pubOpinionDes = pubOpinionDes.replace("'", "‘");
            sql = "insert into publicopinioninfo (ID,eventID,dayTime,pubOpinion,newsArticleCount,rssArticleCount,lastUpdateTime) values ('"+ID+"','"+eventID+"','"+pubOpinionDay+"','"+pubOpinionDes+"','"+newsArticleCount+"','"+rssArticleCount+"','"+new Timestamp(new Date().getTime())+"')";
            //sql = URLEncoder.encode(sql, "utf-8");
            if(DBConnect.excuteUpdate(sql)>0)
                flag = true;
        } catch (Exception e) {
            MDC.put("eventID", eventID);
            LogWriter.logger.warn("舆情信息入库异常");
            e.printStackTrace();
        }
        return flag;
    }

    public static void main(String args[]){
        PubOpinionDBO pubOpinion = new PubOpinionDBO();
        List<PubOpinionInfo> pubOpinionInfos = pubOpinion.loadPubOpinionInfos("N05253W07284920140311215705");
        int i=0;
        for(PubOpinionInfo pubInfo: pubOpinionInfos){
            System.out.println("第"+ i++ +"条：");
            System.out.println(pubInfo.getMediaRepresentArticle());
            System.out.println(pubInfo.getMediaRepresentArticleAbstract());
            System.out.println(pubInfo.getMediaRepresentArticleURL());
            System.out.println(pubInfo.getMediaName());
            System.out.println(pubInfo.getMediaArticleCount());
        }
    }
}
