package inetintelliprocess.processor.barschart;

import inetintelliprocess.util.Config;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class GenerateDatasets {
    private static List<String> xTimes = new ArrayList<String>();
    private static int pageCharNum = 10;
    private static int curPage;
    private static CategoryDataset dataset;
    //	private static int totalPage = (getxTimes().size()) / pageCharNum;
    public static int getPageCharNum() {
        return pageCharNum;
    }

    public static void setPageCharNum(int pageCharNum) {
        GenerateDatasets.pageCharNum = pageCharNum;
    }


    public static CategoryDataset getDataset() {
        return dataset;
    }

    public static void setDataset(CategoryDataset dataset) {
        GenerateDatasets.dataset = dataset;
    }

    public static int getCurPage() {
        return -curPage;
    }

    public static void setCurPage(int curPage) {
        GenerateDatasets.curPage = curPage;
    }

//	public static int getTotalPage() {
//		return totalPage;
//	}
//
//	public static void setTotalPage(int totalPage) {
//		GenerateDatasets.totalPage = totalPage;
//	}

    public static List<String> getxTimes() {
        return xTimes;
    }

    public static List<String> removeDuplicateWithOrder(List<BarChartInfo> BarChartInfos) throws ParseException{
        for(int j = 0; j < BarChartInfos.size(); j++){
            List<String> xValueList = BarChartInfos.get(j).getxTime();
            getxTimes().addAll(xValueList);
        }
        HashSet<String> hashSet = new HashSet<String>();
        List<String> newlist = new ArrayList<String>();
        for (Iterator<String> iterator = getxTimes().iterator(); iterator.hasNext();) {
            String element = (String) iterator.next();
            if (hashSet.add(element)) {
                newlist.add(element);
            }
        }
        getxTimes().clear();
        getxTimes().addAll(newlist);
        return getxTimes();
    }

    public List<BarChartInfo> generateDataList (List<BarChartInfo> BarChartInfos){
        List<BarChartInfo> barCharDataList = new ArrayList<BarChartInfo>();
        int curCharNumStart = getCurPage() * pageCharNum;
        int curCharNumend = getCurPage() * pageCharNum + pageCharNum;
        if(getxTimes().size() < curCharNumStart)
            return null;
        if(getxTimes().size() < curCharNumend)
            curCharNumend = getxTimes().size();
        List<String> xTimeValue = new ArrayList<String>();
        for (int i = curCharNumStart; i < curCharNumend; i++){
            xTimeValue = (List<String>)getxTimes().subList(curCharNumStart, curCharNumend);
        }
//		System.out.println("BarChartInfos length is : "+BarChartInfos.size());
        for(int j = 0; j < BarChartInfos.size(); j++){
            ArrayList<String> xTimeContentPage = new ArrayList<String>();
            ArrayList<String> yCountContentPage = new ArrayList<String>();
            ArrayList<String> urlRepreContentpage = new ArrayList<String>();
            ArrayList<String> webRepreContentpage = new ArrayList<String>();
            BarChartInfo barCharInfoData = BarChartInfos.get(j);
            String word = barCharInfoData.getWord();
            ArrayList<String> xTimeContent = barCharInfoData.getxTime();
            ArrayList<String> yCountContent = barCharInfoData.getyCount();
            ArrayList<String> urlRepreContent = barCharInfoData.getUrlDisplay();
            ArrayList<String> webRepreContent = barCharInfoData.getWebNameDisplay();
            for(int i = 0; i < xTimeValue.size(); i++) {
                int index = xTimeContent.indexOf(xTimeValue.get(i));
                if(index != -1){
                    xTimeContentPage.add(xTimeContent.get(index));
                    yCountContentPage.add(yCountContent.get(index));
                    urlRepreContentpage.add(urlRepreContent.get(index));
                    webRepreContentpage.add(webRepreContent.get(index));
                }
            }
//    		if(!xTimeContentPage.isEmpty())
            barCharDataList.add(new BarChartInfo(word, xTimeContentPage, yCountContentPage, urlRepreContentpage, webRepreContentpage));
        }
        return barCharDataList;
    }

    public static CategoryDataset createDataset(List<BarChartInfo>  barChartInfoList) {
        DefaultCategoryDataset defaultdataset = new DefaultCategoryDataset();
        if(barChartInfoList!=null&&!barChartInfoList.isEmpty())
        {
            for(int j = 0; j < barChartInfoList.size(); j++){
                ArrayList<String> xValueList = barChartInfoList.get(j).getxTime();
                ArrayList<String> yValueList = barChartInfoList.get(j).getyCount();
                if(!xValueList.isEmpty() && !yValueList.isEmpty()){
                    for (int i = 0; i < xValueList.size(); i++) {
                        String category = xValueList.get(i);
                        defaultdataset.addValue(Integer.parseInt(yValueList.get(i)), barChartInfoList.get(j).getWord(), category);
                    }
                }else
                    defaultdataset.addValue(0, barChartInfoList.get(j).getWord(), "");
            }
        }
        else
            return null;
        return defaultdataset;
    }

    public static void makePicture(String evtId)
    {
        // 获取数据集对象
        CategoryDataset dataset;
        dataset = GenerateDatasets.getDataset();
        File file = new File(Config.read("PICTURE_PATH")  + evtId +  ".png");
        if(file.exists())
            file.delete();
        if (dataset != null){
            int rowCount  = dataset.getRowCount();
//			dataset = createDataset();
            // 创建图形对象
            JFreeChart jfreechart = ChartFactory.createBarChart3D("事件衍生信息时序统计分析图",
                    "时间范围", "计数", dataset, PlotOrientation.VERTICAL, true, true,
                    true);
            // 获得图表区域对象
            CategoryPlot categoryPlot = (CategoryPlot) jfreechart.getPlot();
            // 设置网格线可见

            categoryPlot.setDomainGridlinesVisible(true);
            // 获得x轴对象
            CategoryAxis categoryAxis = categoryPlot.getDomainAxis();
            // 设置x轴显示的分类名称的显示位置，如果不设置则水平显示
            // 设置后，可以斜像显示，但分类角度，图表空间有限时，建议采用，横轴显示旋转
            categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.45D));
            //分类轴边距
//			categoryAxis.setCategoryMargin(0.02D);
            //分类轴下（左）边距
            if (rowCount < 3)
                categoryAxis.setLowerMargin(0.4D);
            else
                categoryAxis.setLowerMargin(0.2D);
            //分类轴上（右）边距
            categoryAxis.setUpperMargin(0.02D);
            // 获显示图形对象
//			BarRenderer3D barRenderer3d = (BarRenderer3D) categoryPlot.getRenderer();
            //设置柱子宽度
//			barRenderer3d.setMaximumBarWidth(0.3D);
            // 横轴上的 Lable 是否完整显示
            categoryAxis.setMaximumCategoryLabelWidthRatio(1f);
            //设置柱的颜色
//			barRenderer3d.setSeriesPaint(0, new Color(204, 222, 223));
            //显示柱子上的数字
//			barRenderer3d.setIncludeBaseInRange(true);
//			barRenderer3d.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator()); 
//			barRenderer3d.setBaseItemLabelsVisible(true);
            // 设置不显示边框线
//			barRenderer3d.setDrawBarOutline(false);
            // 将图表已数据流的方式返回给客户端
//			System.out.println("dataset.getRowCount() " + dataset.getRowCount());
//			System.out.println("dataset.getColumnCount() " + dataset.getColumnCount());


            int imgWidth = 0;
            if(rowCount < 5)
                imgWidth = rowCount*230;
            else
                imgWidth = rowCount*110;
            if(rowCount == 0)
                imgWidth = 230;
            try {
                categoryPlot.setBackgroundPaint(ChartColor.WHITE);
                //ChartUtilities.saveChartAsJPEG(new File(Config.read("PICTURE_PATH") + evtId +  ".jpg"), jfreechart, imgWidth, 400);
                ChartUtilities.saveChartAsPNG(new File(Config.read("PICTURE_PATH")  + evtId +  ".png"), jfreechart, imgWidth, 400);
                System.out.println("生成图片成功！");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
