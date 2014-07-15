package inetintelliprocess.processor.barschart;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.util.LogWriter;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BarChartInfo {
    private String word;
    private String xmltxt;
    private ArrayList<String> xTime = new ArrayList<String>();
    private ArrayList<String> yCount = new ArrayList<String>();
    private ArrayList<String> urlDisplay = new ArrayList<String>();
    private ArrayList<String> webNameDisplay = new ArrayList<String>();

    public String getWord() {
        return word;
    }

    public String getXmltxt() {
        return xmltxt;
    }

    public ArrayList<String> getxTime() {
        return xTime;
    }

    public ArrayList<String> getyCount() {
        return yCount;
    }

    public ArrayList<String> getUrlDisplay() {
        return urlDisplay;
    }

    public ArrayList<String> getWebNameDisplay() {
        return webNameDisplay;
    }

    public BarChartInfo(){

    }

    public BarChartInfo(String word, ArrayList<String> xTime, ArrayList<String> yCount, ArrayList<String> urlDisplay, ArrayList<String> webNameDisplay){
        this.word = word;
        this.xTime = xTime;
        this.yCount = yCount;
        this.urlDisplay = urlDisplay;
        this.webNameDisplay = webNameDisplay;
    }

    public List<BarChartInfo> init(EventInfo iEvent) throws ParseException{
        BarChartInfo barCharInfo = new BarChartInfo();
        List<BarChartInfo> barCharInfos = new ArrayList<BarChartInfo>();
        BarsChartDBO loader = new BarsChartDBO();
        barCharInfos = loader.loadBarChartInfo(iEvent.getAnaTimeStatTbleName(iEvent.getEventID()));
        List<BarChartInfo> barCharInfoClean = barCharInfo.generateDatas(barCharInfos);
        return barCharInfoClean;
    }



    @SuppressWarnings("unchecked")
    public void parseResultMerge(Element root) throws ParseException{
        if(root==null) return;
        String startTime = null;
        String endTime = null;
        SimpleDateFormat simples = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Element> elements = root.elements("span");
        int flag = 0;
        int index = 0;
        for(Element e : elements){
            index ++;
            String t1 = e.attributeValue("t1");
            String t2 = e.attributeValue("t2");
            String count = e.attributeValue("count");
            String representPageURL = e.attributeValue("representPageURL");
            String representWebName = e.attributeValue("representWebName");
            if (count.equals("0")){
                if(flag == 0){
                    Date date = simples.parse(t1);
                    flag = 1;
                    startTime = simple.format(date);
                } else {
                    if(index == elements.size()){
                        Date date = simples.parse(t2);
                        endTime = simple.format(date);
                        String xValue = startTime + "~" + endTime;
                        xTime.add(xValue);
                        yCount.add("0");
                        urlDisplay.add("");
                        webNameDisplay.add("");
                    }
                }
            }else{
                if(flag == 1){
                    Date date = simples.parse(t1);
                    endTime = simple.format(date);
                    flag = 0;
                    String xValue = startTime + "~" + endTime;
                    xTime.add(xValue);
                    yCount.add("0");
                    urlDisplay.add("");
                    webNameDisplay.add("");
                }
                Date date = simples.parse(t1);
                Date date1 = simples.parse(t2);
                startTime = simple.format(date);
                endTime = simple.format(date1);
                String xValue = startTime + "~" + endTime;
                xTime.add(xValue);
                String yValue = count;
                yCount.add(yValue);
                urlDisplay.add(representPageURL);
                webNameDisplay.add(representWebName);
            }
        }
    }

    public Element stringToXML(String srcStr){
//		if(srcStr.contains("&"))
//			srcStr = srcStr.replaceAll("&", "&amp;");
        SAXReader reader= new SAXReader();
        Document document;
        Element root = null;
//		System.out.println("srcStr is "+srcStr);
        if(srcStr==null) return root;
        ByteArrayInputStream stream = new ByteArrayInputStream(srcStr.getBytes());
        reader.setEncoding("GBK");
        try{
            //Reader reader = new InputStreamReader(stream,"utf-8");
            //System.out.println(String.valueOf(reader.read(stream)));
            document = reader.read(stream);
            root = document.getRootElement();
        }catch(DocumentException e){
            LogWriter.logger.warn("时序统计信息转换XML文件异常");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return root;
    }

    public List<BarChartInfo> generateDatas (List<BarChartInfo> BarChartInfos) throws ParseException{
        List<BarChartInfo> barChartInfoList= new ArrayList<BarChartInfo>();
        if(BarChartInfos!=null&&!BarChartInfos.isEmpty()){
            for(int j = 0; j < BarChartInfos.size(); j++){
                BarChartInfo bci = BarChartInfos.get(j);
                String str = bci.getXmltxt();
                if(str!=null&&!str.isEmpty())
                    str = str.replace("&", "&amp;");//8月27日添加，否则报错（url中）“对实体 "tid" 的引用必须以 ';' 分隔符结尾”
                Element root = bci.stringToXML(str);
                bci.parseResultMerge(root);
                String word = bci.getWord();
                ArrayList<String> xTimeContent = bci.getxTime();
                ArrayList<String> yCountContent = bci.getyCount();
                ArrayList<String> urlRepreContent = bci.getUrlDisplay();
                ArrayList<String> webRepreContent = bci.getWebNameDisplay();
                if(!xTimeContent.isEmpty() && !yCountContent.isEmpty())
                    barChartInfoList.add(new BarChartInfo(word, xTimeContent, yCountContent, urlRepreContent, webRepreContent));
            }
        }
        return barChartInfoList;
    }

}
