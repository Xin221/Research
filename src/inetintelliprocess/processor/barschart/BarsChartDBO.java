package inetintelliprocess.processor.barschart;

import inetintelliprocess.dbc.DBConnect;
import inetintelliprocess.util.LogWriter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;

public class BarsChartDBO {

    public List<BarChartInfo> loadBarChartInfo(String tblName) {
        String sql = null;
        List<BarChartInfo> result = new ArrayList<BarChartInfo>();
        try {
            sql = "select word, xmltxt from " + tblName + " ";
            result = DBConnect.excuteQuery(BarChartInfo.class, sql);
        } catch (Exception e) {
            MDC.put("eventID",tblName.split("_")[0]);
            LogWriter.logger.warn("" + tblName + "加载时序统计信息异常");
            e.printStackTrace();
        }
        return result ;
    }
}
