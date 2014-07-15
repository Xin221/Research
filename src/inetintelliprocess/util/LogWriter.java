package inetintelliprocess.util;
/**
 * util辅助工具类
 */
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
/**
 *
 * 日志管理的业务类
 *
 */
public class LogWriter {
    public static Logger logger = Logger.getLogger(LogWriter.class);

    public static void writelogo(String s) {
        String t = new Timestamp(new Date().getTime()).toString();
        s = "时间:" + t + "::" + s;
    }

    public static void main(String[] args){
        LogWriter.logger.info("试一试");
    }
}
