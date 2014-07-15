package inetintelliprocess.processor.newpubopinion;


import java.util.Date;
import java.util.List;

/**
 * newsPubOpinion
 * @author RoseRye
 *
 */
public class NewsPubOpinion {
    private String eventID;
    private Date dayTime;
    private String lastUpdateTime;
    private String pubOpinion;
    private List<PubOpinionInfo> pubOpinionList;

    private int newsAticleCount;
    private int rssArticleCount;

    public NewsPubOpinion()
    {

    }

    public String getEventID() {
        return eventID;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public Date getDayTime() {
        return dayTime;
    }
    public void setDayTime(Date date) {
        this.dayTime = date;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }
    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getPubOpinion() {
        return pubOpinion;
    }
    public void setPubOpinion(String pubOpinion) {
        this.pubOpinion = pubOpinion;
    }
    public int getNewsAticleCount() {
        return newsAticleCount;
    }
    public void setNewsAticleCount(int newsAticleCount) {
        this.newsAticleCount = newsAticleCount;
    }
    public int getRssArticleCount() {
        return rssArticleCount;
    }
    public void setRssArticleCount(int rssArticleCount) {
        this.rssArticleCount = rssArticleCount;
    }

    public List<PubOpinionInfo> getPubOpinionList() {
        return pubOpinionList;
    }
    public void setPubOpinionList(List<PubOpinionInfo> pubOpinionList) {
        this.pubOpinionList = pubOpinionList;
    }
}
