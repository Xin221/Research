package inetintelliprocess.processor.newpubopinion;

import inetintelliprocess.util.Config;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;

public class LineChartPages{
    private static final long serialVersionUID = 1585397081043433662L;

    @SuppressWarnings("deprecation")
    public void saveChart()	throws IOException {

        // 获取数据集对象
        CategoryDataset dataset = NewsPubDataDBO.getDataset();

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

        categoryplot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        BarRenderer render = new BarRenderer();
        render.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        render.setItemLabelsVisible(true);
        categoryplot.setRenderer(render);

        ValueAxis rangeAxis = categoryplot.getRangeAxis();
        rangeAxis.setUpperMargin(0.15);

        // 将图表已数据流的方式返回给客户端
        String outputPath = Config.read("PICTURE_PATH");
        FileOutputStream out = new FileOutputStream(outputPath);
        ChartUtilities.writeChartAsJPEG(out,jfreechart,500,300);
        out.flush();
    }


}
