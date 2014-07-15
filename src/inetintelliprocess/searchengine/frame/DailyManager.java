package inetintelliprocess.searchengine.frame;
/**
 * frame搜索器的框架
 */
import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.searchengine.searchers.dailysearcher.DailyCoreSearcher;
import inetintelliprocess.searchengine.searchers.dailysearcher.DailySearcher;
import inetintelliprocess.util.LogWriter;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * 日常搜索管理器
 *
 */
public class DailyManager extends Thread{
    private ArrayList<DailySearcher> threadList = new ArrayList<DailySearcher>();//日常搜索队列
    private ArrayList<DailyCoreSearcher> coreSearches = null;//日常搜索核心队列
    private DailySearcher generalSearcher = new DailySearcher(null);//普通日常搜索
    private ArrayList<KeyWord> kwords = null;//关键字列表，test中keyword



    // 加RSS订阅内容***************
    private inetintelliprocess.searchengine.rss.FeedReaderFrame m_rssfeed = null;
    private inetintelliprocess.processor.wordfeaturemanage.WordFeatureThread m_analysis = null;
    private static Object lock = new Object();
    private boolean running = false;



    /**
     * 判断日常搜索器是否在运行
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 启动日常搜索器
     */
    public void startWork() {

        synchronized (lock) {
            //如果running==true则正在执行
            //否则启动搜索器并设置running为true
            if (!running) {
                running = true;
                start();
            } else {
                System.out.println("mgr正在执行");
            }
        }
        //setStartState();
    }

    public void stopWork () {
        while (true) {
            boolean flag = true;
            for (DailyCoreSearcher cs : coreSearches) {
                if (cs.isRunning()) {
                    cs.stopWork();
                    flag = false;
                    break;
                }
            }
            if (generalSearcher.isRunning()) {
                generalSearcher.stopWork();
                flag = false;
            }
            if (flag) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//		setEndState();
        running = false;
    }

    public ArrayList<DailySearcher> getThreadList() {
        return threadList;
    }

    public void setThreadList(ArrayList<DailySearcher> threadList) {
        this.threadList = threadList;
    }

    public ArrayList<DailyCoreSearcher> getCoreSearches() {
        return coreSearches;
    }

    public void setCoreSearches(ArrayList<DailyCoreSearcher> coreSearches) {
        this.coreSearches = coreSearches;
    }

    public DailySearcher getGeneralSearcher() {
        return generalSearcher;
    }

    public void setGeneralSearcher(DailySearcher generalSearcher) {
        this.generalSearcher = generalSearcher;
    }

    public ArrayList<KeyWord> getKwords() {
        return kwords;
    }

    public void setKwords(ArrayList<KeyWord> kwords) {
        this.kwords = kwords;
    }

    public inetintelliprocess.searchengine.rss.FeedReaderFrame getM_rssfeed() {
        return m_rssfeed;
    }

    public void setM_rssfeed(
            inetintelliprocess.searchengine.rss.FeedReaderFrame mRssfeed) {
        m_rssfeed = mRssfeed;
    }

    public inetintelliprocess.processor.wordfeaturemanage.WordFeatureThread getM_analysis() {
        return m_analysis;
    }

    public void setM_analysis(
            inetintelliprocess.processor.wordfeaturemanage.WordFeatureThread m_analysis) {
        this.m_analysis = m_analysis;
    }

    /**
     * 初始化
     * 解析test.xml文件，形成coreSearches、kwords、generalSearcher列表
     * @return
     */
    public boolean init() {
        String ConfigFname = inetintelliprocess.util.XmlProcessor
                .getKeyWordFilename(); // 获取关键字文件路径 test.xml
        this.coreSearches = new ArrayList<DailyCoreSearcher>();//核心搜索列表

        this.kwords = new ArrayList<KeyWord>();//关键字列表
        try {
            File file = new File(ConfigFname);
            //DocumentBuilderFactory定义工厂API，使应用程序能够从 XML 文档获取生成 DOM 对象树的解析器
            //利用这个工厂来获得具体的解析器对象
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            //newInstance()获取 DocumentBuilderFactory 的新实例
            //DocumentBuilder定义 API， 使其从 XML 文档获取 DOM 文档实例
            DocumentBuilder builder = factory.newDocumentBuilder();//获取 具体的DOM解析器
            Document doc = builder.parse(file);//将给定文件的内容解析为一个 XML 文档，并且返回一个新的 DOM Document 对象
            //得到所有<coreWeb>标签对应的Node对象的一个列表
            NodeList nlist = doc.getElementsByTagName("coreWeb");

            for (int i = 0; i < nlist.getLength(); i++) {
                Element cn = (Element) (nlist.item(i));//得到第i个Node对象
                NodeList cnlist = cn.getChildNodes();//DOM树中当前Node的子Nodes
                for (int j = 0; j < cnlist.getLength(); j++) {//对每一个核心网站
                    Node urlNode = cnlist.item(j);

                    if (urlNode.getNodeName().equals("URL")) {
                        DailyCoreSearcher cs = new DailyCoreSearcher(this);//只做了parentMgr = this（当前日常搜索器）
                        if (cs.init((Element) (urlNode))) {//初始化，加载搜索模板
                            this.coreSearches.add(cs);//加入核心搜索队列
                        }
                    }
                }
            }

            //与上类似，生成关键字列表
            nlist = doc.getElementsByTagName("key");
            for (int i = 0; i < nlist.getLength(); i++) {
                Element cn = (Element) (nlist.item(i));
                String s = cn.getTextContent();
                KeyWord kw = new KeyWord();
                kw.setWord(s);
                System.out.println(i+": "+s);
                this.kwords.add(kw);
            }
            this.generalSearcher = new DailySearcher(this);
            this.generalSearcher.init();//主要是把url初始化为TargetLink类中DefLinkURL缺省值"http://news.qq.com/"
        } catch (Exception exp) {
            LogWriter.logger.fatal("XML文件" + ConfigFname + "解析失败");
        }
        return true;
    }
    /**
     * 打印输出每一个日常核心搜索的首站、名称、标签名、属性名等信息
     */
    public void printMe() {
        for (DailyCoreSearcher cs : coreSearches) {
            String s = cs.getMyTemplate().getBaseURI() + "("
                    + cs.getMyTemplate().getName() + ")::<"
                    + cs.getMyTemplate().getTagName() + " "
                    + cs.getMyTemplate().getAttName() + "="
                    + cs.getMyTemplate().getValue() + ">";
            System.out.println(s);
        }
    }

    /**
     * 启动日常搜索
     */
    public void run() {
        System.out.println("start daily searcher manager");
        //核心搜索队列coreSearches
        if (this.coreSearches != null && this.coreSearches.size() > 0) {
            for (DailyCoreSearcher cs : coreSearches) {//每一个核心搜索子线程
                threadList.add(cs);//加入搜索队列
                System.out.println(cs.getName() + "core准备开始执行");
                MDC.put("eventID", "日常搜索");
                LogWriter.logger.info(cs.getName() + cs.getBaseURI() + "core准备开始执行");//写入日志
                if (!cs.isRunning()) {
                    cs.startWork();//启动核心子线程搜索
                } else {
                    System.out.println(cs.getName() + "core正在执行");
                }
            }
			
			/*TargetLink tlink = new TargetLink();
			tlink.setLinkURL(tlink.getDefLinkURL());
			generalSearcher.getMyLinkCache().addLink(tlink);*/
            threadList.add(generalSearcher);//将普通搜索子线程加入搜索队列
//			System.out.println(generalSearcher.getName() + ",general"
//					+ generalSearcher.isRunning());
//			System.out.println(generalSearcher.getName() + ",general"
//					+ generalSearcher.isRunning());
            System.out.println(generalSearcher.getName() + "general准备开始执行");
            LogWriter.logger.info(generalSearcher.getName() + "general准备开始执行");//写入日志
            if (!generalSearcher.isRunning()) {
                generalSearcher.startWork();//启动普通子线程搜索
            } else {
                System.out.println(generalSearcher.getName() + "general正在执行");
            }
        }
    }
    public static void main(String[] args){
        DailyManager dMgr = new DailyManager();
        dMgr.init();
        dMgr.startWork();
    }
}
