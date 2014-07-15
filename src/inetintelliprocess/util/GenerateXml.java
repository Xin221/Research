package inetintelliprocess.util;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.searchengine.searchers.WebPageInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.MDC;

public class GenerateXml {
    public String toPubB12(List<WebPageInfo> list, String eventID){
        String ret = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        String eventName = null;
        String eventLocation = null;
        double longtitude = 0.0;
        double latitude = 0.0;
        String eventTime = null;
        float magnitude = 0;
        String deathInfo = null;
        String injureInfo = null;
        String buildingInfo = null;
        String detectTime = null;
        String sql = new String();
        EventInfo eventInfo = null;
        try{
            sql = "select * from eventinfo where eventID='"+eventID+"'";
            eventInfo = DBConnect.excuteReadOneRow(EventInfo.class,sql);
            if(eventInfo!=null){
                eventName = eventInfo.getEventName();
                eventLocation = eventInfo.getEventLocation();
                longtitude = eventInfo.getLocx();
                latitude = eventInfo.getLocy();
                eventTime = String.valueOf(eventInfo.getEventTime());
                magnitude = eventInfo.getMagnitude();
                deathInfo = eventInfo.getDeathInfo();
                injureInfo = eventInfo.getInjureInfo();
                buildingInfo = eventInfo.getBuildingInfo();
                Date tt = eventInfo.getDeathUpdateTime();
                detectTime = (tt==null)?"":tt.toString();
            }
        } catch (Exception e) {
            MDC.put("eventID", eventID);
            LogWriter.logger.error("查询eventinfo表中事件"+eventID+"不成功");
            e.printStackTrace();
        }
        ret = ret + "<internetInfo ID=\"" + eventID + "\">";
        ret = ret + "<eventTime>" + String.valueOf(eventTime) + "</eventTime>";
        ret = ret + "<magnitude>" + String.valueOf(magnitude) + "</magnitude>";
        if(eventLocation==null||eventLocation.isEmpty())
            ret = ret + "<location>" + eventName + "</location>";
        else
            ret = ret + "<location>" + eventName + "," + eventLocation + "</location>";
        ret = ret + "<longtitude>" + String.valueOf(longtitude) + "</longtitude>";
        ret = ret + "<latitude>" + String.valueOf(latitude) + "</latitude>";
        ret = ret + "<deathInfo>" + deathInfo + "</deathInfo>";
        ret = ret + "<injureInfo>" + injureInfo + "</injureInfo>";
        ret = ret + "<buildingInfo>" + buildingInfo + "</buildingInfo>";
        if(detectTime==null||detectTime.isEmpty())
            ret = ret + "<detectTime></detectTime>";
        else
            ret = ret + "<detectTime>" + detectTime + "</detectTime>";
        ret = ret + "<publicopinion>";
        String str = new String();
        if(list!=null&&!list.isEmpty()){
            for(WebPageInfo info:list){
                str = str + "<item>";
                str = str + "<title>" + info.getPageTitle().trim() + "</title>";
                str = str + "<time>" + info.getPageTime() + "</time>";
                str = str + "<content>" + info.getPageContent().trim() + "</content>";
                str = str + "<url>" + info.getUrl() + "</url>";
                str = str + "<webname>" + info.getWebName() + "</webname>";
                str = str + "</item>";
            }
            ret = ret + str;
        }
        ret = ret + "</publicopinion></internetInfo>";
        return ret;
    }

    public List<WebPageInfo> getAllInfo(String tblName) {
        String sql = null;
        List<WebPageInfo> res = new ArrayList<WebPageInfo>();
        try {
            sql = "SELECT * FROM "+tblName+" where sendTo='0'";
            res = DBConnect.excuteQuery(WebPageInfo.class, sql);
        } catch (Exception e) {
            MDC.put("eventID", tblName);
            LogWriter.logger.error("获取数据表所有网页信息错误");
            e.printStackTrace();
        }
        return res;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
//		String B12URL = "http://192.168.0.114/zhgl/services/B11DisasterInfoService";
        GenerateXml gxml = new GenerateXml();
        String toB12 = gxml.toPubB12(gxml.getAllInfo("S26870E11998020131021172341"), "S26870E11998020131021172341");
        if(toB12!=null){
            System.out.println("发送互联网灾情、舆情至B12包：");
            System.out.println("toB12 is :\n"+toB12);
//			String method = "B11putDisinfoB12Start_Service";		//方法是他们给的
//			StringBuffer sb = new StringBuffer();
//			sb.append(toB12);		//这里写发送的内容
//			Object[] opAddEntryArgs = new Object[] { sb.toString() };
//			String result = new inetintelliprocess.searchengine.frame.SearcherManager("S26870E11998020131021172341").executeService(opAddEntryArgs, B12URL, method, true);
//			System.out.println("客户端接收到  " + result);
            System.out.println("发送互联网灾情、舆情至B12包结束！");
        }
    }
}

