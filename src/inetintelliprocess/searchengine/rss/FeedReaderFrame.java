package inetintelliprocess.searchengine.rss;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.bean.KeyWord;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedReaderFrame {
    private List<RSSLinkInfo> rssLinkInfos = null;
    private List<KeyWord> keyWords = null;
    private List<KeyWord> eventKeywords = null;
    private SyndEntry eventSyndEntry = null;
    private File RSSFileXML;

    public boolean init() {
        rssLinkInfos = new ArrayList<RSSLinkInfo>();
        RSSDBO rssdbo = new RSSDBO();
        rssLinkInfos = rssdbo.loadRSSInfos();
        return true;
    }

    public boolean initKeyWords(){
        keyWords = new ArrayList<KeyWord>();
        String ConfigFname = inetintelliprocess.util.XmlProcessor.getKeyWordFilename();
        Document doc = inetintelliprocess.util.XmlProcessor.getXmlDoc(ConfigFname);

        NodeList nlist = doc.getElementsByTagName("key");
        for (int i = 0; i < nlist.getLength(); i++) {
            Element cn = (Element) (nlist.item(i));
            String keys = cn.getTextContent();
            keyWords.add(new KeyWord(keys));
        }
        return true;
    }

    public boolean initKeyWords(EventInfo eventInfo){
        eventKeywords = new ArrayList<KeyWord>();
        //RSSAnalysisTool rssAnalysisTool = new RSSAnalysisTool();
        List<KeyWord> eventInfoKeys = eventInfo.getAddrKeyWords();
        //rssAnalysisTool.getEventKeyWords(eventInfo);
        if(eventInfoKeys==null||eventInfoKeys.size() == 0) {
            return true;
        } else
            eventKeywords.addAll(eventInfoKeys);
        return true;
    }

    public FeedReaderFrame(EventInfo eventInfo){
        if (eventInfo == null) {
            init();
            initKeyWords();
        } else {
            init();
            initKeyWords();
            initKeyWords(eventInfo);
        }
        //System.out.println(eventInfo.getEventName()+"\t"+eventInfo.getEventLocation());
    }

    /**
     *处理RSS信息：
     *<p>
     *1.与缓存文件对比（生成缓存文件）,选出更新项
     *<p>
     *2.对更新项调用信息处理功能筛选
     *<p>
     *3.筛选信息入库
     */
    public void dealRSSFead(SyndFeed feed, String tname, String rssLinkURl, String rssWebType, String rssWebName, String rssExtractType){
        List<?> list = feed.getEntries();
        SyndEntry rssSyndEntry = (SyndEntry)list.get(0);
        if(null == eventSyndEntry)
            eventSyndEntry = new SyndEntryImpl();
        if(!eventSyndEntry.equals(rssSyndEntry)) {
            for (int i=0; i< list.size(); i++) {
                SyndEntry rssSyndEntryi = (SyndEntry)list.get(i);
                if(eventSyndEntry.equals(rssSyndEntryi)){
                    break;
                }
                RSSAnalysisTool rssAnalyTool = new RSSAnalysisTool();
                if(rssSyndEntryi.getDescription() != null && rssSyndEntryi.getDescription().getValue() != null){
                    String rssPageContent = rssSyndEntryi.getDescription().getValue();
                    if (rssPageContent != null || rssPageContent != ""){
                        if (isPageThemeValid(rssSyndEntryi.getTitle(), rssPageContent)) {
                            rssPageContent = rssAnalyTool.cleanTag(rssPageContent);
                            //System.out.println(rssLinkURl);
                            if(rssExtractType.equals("true")) {
                                rssPageContent = rssAnalyTool.findContent(rssPageContent);
                            }
                            RSSPageInfo rssPageInfo = new RSSPageInfo();
                            rssPageInfo.setPageTitle(rssSyndEntryi.getTitle());
                            Date publishedDate = rssSyndEntryi.getPublishedDate();
                            if(publishedDate!=null)
                                rssPageInfo.setPageTime(rssAnalyTool.DateFormat(publishedDate));
                            else
                                continue;
                            if(rssPageContent != null && !rssPageContent.isEmpty()) {
                                rssPageInfo.setPageContent(rssPageContent.trim());
                                if (rssPageContent.length() > 50)
                                    rssPageInfo.setAbstract(rssPageContent.substring(0, 50));
                                else
                                    rssPageInfo.setAbstract(rssPageContent);
                            }
                            else {
                                rssPageInfo.setPageContent(rssPageContent);
                                rssPageInfo.setAbstract(rssPageContent);
                            }
                            rssPageInfo.setUrl(rssSyndEntryi.getLink());
                            rssPageInfo.setKeyWords(rssSyndEntryi.getTitle());
                            rssPageInfo.setWebType(rssWebType);
                            rssPageInfo.setWebName(rssWebName);
                            try {
                                boolean rssWriteDB = rssPageInfo.writetoDB(tname, rssPageInfo);
                                if(!rssWriteDB) {
                                    GenerateRSSFeadXML p = new GenerateRSSFeadXML();
                                    p.createXml(RSSFileXML, rssPageInfo.getPageId(), rssSyndEntryi.getTitle(),
                                            rssAnalyTool.DateFormat(rssSyndEntryi.getPublishedDate()),
                                            rssPageContent, rssSyndEntryi.getLink(), rssWebType, rssWebName);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            eventSyndEntry = rssSyndEntry;
        }
    }

    public boolean runRSSFeed(EventInfo rssEvent, String tname){
        String RSSConfigFname = inetintelliprocess.util.XmlProcessor.getRSSFilename(rssEvent.getEventID());
        System.out.println(RSSConfigFname);
        RSSFileXML = new File(RSSConfigFname);
        int rssLinkNum = 0;
        //System.out.println(rssLinkInfos.size());
        for(RSSLinkInfo rssLinkInfo : rssLinkInfos) {
            try {
                //System.out.println(rssLinkInfo.getRssLinkURL());
                URL feedUrl = new URL(rssLinkInfo.getRssLinkURL());
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(feedUrl));
                dealRSSFead(feed, tname, rssLinkInfo.getRssLinkURL(), rssLinkInfo.getRssWebType(), rssLinkInfo.getRssWebName(), rssLinkInfo.getRssExtractType());
                RSSDBO.setRssExtractTypeDB(rssLinkInfo,true);
            }catch (Exception ex) {
                RSSDBO.setRssExtractTypeDB(rssLinkInfo,false);
            }
            rssLinkNum++;
        }
        if(rssLinkNum == rssLinkInfos.size()){
            return false;
        }
        return true;
    }

    public boolean isPageThemeValid(String tiltle, String content) {
        RSSAnalysisTool rssAnalyTool = new RSSAnalysisTool();
        boolean titleContains = rssAnalyTool.isKeyWordContains(tiltle, keyWords, eventKeywords);
        boolean contentContains = rssAnalyTool.isKeyWordContains(content, keyWords, eventKeywords);
        return titleContains || contentContains;
    }
	/*public static void main(String[] args) {
		FeedReaderFrame feedReader = new FeedReaderFrame();
		if(feedReader.init()) {
			for (int i = 0; i < feedReader.getRssLinkInfos().size(); i++) {
				System.out.println(feedReader.getRssLinkInfos().get(i).getID());
				System.out.println(feedReader.getRssLinkInfos().get(i).getRssLinkURL());
				System.out.println(feedReader.getRssLinkInfos().get(i).getRssWebName());
				System.out.println(feedReader.getRssLinkInfos().get(i).getRssWebType());
				System.out.println(feedReader.getRssLinkInfos().get(i).getRssExtractType());
			}
		}
	}*/
//	public static void main(String[] args) {
//		FeedReaderFrame feedReader = new FeedReaderFrame();
//		EventInfo myEvent = new EventInfo("ev111") ;
//		if(feedReader.initKeyWords(myEvent)) {
//			for(int i = 0; i < feedReader.eventKeywords.size(); i++){
//				System.out.println("eventKeywords: " + feedReader.eventKeywords.get(i));
//			}
//		}
//		System.out.println("事件存在的name和location："+feedReader.init(myEvent));
//		System.out.println("XML文件的主题词："+ feedReader.initKeyWords());
//	}

}
