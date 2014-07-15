package inetintelliprocess.util;
/**
 * util辅助工具类
 */
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
/**
 * 对所有用到的xml文件解析操作
 * XML文件解析工具类
 * @author WQH
 */
public class XmlProcessor {

    /**
     * 获取主题词典的XML文件名称
     */
    public static String getDicFilename() {
        return Config.read("THEME_WORD_FILE");
    }

    /**
     * 获取搜索条件关键词的XML文件名称
     */
    public static String getKeyWordFilename() {
        return Config.read("THEME_WORD_FILE");
    }

    /**
     * 获取警告事件爬取RSS文本对象生成的XML文件名称
     * @param eventRSS 警告事件标识
     */
    public static String getRSSFilename(String eventRSS) {
        if(eventRSS != null && eventRSS != "")
            return Config.read("RSS_INFO_DYNAMIC_FILE") + eventRSS +".xml";
        else
            return Config.read("RSS_INFO_FILE");
        //return null;
    }

    /**
     * 用第三方工具包解析XML文件
     * @param fname XML文件名称
     */
    public static Document getXmlDoc(String fname) {
        try {
            File file = new File(fname);
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            return doc;
        } catch (Exception exp) {
            LogWriter.logger.fatal(fname+"XML解析错误");
        }
        return null;
    }

    public static void saveXml(Document doc, String fname) {
        try {
            Source xmlSource = new DOMSource(doc);
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer();
            Result result = new StreamResult(new File(fname));
            transformer.transform(xmlSource, result);
        } catch (Exception exp) {
            LogWriter.logger.error(fname+"XML重新载入错误");
        }
    }


}
