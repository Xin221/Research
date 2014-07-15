package inetintelliprocess.searchengine.rss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class GenerateRSSFeadXML {
    public GenerateRSSFeadXML(){
    }
    /*将提供的1个整型变量，6个字符串生成为xml文件*/
    public void createXml(File xmlfile, String pageId, String pageTitle, String pageTime, String pageContent, String url, String rssWebType, String rssWebName) {
		/*判断xml文件是否存在，存在继续写入，不存在就创建*/
        if(xmlfile.exists()){
            xmlfile.delete();

        }
        try {
				/*创建xml文件*/
            Document doc = new Document();
            Element eltRoot = new Element("pages");
            doc.setRootElement(eltRoot);
				
				
				/*创建表示一个page信息的各元素节点*/
            Element eltpage = new Element("page");
            Element eltpageTitle = new Element("pageTitle");
            Element eltpageTime = new Element("pageTime");
            Element eltpageContent = new Element("pageContent");
            Element elturl = new Element("url");
            Element eltrssWebType = new Element("rssWebType");
            Element eltrssWebName = new Element("rssWebName");
			
			
				/*把6个字符串分别设置为对应节点的文本内容*/
            eltpageTitle.setText(pageTitle);
            eltpageTime.setText(pageTime);
            eltpageContent.setText(pageContent);
            elturl.setText(url);
            eltrssWebType.setText(rssWebType);
            eltrssWebName.setText(rssWebName);
			
			
				/*将<pageTitle>,<pageTime>,<pageContent>,<url>,<keyWords>,<Abstract>元素添加为
				 * <page>元素的内容*/
            eltpage.addContent(eltpageTitle);
            eltpage.addContent(eltpageTime);
            eltpage.addContent(eltpageContent);
            eltpage.addContent(elturl);
            eltpage.addContent(eltrssWebType);
            eltpage.addContent(eltrssWebName);
			
				/*把整型参数s1转变为字符串tmp，为<page>元素设置属性pageId,值为tmp*/
            String tmp = pageId;
            eltpage.setAttribute("pageId",tmp);


            Element root = doc.getRootElement();
            root.addContent(eltpage);


            XMLOutputter xmlOut = new XMLOutputter();
            Format fmt = Format.getPrettyFormat();
            fmt.setEncoding("GB2312");
            fmt.setIndent("  ");
            xmlOut.setFormat(fmt);
//				xmlOut.output(doc, System.out);
            xmlOut.output(doc,new FileOutputStream(xmlfile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }

}



