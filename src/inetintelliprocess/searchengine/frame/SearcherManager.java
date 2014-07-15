package inetintelliprocess.searchengine.frame;
/**
 * frame搜索器的框架
 */
import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.dbc.DbTools;
import inetintelliprocess.processor.barschart.BarChartInfo;
import inetintelliprocess.processor.barschart.GenerateDatasets;
import inetintelliprocess.processor.newpubopinion.NewsPubDataDBO;
import inetintelliprocess.processor.timeanalyse.AnalyTimeStat;
import inetintelliprocess.processor.wordfeaturemanage.WordFeatureThread;
import inetintelliprocess.searchengine.events.EventHandler;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.searchengine.searchers.WebPageInfoDbo;
import inetintelliprocess.searchengine.searchers.eventsearcher.CoreSearcher;
import inetintelliprocess.searchengine.searchers.eventsearcher.Searcher;
import inetintelliprocess.searchengine.searchers.otherQuery.*;
import inetintelliprocess.util.Config;
import inetintelliprocess.util.GenerateWord;
import inetintelliprocess.util.GenerateXml;
import inetintelliprocess.util.LogWriter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 优先级
 * @state
 * priorityValue = aX + bY 
 * X 区分用户请求还是告警请求，Y 表示等待时长
 */
/**
 * 信息搜索的搜索器
 * 主要负责创建搜索器、创建事件处理对象、调用搜索类
 * 初始化事件处理对象eventHandler
 * 通过该类中的方法调用核心网站搜索和普通玩站搜索、
 * 创建管理子线程、初始化和更新事件优先级、控制事件搜索的开始和结束
 */
public class SearcherManager extends Thread {
    private ArrayList<Searcher> threadList = new ArrayList<Searcher>();
    private GoogleQuery1 googleQuery = new GoogleQuery1();
    private BaiduQuery baiduQuery = new BaiduQuery();
    private SoQuery soQuery = new SoQuery();
    private ArrayList<CoreSearcher> coreSearches = null;
    private Searcher generalSearcher = new Searcher(null);
    private ArrayList<KeyWord> kwords = null;
    private ArrayList<KeyWord> keys = null;
    private ArrayList<KeyWord> addrwords = null;
    private EventHandler evtHandler = null;
    private float priorityValue = 0;
    private static final float parameterA = 2.4f;
    private static final float parameterB = 1.5f;
    private int priorityVariable = 1;
    private long waitTime = 0;
    //private int googleCount = inetintelliprocess.entry.SearcherEntry.GoogleCount;
    private String toB12 = null;
    //private static final String B12URL = "http://192.168.0.100:9090/demows/services/testService";
    private static final String B12URL = Config.read("B12_URL");

    //舆情简报分析
    private inetintelliprocess.processor.newspubopinion.PubOpinionThread m_pub = null;

    // 加RSS订阅内容***************
    private inetintelliprocess.searchengine.rss.RSSFeedThread m_rssfeedthread = null;
    private inetintelliprocess.searchengine.rss.FeedReaderFrame m_rssfeed = null;//????????
    private inetintelliprocess.processor.wordfeaturemanage.WordFeatureThread m_analysis = null;
    /*private String ID = "";*/
    private static Object lock = new Object();
    private boolean running = false;


    public SearcherManager(String ID){
        MDC.put("eventID", ID);
        LogWriter.logger.info(ID + "开始初始化！");
        EventInfo info;
        try {
            if(ID != null){
                info = EventHandler.getEventFrom(ID);//读eventinfo表，得到事件信息
                if(info!=null) {
                    System.out.println("info is not null!");
                    evtHandler = EventHandler.createHandler(info);//创建事件处理对象，在此处处理eventLog表
                    if (evtHandler != null){
                        System.out.println("初始化成功");
                        updatePriority(new Date());
                        MDC.put("eventID",evtHandler.getEvtInfo().getEventID());
                        LogWriter.logger.info(evtHandler.getEvtInfo().getEventID() + "初始化成功");
                    }
                    else {
                        MDC.put("eventID",ID);
                        LogWriter.logger.info(evtHandler.getEvtInfo().getEventID() + "初始化错误");
                    }
                }
            }
        }catch (Exception e) {
            MDC.put("eventID", ID);
            LogWriter.logger.error("SearcherManager创建失败！");
            e.printStackTrace();
        }
    }


    //2014年3月14日以前版本，每来一个地震事件，则进行初始化，其中包括写入eventlog表、创建事件处理对象、创建相关衍生表、初始化搜索子线程，
    //其中，初始化时候直接创建搜索子线程容易创建子线程太多而溢出。
//	public SearcherManager(String ID){
//		MDC.put("eventID", ID);
//		LogWriter.logger.info(ID + "开始初始化！");
//		EventInfo info;
//		try {
//			if(ID != null){
//				info = EventHandler.getEventFrom(ID);//读eventinfo表，得到事件信息
//				if(info!=null) {
////					System.out.println("info is not null!!!");
//					evtHandler = EventHandler.createHandler(info);//创建事件处理对象，在此处处理eventLog表
//					if (evtHandler != null){
//							if (init(evtHandler)) {
//								// 初始化日志记录
//								System.out.println("初始化成功");
//								updatePriority(new Date());
//							} else {
//								MDC.put("eventID",evtHandler.getEvtInfo().getEvenID());
//								LogWriter.logger.info(evtHandler.getEvtInfo().getEvenID()
//										+ "初始化错误");
////								System.out.println("error");
//							}
//					}
//				}
//				}
//			}catch (Exception e) {
//				MDC.put("eventID", this.getID());
//				LogWriter.logger.error("SearcherManager创建失败！");
//				e.printStackTrace();
//		}
//	}
//	

    public int getPriorityVariable() {
        return priorityVariable;
    }

    public void setPriorityVariable(int priorityVariable) {
        this.priorityVariable = priorityVariable;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }
    public float getPriorityValue() {
        return priorityValue;
    }

    public void setPriorityValue(float priorityValue) {
        this.priorityValue = priorityValue;
    }

    public float updatePriority(Date date,String priority){
        float priVariable = 0;
        int priValue = Integer.parseInt(priority);
        setPriorityVariable(priValue);
        setWaitTime((date.getTime()-evtHandler.getArrivalTime().getTime())/1000);
        priVariable = parameterA * getPriorityVariable() + parameterB * getWaitTime();
        setPriorityValue(priVariable);
        return priorityValue;
    }
    public float updatePriority(String priority){
        float priVariable = 0;
        int priValue = Integer.parseInt(priority);
        setPriorityVariable(priValue);
        Date date=evtHandler.getArrivalTime();
        setWaitTime((new Date().getTime()-date.getTime())/1000);
        priVariable = parameterA * getPriorityVariable() + parameterB * getWaitTime();
        setPriorityValue(priVariable);
        return priorityValue;
    }

    public float updatePriority(Date date){
        float priVariable = 0;
        setWaitTime((date.getTime()-evtHandler.getArrivalTime().getTime())/1000);
        priVariable = parameterA * getPriorityVariable() + parameterB * getWaitTime();
        setPriorityValue(priVariable);
        return priorityValue;
    }

    public String getID(){
        String evtID = new String();
        evtID = evtHandler.getEvtInfo().getEventID();
        return evtID;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 开始执行
     */
    public void startWork() {
        synchronized (lock) {
            if (!running) {
                start();
                System.out.println(this.getID()+"开始执行！");
                MDC.put("eventID", this.getID());
                LogWriter.logger.info(this.getID()+"开始执行！");
                running = true;
            } else {
                System.out.println("mgr正在执行");
            }
        }
        setStartState();

    }

    public void stopWork() {
        while (true) {
            boolean flag = true;
            for (CoreSearcher cs : coreSearches) {
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
            if(m_rssfeedthread.isRunning()){
                m_rssfeedthread.stopWork();
                flag = false;
            }
            if(m_analysis.isRunning()){
                m_analysis.stopWork();
                flag = false;
            }
            if(m_pub.isRunning()){
                m_pub.stopWork();
                flag = false;
            }
            if (flag) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        setSuspendState();
        running = false;
    }

    public ArrayList<Searcher> getThreadList() {
        return threadList;
    }

    public void setThreadList(ArrayList<Searcher> threadList) {
        this.threadList = threadList;
    }

    public ArrayList<CoreSearcher> getCoreSearches() {
        return coreSearches;
    }

    public void setCoreSearches(ArrayList<CoreSearcher> coreSearches) {
        this.coreSearches = coreSearches;
    }

    public Searcher getGeneralSearcher() {
        return generalSearcher;
    }

    public void setGeneralSearcher(Searcher generalSearcher) {
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

	/*public String getID() {
		return ID;
	}

	public void setID(String iD) {
		this.ID = iD;
	}*/

    public EventHandler getEvtHandler() {
        return evtHandler;
    }

    public void setEvtHandler(EventHandler evtHandler) {
        this.evtHandler = evtHandler;
    }

	/*public Boolean addGeneralLink(TargetLink lk) {
		if (lk == null)
			return false;
		if (generalSearcher == null) {
			generalSearcher = new Searcher(null);
			generalSearcher.init();
		}

		return generalSearcher.getMyLinkCache().addLink(lk);
	}*/

    public boolean init(EventHandler evtH) {
        if (evtH == null)
            return false;

        if (!init())
            return false;
        this.evtHandler = evtH;
        this.kwords = new ArrayList<KeyWord>();
        //解析事件名，加入关键字列表
        this.addrwords = new ArrayList<KeyWord>();
        if(!this.evtHandler.equals(null)&&!this.evtHandler.equals("")){
            KeyWord KWord = new KeyWord();
            ArrayList<KeyWord> word1 = KWord.multiRegionInfo(this.evtHandler.getEvtInfo().getEventLocation());
            addrwords.addAll(word1);
            for(KeyWord s:word1){
                if(s.getWord()!=null&&!s.getWord().isEmpty())
                    this.googleQuery.addQueryAddress(s.getWord());
                //this.getEvtHandler().getEvtInfo().addAddrKeyWord(s);
                System.out.println(s.getWord());
            }
//			int i=0;
//			for(KeyWord s:kwords){
//				System.out.println(++i+"  "+s.getWord());
//			}
//			double x = this.evtHandler.getEvtInfo().getLocxd();//5月31日新加。。。
//			double y = this.evtHandler.getEvtInfo().getLocyd();//5月31日新加。。。
//			System.out.println("x is "+x+"\ny is "+y);//5月31日新加。。。
//			LogWriter.logger.info("读出经纬度为：经度（"+x+"）, 纬度（"+y+"）");
//			ArrayList<KeyWord> word2 = KWord.multiRegion_Gis(x, y);//5月31日新加。。。
//			addrwords.addAll(word2);//5月31日新加。。。
//			for(KeyWord wd : word2){//5月31日新加。。。
//				if(wd.getWord()!=null&&!wd.getWord().isEmpty()&&!isExistWord(wd)){//5月31日新加。。。
//					addrwords.add(wd);//5月31日新加。。。
//					this.googleQuery.addQueryAddress(wd.getWord());
//				}
//			}//5月31日新加。。。
            /////////////6月3日新加！
            //根据事件名和地点取出英文关键词
            word1 = KWord.multiRegionLocInfo(this.evtHandler.getEvtInfo().getEventLocation());
            for(KeyWord k: word1)
                if(!isExistWord(k)){
                    //System.out.println("加入地点关键词"+evtLocation);
                    addrwords.add(k);
                    this.googleQuery.addQueryAddress(k.getWord());
                    //this.getEvtHandler().getEvtInfo().addAddrKeyWord(k);
                }
        }
        //DbTools.insertKeyWord(kwords,this.getID());
        int i=0;
        for(KeyWord s:addrwords){
            System.out.println(++i+"  "+s.getWord());

        }



        //初始化rss线程，仅解析订阅的内容，不做深层搜索
        // 加rss订阅信息*******
        this.m_rssfeed = new inetintelliprocess.searchengine.rss.FeedReaderFrame(evtHandler.getEvtInfo());
        this.m_rssfeedthread = new inetintelliprocess.searchengine.rss.RSSFeedThread(m_rssfeed);
        this.m_rssfeedthread.setRssEvent(evtHandler.getEvtInfo());

        this.m_analysis.init(evtHandler.getEvtInfo());

        this.m_pub = new inetintelliprocess.processor.newspubopinion.PubOpinionThread();
        m_pub.setPubEvent(evtHandler.getEvtInfo());

        return true;
    }

    //加载搜索处理模板，test.xml
    public boolean init() {

        String ConfigFname = inetintelliprocess.util.XmlProcessor
                .getKeyWordFilename(); //DictionaryManage.
        this.coreSearches = new ArrayList<CoreSearcher>();

        // 加rss订阅*************************
        this.m_analysis = new WordFeatureThread(this);
        this.keys = new ArrayList<KeyWord>();
        try {
            File file = new File(ConfigFname);
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList nlist = doc.getElementsByTagName("coreWeb");
            for (int i = 0; i < nlist.getLength(); i++) {
                Element cn = (Element) (nlist.item(i));
                NodeList cnlist = cn.getChildNodes();
                for (int j = 0; j < cnlist.getLength(); j++) {
                    Node urlNode = cnlist.item(j);

                    if (urlNode.getNodeName().equals("URL")) {
                        CoreSearcher cs = new CoreSearcher(this);
                        if (cs.init((Element) (urlNode))) {
                            this.coreSearches.add(cs);
                        }
                    }
                }
            }

            nlist = doc.getElementsByTagName("key");
            for (int i = 0; i < nlist.getLength(); i++) {
                Element cn = (Element) (nlist.item(i));
                String s = cn.getTextContent();
                KeyWord kw = new KeyWord();
                kw.setWord(s);
                System.out.println(s);
                this.keys.add(kw);
                googleQuery.addQueryXml(kw.getWord());
                //this.getEvtHandler().getEvtInfo().addKeyWord(kw);
            }
            this.generalSearcher = new Searcher(this);
            this.generalSearcher.init();
        } catch (Exception exp) {
            LogWriter.logger.fatal("XML文件" + ConfigFname + "解析失败");
        }
        return true;
    }

    public void printMe() {
        for (CoreSearcher cs : coreSearches) {
            String s = cs.getMyTemplate().getBaseURI() + "("
                    + cs.getMyTemplate().getName() + ")::<"
                    + cs.getMyTemplate().getTagName() + " "
                    + cs.getMyTemplate().getAttName() + "="
                    + cs.getMyTemplate().getValue() + ">";
            System.out.println(s);
        }
    }
    /**
     * 设置状态
     * @state
     * 未开始0，正在1，中止2，已完成3.
     */
    public void setStartState() {
        evtHandler.setStartState();
        LogWriter.logger.info("事件"+this.getID()+"开始执行了！");
    }

    public void setSuspendState() {
		/*for (Searcher s : threadList) {
			s.setRunning(false);
		}*/
        threadList.clear();
        evtHandler.setSuspendState();
        LogWriter.logger.info("事件"+this.getID()+"中止执行！");
    }

    public void setEndState() {
		/*for (Searcher s : threadList) {
			s.setRunning(false);
		}*/
        threadList.clear();
        evtHandler.setEndState();

    }
    public boolean isStop() {

        for (CoreSearcher cs : coreSearches) {
            if (cs.isRunning() == true)
                return false;

        }
        if (generalSearcher.isRunning() == true)
            return false;
        return true;
    }

    // 话题追踪
    public void updateKwords(ArrayList<KeyWord> words) {

        for (int i = 0; i < words.size(); i++) {
            KeyWord kw = new KeyWord();
            // 判断parentManager.words是否存在重复的关键词
            if (!isExistWord(words.get(i))&&words.get(i).getWord()!=null) {
                kw.setWord(words.get(i).getWord());
                kw.setCount(words.get(i).getCount());
                System.out.println("加入新词：：" + words.get(i).getWord());
                this.kwords.add(kw);
            } else {
                continue;
            }
        }
        // 写入数据库
        this.getEvtHandler()
                .writeKwordsToEvtinfo(
                        this.getEvtHandler().getEvtInfo().getEventID(),
                        this.getKwords());

    }

    // 用于话题追踪
    public boolean isExistWord(KeyWord word) {
        boolean flag = false;
        for (int i = 0; i < this.kwords.size(); i++) {
            KeyWord kword = this.kwords.get(i);
            if (kword.getWord().equals(word.getWord())) {
                // 判断本次的count值是否 > 上次的count

                if (kword.getCount() > word.getCount()
                        || !kword.isValidNewWord(this.evtHandler.getEvtInfo())) {
                    // 剔除当前词，说明已不再是最新话题
                    this.kwords.remove(i);

                } else {
                    // 替换值
                    kword.setCount(word.getCount());
                }
                flag = true;
                return flag;
            } else
                continue;
        }
        return flag;
    }

    /**
     * 调用核心网站、普通网站以及RSS搜索子线程，创建URL搜索队列
     * 判断事件的各个子线程是否执行完，若都执行完，则置状态为3（正常执行完）
     */
    public void run() {
        System.out.println("开始@@@@@@@@@@@@@@@"+this.getID()+"++++++++++++");
        System.out.println("start searcher manager"+this.getID());

        if (!init(evtHandler))
            init(evtHandler);
        this.searchDaily();
        //google搜索
        this.googleQuery.search(this.getID());
        this.baiduQuery.search(this.getID(),this.addrwords);
        this.soQuery.search(this.getID(),this.addrwords);

        if (this.coreSearches != null && this.coreSearches.size() > 0) {
            for (CoreSearcher cs : coreSearches) {
                threadList.add(cs);
                System.out.println(cs.getName() + "core准备开始执行");
                MDC.put("eventID", this.getID());
                LogWriter.logger.info(cs.getName() + "core准备开始执行");
                if (!cs.isRunning()) {
                    cs.startWork();
                } else {
                    MDC.put("eventID", this.getID());
                    LogWriter.logger.info(cs.getName() + "core正在执行");
                    System.out.println(cs.getName() + "core正在执行");
                }
            }
        }

        threadList.add(generalSearcher);
        System.out.println(generalSearcher.getName() + "general准备开始执行");
        MDC.put("eventID", this.getID());
        LogWriter.logger.info(generalSearcher.getName() + "general准备开始执行");
        if (!generalSearcher.isRunning()) {
            generalSearcher.startWork();
        } else {
            MDC.put("eventID", this.getID());
            LogWriter.logger.info(generalSearcher.getName() + "general正在执行");
            System.out.println(generalSearcher.getName() + "general正在执行");
        }

        // 加rss订阅****************************
        this.m_rssfeedthread.startWork();
        this.m_analysis.startWork();
        this.m_pub.startWork();

        while (true) {
            boolean flag = false;
            for (CoreSearcher cs : coreSearches) {
                if (cs.isRunning())
                    break;
                flag = true;
            }
            if (generalSearcher.isRunning())
                flag = false;
            if(m_rssfeedthread.isRunning())
                flag = false;

            if (flag) {
                if(m_analysis.isRunning()){
                    m_analysis.stopWork();
                }
                if(m_pub.isRunning())
                    m_pub.stopWork();
                running = false;

                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        /////////////////////////////start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }//搜完一个大循环之后休息半个小时

        setEndState();//如果子线程都执行完，则eventLog里面该事件状态改为3
        MDC.put("eventID", this.getID());
        LogWriter.logger.info("事件"+this.getID()+"执行完毕！");
        System.out.println(evtHandler.getEvtInfo().getEventID()+"事件任务执行完毕!!!");

        System.out.println("开始时序统计。");
        AnalyTimeStat analyTimeStat = new AnalyTimeStat();
        analyTimeStat.runTimeStatAnalysis(evtHandler.getEvtInfo());
        System.out.println("时序统计结束。");

        System.out.println("开始生成信息统计报告。");
        NewsPubDataDBO.setDataset(NewsPubDataDBO.init(evtHandler.getEvtInfo()));
        NewsPubDataDBO.makePicture(this.getID());

        BarChartInfo barInfo = new BarChartInfo();
        List<BarChartInfo> barCharInfos = new ArrayList<BarChartInfo>();
        try {
            barCharInfos = barInfo.init(evtHandler.getEvtInfo());
            GenerateDatasets.removeDuplicateWithOrder(barCharInfos);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        GenerateDatasets generateDatadets = new GenerateDatasets();
        int CurrPage = GenerateDatasets.getCurPage();
        int xTimeNum = GenerateDatasets.getxTimes().size();
        int pageRowNum = GenerateDatasets.getPageCharNum();
        int CountPage;
        if(xTimeNum % pageRowNum == 0)
            CountPage = xTimeNum / pageRowNum;
        else
            CountPage = xTimeNum / pageRowNum + 1;
        if(CountPage != 0){
            CurrPage = 1;
        }
        GenerateDatasets.setCurPage(CurrPage - 1);
        List<BarChartInfo>  barChartInfoList = generateDatadets.generateDataList(barCharInfos);
        GenerateDatasets.setDataset(GenerateDatasets.createDataset(barChartInfoList));
        GenerateDatasets.makePicture(this.getID());
        GenerateWord.createANewFile(GenerateWord.loadWordBean(this.getID(),barCharInfos));
        System.out.println("生成信息统计报告word成功。");

        ///添加发送互联网灾情舆情至B12包
        GenerateXml gxml = new GenerateXml();
        List<WebPageInfo> lists = gxml.getAllInfo(this.getID());
        toB12 = gxml.toPubB12(lists,this.getID());
        if(toB12!=null){
            MDC.put("eventID", this.getID());
            LogWriter.logger.info("发送互联网灾情、舆情至B12包:"+B12URL);
            System.out.println("发送互联网灾情、舆情至B12包");
            //System.out.println("toB12 is :\n"+toB12);
            String method = "B11putDisinfoB12Start_Service";		//方法是他们给的
            StringBuffer sb = new StringBuffer();
            sb.append(toB12);		//这里写发送的内容
            Object[] opAddEntryArgs = new Object[] { sb.toString() };
            String result = executeService(opAddEntryArgs, B12URL, method, true);
            System.out.println("客户端接收到:  " + result);
            MDC.put("eventID", this.getID());
            LogWriter.logger.info("客户端接收到:"+result);
            if(Integer.parseInt(result)==1){
                for(WebPageInfo info:lists)
                    WebPageInfoDbo.setSent(this.getID(), info.getPageId());
            }
            MDC.put("eventID", this.getID());
            LogWriter.logger.info("发送互联网灾情、舆情至B12包结束！");
            System.out.println("发送互联网灾情、舆情至B12包结束！");
        }
    }



    public String executeService(Object[] opAddEntryArgs, String url,
                                 String method, boolean b) {
        String xml = "";
        try {
            RPCServiceClient serviceClient = null;
            serviceClient = new RPCServiceClient();
            Options options = serviceClient.getOptions();
            EndpointReference targetEPR = new EndpointReference(url);

            options.setTo(targetEPR);
            QName opAddEntry = new QName("http://B11DisasterdatatoB12Service.sinosoft.com", method);
            @SuppressWarnings("rawtypes")
            Class[] classes = new Class[] { String.class };
            if (b) {
                xml = (String) serviceClient.invokeBlocking(opAddEntry,
                        opAddEntryArgs, classes)[0];
            } else {
                serviceClient.invokeRobust(opAddEntry, opAddEntryArgs);

            }
        } catch (AxisFault e) {
            e.printStackTrace();
        }
        return xml;
    }

    public static void main(String[] args){
		/*Server s = new Server();
		s.start();*/
        SearcherManager mgr1 = new SearcherManager("N34400E07360020051008104847");
        mgr1.startWork();

    }
    public ArrayList<KeyWord> getAddrwords() {
        return addrwords;
    }
    public void setAddrwords(ArrayList<KeyWord> addrwords) {
        this.addrwords = addrwords;
    }
    public ArrayList<KeyWord> getKeys() {
        return keys;
    }
    public void setKeys(ArrayList<KeyWord> keys) {
        this.keys = keys;
    }
    public void searchDaily(){
        List<WebPageInfo> infoListAll = new ArrayList<WebPageInfo>();
        int addrKey = this.getAddrwords().size();
        String sql = new String();
        if(this.getAddrwords()!=null && !this.getAddrwords().isEmpty()){
            try {
                sql = sql+"select * from generalwebinfotbl where pageContent like '%"+new String(this.getAddrwords().get(0).getWord().getBytes("UTF-8"),"UTF-8")+"%'";

                if(this.getAddrwords().size()>1)
                    for(int i=1; i<addrKey; i++){
                        sql = sql + " and pageContent like '%" +new String(this.getAddrwords().get(i).getWord().getBytes("UTF-8"),"UTF-8") + "%'";
                    }
                sql = sql.concat(";");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("searchDaily sql is "+sql);
        infoListAll = DbTools.querySqlWeb(this.getID(), sql);
        System.out.println(infoListAll.size());
        try {
            List<WebPageInfo> infoList = new ArrayList<WebPageInfo>();
            for(int i=0;i<infoListAll.size()-20;i=i+20){
                if(i+20>infoListAll.size())
                    infoList = infoListAll.subList(i,infoListAll.size());
                else
                    infoList = infoListAll.subList(i,i+20);
                if(WebPageInfo.write(this.getID(), infoList)){
                    MDC.put("eventID",this.getID());
                    LogWriter.logger.error("从日常搜索库中写入到事件"+this.getID()+"第"+i+"-"+(i+4)+"个成功");
                    System.out.println("从日常搜索库中写入到事件"+this.getID()+"成功");
                }
            }
//			if(info.write(this.getID(), infoList)){
//				MDC.put("eventID",this.getID());
//				LogWriter.logger.error("从日常搜索库中写入到事件"+this.getID()+"成功");
//				System.out.println("从日常搜索库中写入到事件"+this.getID()+"成功");
//			}
        } catch (Exception e) {
            MDC.put("eventID",this.getID());
            LogWriter.logger.error("从日常搜索库中写入到事件"+this.getID()+"失败");
            e.printStackTrace();
        }
    }
}
