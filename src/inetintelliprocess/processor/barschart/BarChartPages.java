package inetintelliprocess.processor.barschart;

import inetintelliprocess.util.Config;

import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;


public class BarChartPages{

    private static final long serialVersionUID = -566713680648708515L;


    public void saveChart() throws IOException {
//		System.out.println("!!!!!@!!!!!!!!!!!!!");
        // 获取数据集对象
        CategoryDataset dataset;
        dataset = GenerateDatasets.getDataset();
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
            //下面为7月份新添加的
            String outputPath = Config.read("PICTURE_PATH");
            FileOutputStream out = new FileOutputStream(outputPath);
            ChartUtilities.writeChartAsJPEG(out,jfreechart,imgWidth,400);
            out.flush();
        }
    }

}
