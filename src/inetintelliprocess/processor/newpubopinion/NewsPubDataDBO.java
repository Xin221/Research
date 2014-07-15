package inetintelliprocess.processor.newpubopinion;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.util.Config;
import inetintelliprocess.util.LogWriter;
import inetintelliprocess.processor.newpubopinion.NewsPubDataDBO;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class NewsPubDataDBO {
    private static CategoryDataset dataset;

    public static CategoryDataset getDataset() {
        return dataset;
    }

    public static void setDataset(CategoryDataset dataset) {
        NewsPubDataDBO.dataset = dataset;
    }

    public static CategoryDataset init(EventInfo iEvent) {
        List<NewsPubOpinion> pubList = NewsPubDataDBO.loadLineConfigFromDB(iEvent.getEventID());
        CategoryDataset catdata = createDataset(pubList);
        return catdata;
    }

    public List<NewsPubLately> loadNewsPubLately(String eventID){
        String sql = null;
        List<NewsPubLately> result = new ArrayList<NewsPubLately>();
        try {
            //sql = "select pageTitle, DATE(pageTime) as day , url, webName from " + eventID + " as t1 inner join (select distinct DATE(pageTime) ss from " + eventID + " order by pageTime desc limit 0,1) as t2 on DATE(t1.pageTime) =t2.ss";  
            sql = "select pageTitle, DATE(pageTime) as day , url, webName from " + eventID + " as t1 inner join (select distinct DATE(pageTime) as ss from " + eventID + " where pageTime is not null and pageTime <> 'null' order by pageTime desc limit 0,1) as t2 on DATE(pageTime) =t2.ss";
            result = DBConnect.excuteQuery(NewsPubLately.class, sql);
        } catch (Exception e) {
            MDC.put("eventID",eventID);
            LogWriter.logger.warn("加载事件" + eventID + "最近一天新闻发布情况异常");
            e.printStackTrace();
        }
        return result ;
    }

    public static List<NewsPubOpinion> loadLineConfigFromDB(String eventID) {
        String sql = null;
        List<NewsPubOpinion> res = new ArrayList<NewsPubOpinion>();
        sql = "SELECT * FROM publicopinioninfo where eventID = '" + eventID + "' order by dayTime desc limit 0,30";
        try {
            res = DBConnect.excuteQuery(NewsPubOpinion.class, sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            MDC.put("eventID",eventID);
            LogWriter.logger.error("获取舆情错误");
            e.printStackTrace();
        }
        return res;
    }
    /**
     *
     * @param eventID 事件ID
     * @return 获取唯一pubOpinion的一段拼好的文字
     */
    public static StringBuffer getArticle(String eventID)
    {
        List<PubOpinionInfo> pubList = new ArrayList<PubOpinionInfo>();
        PubOpinionInfo pub = new PubOpinionInfo();
        String sql = null;
        sql = "SELECT pubOpinion,lastUpdateTime FROM publicopinioninfo where eventID = '" + eventID + "' order by dayTime desc limit 1";
        try {
            NewsPubOpinion newsPub = DBConnect.excuteReadOneRow(NewsPubOpinion.class, sql);
            pubList = pub.getPubOpinionList(newsPub.getPubOpinion(), newsPub.getLastUpdateTime());
        }catch(Exception e)
        {
            MDC.put("eventID", eventID);
            LogWriter.logger.error("获取pubOpinion失败");
        }
        return SplitArticle(pubList, eventID);
    }

    public static StringBuffer SplitArticle(List<PubOpinionInfo> pub, String eventID)
    {
        StringBuffer Article = new StringBuffer();
        for(PubOpinionInfo puuindex: pub)
        {
            String b = "<p style='LINE-HEIGHT:35px' style='text-indent: 2em'>截止到" + puuindex.getMediaUpdatedDate().toString() +  "，" + puuindex.getMediaName() + "发表了" + puuindex.getMediaArticleCount() + "篇" + "关于" + eventID + "的文章" + "。<b>其中代表文章标题为：</b>" + puuindex.getMediaRepresentArticle() + "。<b>内容简介：</b>" + puuindex.getMediaRepresentArticleAbstract() + "<b>该文章网址为：</b>" + puuindex.getMediaRepresentArticleURL()+"<br><p>";
            Article.append(b);
            b = null;
            puuindex = null;
        }
        return Article;
    }

    /**
     * 返回生成折线图需要的数据集
     *
     * @return
     */
    public static CategoryDataset createDataset(List<NewsPubOpinion> pubList) {
        DefaultCategoryDataset defaultdataset = new DefaultCategoryDataset();
        if(pubList==null||pubList.isEmpty()) return null;
        for (int i=pubList.size()-1; i>=0; i--){
            NewsPubOpinion pub = pubList.get(i);
            defaultdataset.addValue(pub.getNewsAticleCount(), "网站搜索信息条数", pub.getDayTime());
            defaultdataset.addValue(pub.getRssArticleCount(), "RSS搜索信息条数", pub.getDayTime());
        }
        return defaultdataset;
    }

    @SuppressWarnings("deprecation")
    public static void makePicture(String evtId)
    {
        File file = new File(Config.read("PICTURE_PATH") +"line" + evtId +  ".png");
        if(file.exists())
            file.delete();
        // 获取数据集对象
        CategoryDataset dataset = NewsPubDataDBO.getDataset();
        if(dataset==null) return;
        int rowCount  = dataset.getColumnCount();
        JFreeChart jfreechart = ChartFactory.createBarChart("最新日新闻发布统计图",
                null, "发布新闻统计量", dataset, PlotOrientation.VERTICAL, true, false,
                false);
        // 设置图表的子标题-----------------------
        jfreechart.addSubtitle(new TextTitle("按日期"));
        // 获得图表区域对象
        CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
        categoryplot.setBackgroundPaint(Color.white);
        categoryplot.setRangeGridlinesVisible(true);
        categoryplot.setDomainGridlinesVisible(true);

        CategoryAxis domainAxis = categoryplot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.45D));
        categoryplot.setDomainAxis(domainAxis);
        categoryplot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        BarRenderer render = new BarRenderer();
        render.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        render.setItemLabelsVisible(true);
        categoryplot.setRenderer(render);
        //render.setMaximumBarWidth(0.04);
        render.setItemMargin(0);
        ValueAxis rangeAxis = categoryplot.getRangeAxis();
        rangeAxis.setUpperMargin(0.15);
        int imgWidth = 0;
        if(rowCount < 10)
            imgWidth = 500;
        else
            imgWidth = rowCount*30;
        try {
            //ChartUtilities.saveChartAsJPEG(new File(Config.read("PICTURE_PATH") +"line" + evtId +  ".jpg"), jfreechart, 500, 300);
            ChartUtilities.saveChartAsPNG(new File(Config.read("PICTURE_PATH") +"line" + evtId +  ".png"), jfreechart, imgWidth, 300);
            System.out.println("生成line图片成功！");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        //System.out.print(NewsPubDataDBO.getArticle("eqw092_2n14_220121112061501").toString());
        List<NewsPubLately> nplist = new ArrayList<NewsPubLately>();
        NewsPubDataDBO dao = new NewsPubDataDBO();
        System.out.println(NewsPubDataDBO.getArticle("n05253w07284920140311215705").toString());
        nplist = dao.loadNewsPubLately("n05253w07284920140311215705");
        if(nplist.isEmpty()) System.out.println("nplist is empty!");
        System.out.println(nplist.size());
        for(NewsPubLately e : nplist) {
            System.out.println(e.getPageTitle());
            System.out.println(e.getDay());
            System.out.println(e.getUrl());
            System.out.println(e.getWebName());
        }
    }
}
