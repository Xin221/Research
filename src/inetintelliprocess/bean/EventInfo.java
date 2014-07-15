package inetintelliprocess.bean;
/**
 * bean基本类（处理、搜索两部分用的共同的属性信息）
 */
import inetintelliprocess.processor.barschart.BarChartInfo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * 事件信息管理的业务类
 * 主要负责定义事件对象详细信息
 * 包括事件ID、事件名称、事件地点、事件时间等
 * 事件基本属性and基本操作
 *
 * 警告事件的构成
 */
public class EventInfo {

    private String eventID = "";
    private String eventName = "";
    private String eventLocation = "";
    private List<KeyWord> keyWords = null;
    private List<KeyWord> addrKeyWords = null;
    private Date eventTime = null;
    private float magnitude = 0;
    private double locx = 0;//5月30日新加
    private double locy = 0;//5月30日新加
    private String deathInfo = "";
    private String injureInfo = "";
    private String buildingInfo = "";
    private String url = "";
    private String webName = "";
    private Date deathUpdateTime = null;
    private String kwords = "";
    List<BarChartInfo> barCharInfos;
    String timePic;
    String newsPic;


    public List<BarChartInfo> getBarCharInfos() {
        return barCharInfos;
    }

    public void setBarCharInfos(List<BarChartInfo> barCharInfos) {
        this.barCharInfos = barCharInfos;
    }


    public float getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }




    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getTimePic() {
        return timePic;
    }

    public void setTimePic(String timePic) {
        this.timePic = timePic;
    }

    public String getNewsPic() {
        return newsPic;
    }

    public void setNewsPic(String newsPic) {
        this.newsPic = newsPic;
    }

    public void setKeyWords(List<KeyWord> keyWords) {
        this.keyWords = keyWords;
    }

    public void setAddrKeyWords(List<KeyWord> addrKeyWords) {
        this.addrKeyWords = addrKeyWords;
    }

    public List<KeyWord> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(ArrayList<KeyWord> keyWords) {
        this.keyWords = keyWords;
    }

    public List<KeyWord> getAddrKeyWords() {
        return addrKeyWords;
    }

    public void setAddrKeyWords(ArrayList<KeyWord> addrKeyWords) {
        this.addrKeyWords = addrKeyWords;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    //5月30日新加
    public void setLocx(double x){
        this.locx = x;
    }
    //5月30日新加
    public double getLocx(){
        return locx;
    }
    //5月30日新加
    public double getLocy(){
        return locy;
    }
    //5月30日新加
    public void setLocy(double y){
        this.locy = y;
    }

    public String getDeathInfo() {
        return deathInfo;
    }

    public void setDeathInfo(String deathInfo) {
        this.deathInfo = deathInfo;
    }

    public String getInjureInfo() {
        return injureInfo;
    }

    public void setInjureInfo(String injureInfo) {
        this.injureInfo = injureInfo;
    }

    public String getBuildingInfo() {
        return buildingInfo;
    }

    public void setBuildingInfo(String buildingInfo) {
        this.buildingInfo = buildingInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public Date getDeathUpdateTime() {
        return deathUpdateTime;
    }

    public void setDeathUpdateTime(Date deathUpdateTime) {
        this.deathUpdateTime = deathUpdateTime;
    }

    public String getKwords() {
        return kwords;
    }

    public void setKwords(String kwords) {
        this.kwords = kwords;
    }

    public EventInfo() {
        eventID = "";
        eventName = "";
        eventLocation = "";
        eventTime = null;
        magnitude = 0;
        locx = 0;//5月30日新加
        locy = 0;//5月30日新加
        timePic = eventID + ".png";
        newsPic = "";
    }

    //5月30日修改。。。
    public EventInfo(String ID, String name, String Location, Date time, float magniValue, double x, double y) {
        eventID = ID;
        eventName = name;
        eventLocation = Location;
        eventTime = time;
        magnitude = magniValue;
        locx = x;
        locy = y;
    }

    public void gethighfrequencyWord() throws ParseException
    {
        BarChartInfo barInfo = new BarChartInfo();
        EventInfo myEvent = new EventInfo(eventID) ;
        barCharInfos = barInfo.init(myEvent);
    }

    //5月30日
//	public EventInfo(String ID, String name, String Location, Date time, float magniValue) {
//		evenID = ID;
//		eventName = name;
//		eventLocation = Location;
//		eventTime = time;
//		magnitude = magniValue;
//	}

    public ArrayList<String> keyParser(String s) {
        String[] ss;
        ArrayList<String> list = new ArrayList<String>();
        ss = s.split(",");
        for (int i = 0; i < ss.length; i++) {
            list.add(ss[i]);
        }
        return list;
    }

    public void addKeyWord(KeyWord s) {
        if (keyWords == null) {
            keyWords = new ArrayList<KeyWord>();
        } else {
            for (int i = 0; i < keyWords.size(); i++) {
                if (keyWords.get(i).equals(s))
                    return;
            }
        }
        keyWords.add(s);
    }

    public void addAddrKeyWord(KeyWord k) {
        if (addrKeyWords == null) {
            addrKeyWords = new ArrayList<KeyWord>();
        } else {
            for (int i = 0; i < addrKeyWords.size(); i++) {
                if (addrKeyWords.get(i).equals(k))
                    return;
            }
        }
        addrKeyWords.add(k);
    }

    /**
     * 构造函数
     * @param ID 警告事件标识
     */
    public EventInfo(String ID) {
        this.eventID = ID;
    }

    /**
     * 获取日常搜索或警告事件搜索表的表名
     * @param ID 警告事件标识
     */
    public String getWebPageTbleName(String ID) {
        if (ID.equals(""))
            return "generalwebinfotbl";
        else{
            ID = ID.replace(".", "_");
            return ID;
        }
    }
    /**
     * 获取篇频信息表的表名
     * @param ID 警告事件标识
     */
    public String getAnaPageCountTbleName(String ID) {
        if (ID.equals(""))
            return "pagecount";
        else{
            ID = ID.replace(".", "_");
            return ID + "_pagecount";
        }
    }
    /**
     * 获取词频信息表的表名
     * @param ID 警告事件标识
     */
    public String getAnaWordCountTbleName(String ID) {
        if (ID.equals(""))
            return "wordinfo";
        else{
            ID = ID.replace(".", "_");
            return ID + "_wordinfo";
        }
    }
    /**
     * 获取特征空间向量信息表的表名
     * @param ID 警告事件标识
     */
    public String getAnaSpaceInfoTbleName(String ID) {
        if (ID.equals(""))
            return "spaceinfo";
        else{
            ID = ID.replace(".", "_");
            return ID + "_spaceinfo";
        }
    }
    /**
     * 获取时序统计信息表的表名
     * @param ID 警告事件标识
     */
    public String getAnaTimeStatTbleName(String ID) {
        if (ID.equals(""))
            return "wordtimestat";
        else{
            ID = ID.replace(".", "_");
            return ID + "_wordtimestat";
        }
    }



}
