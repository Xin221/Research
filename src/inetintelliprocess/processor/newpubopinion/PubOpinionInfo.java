package inetintelliprocess.processor.newpubopinion;

import inetintelliprocess.util.LogWriter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * NewsPubOpinion类里面pubOpinion从root里解析出的bean
 * @author RoseRye
 *
 */
public class PubOpinionInfo {
    private String mediaName;
    private String mediaArticleCount;
    private String mediaRepresentArticle;
    private String mediaRepresentArticleURL;
    private String mediaRepresentArticleAbstract;
    private String mediaUpdatedDate;

    public String getMediaUpdatedDate() {
        return mediaUpdatedDate;
    }

    public void setMediaUpdatedDate(String updatedDate) {
        this.mediaUpdatedDate = updatedDate;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getMediaArticleCount() {
        return mediaArticleCount;
    }

    public void setMediaArticleCount(String mediaArticleCount) {
        this.mediaArticleCount = mediaArticleCount;
    }

    public String getMediaRepresentArticle() {
        return mediaRepresentArticle;
    }

    public void setMediaRepresentArticle(String mediaRepresentArticle) {
        this.mediaRepresentArticle = mediaRepresentArticle;
    }

    public String getMediaRepresentArticleURL() {
        return mediaRepresentArticleURL;
    }

    public void setMediaRepresentArticleURL(String mediaRepresentArticleURL) {
        this.mediaRepresentArticleURL = mediaRepresentArticleURL;
    }

    public String getMediaRepresentArticleAbstract() {
        return mediaRepresentArticleAbstract;
    }

    public void setMediaRepresentArticleAbstract(
            String mediaRepresentArticleAbstract) {
        this.mediaRepresentArticleAbstract = mediaRepresentArticleAbstract;
    }

    @SuppressWarnings("unchecked")
    public List<PubOpinionInfo> parsePubOpinion(Element root, String updatedDate1) {
        List<Element> pubOpinionParsed = new ArrayList<Element>();
        List<PubOpinionInfo> pubOpinionList = new ArrayList<PubOpinionInfo>();
        pubOpinionParsed = root.elements("publicOpinion");
        for(Element e: pubOpinionParsed)
        {
            PubOpinionInfo pub = new PubOpinionInfo();
            pub.setMediaName(e.attributeValue("MediaName"));
            pub.setMediaArticleCount(e.attributeValue("MediaArticleCount"));
            pub.setMediaRepresentArticle(e.attributeValue("MediaRepresentArticle"));
            pub.setMediaRepresentArticleURL(e.attributeValue("MediaRepresentArticleURL"));
            pub.setMediaRepresentArticleAbstract(e.attributeValue("MediaRepresentArticleAbstract"));
            pub.setMediaUpdatedDate(updatedDate1);
            pubOpinionList.add(pub);
        }
        return pubOpinionList;

    }

    public Element stringToXML(String srcStr) {
        if(srcStr.contains("&"))
            srcStr = srcStr.replaceAll("&", "&amp;");
        ByteArrayInputStream stream;
        SAXReader reader = new SAXReader();
        Document document;
        Element root = null;
        try {
            stream = new ByteArrayInputStream(srcStr.getBytes());
            reader.setEncoding("GB2312");
            document = reader.read(stream);
            root = document.getRootElement();
        } catch (DocumentException e) {
            LogWriter.logger.warn("舆情简报信息转换XML文件异常");
//			System.out.println(e.getMessage());
        }
        return root;
    }

    public List<PubOpinionInfo> getPubOpinionList(String root, String updatedDate1) {
        List<PubOpinionInfo> pubOpinionList = new ArrayList<PubOpinionInfo>();
        pubOpinionList = parsePubOpinion(stringToXML(root),updatedDate1);
        return pubOpinionList;
    }

}
