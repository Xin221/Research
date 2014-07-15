package inetintelliprocess.util;
/**
 * util辅助工具类
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * 读取数据库连接信息文件的类
 * 数据库配置文件
 *
 */
public class Config {

    public static String read(String key) {
        String value = null;
        try {
            InputStream inputStream = new BufferedInputStream(
                    new FileInputStream(findFile("parameters.properties")));
            Properties properties = new Properties();
            properties.load(inputStream);
            value = (String) properties.get(key);
        } catch (FileNotFoundException e) {
            //LogWriter.logger.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            //LogWriter.logger.error(e);
            e.printStackTrace();
        }
        return value;
    }

    public static String read(String key, String file) {
        String value = null;
        try {
            InputStream inputStream = new BufferedInputStream(
                    new FileInputStream(file));
            Properties properties = new Properties();
            properties.load(inputStream);
            value = (String) properties.get(key);
        } catch (FileNotFoundException e) {
            LogWriter.logger.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
        return value;
    }

    public static File findFile(String fileName) {
        File file = null;
        if (new File(fileName).exists()) {
            file = new File(fileName);
        } else if (new File("WebRoot/config/" + fileName).exists()) {
            file = new File("WebRoot/config/" + fileName);
        } else if (new File("webapps/" + Parameters.PROJECT_NAME + "/config/"
                + fileName).exists()) {
            file = new File("webapps/" + Parameters.PROJECT_NAME + "/config/"
                    + fileName);
        } else if (new File("../webapps/" + Parameters.PROJECT_NAME
                + "/config/" + fileName).exists()) {
            file = new File("../webapps/" + Parameters.PROJECT_NAME
                    + "/config/" + fileName);
        }
        return file;
    }

    public static void main(String[] args) {
        System.out.println(Config.read("DATABASE_IP"));
    }

}
