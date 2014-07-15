package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.processor.wordfeaturemanage.WordVector.wordCount;
import inetintelliprocess.searchengine.frame.SearcherManager;
import inetintelliprocess.util.LogWriter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.MDC;
/**
 * 线程入口
 * 执行信息分析主体，通过该类中的方法进行特征提取、事件演化空间分析以及统计服务
 *
 */
public class WordFeatureThread extends Thread{
    private SearcherManager parentMgr = null;
    private KeyWordMgr kwMgr =null ;
    private List<KeyWordMgr> spaces = null;
    private List<WebPageInfo> pageCache = null ;
    private String workTimeStartString = ""  ;
    private EventInfo myEvent = null ;
    private static Object lock = new Object();

    protected boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void startWork() {
        synchronized (lock) {
            if (!running) {
                running = true;
                start();
            }
        }
    }

    public void stopWork() {
        synchronized (lock) {
            running = false;
        }
    }
    public WordFeatureThread(SearcherManager p) {
        super(p);
        this.parentMgr = p;
    }

    public void run() {
        workTimeStartString = new String();
        runAnalysis(myEvent) ;
    }

    public boolean init(EventInfo iEvent) {
        myEvent = iEvent ;
        return init() ;
    }

    //解析词典信息xml，获取主题词列表
    public boolean init() {
        kwMgr = null ;
        kwMgr = new KeyWordMgr() ;
        spaces=new ArrayList<KeyWordMgr>();
        if(kwMgr.load()){
            spaces.add(kwMgr) ;//初始空间
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.info("空间特征向量初始化成功");
            return true ;
        }
        kwMgr = null ;
        MDC.put("eventID", myEvent.getEventID());
        LogWriter.logger.info("空间特征向量初始化失败");
        return false ;
    }

    //启动事件分析任务，将启动分析任务情况写入日志信息
    public void runAnalysis(EventInfo iEvent) {
        myEvent = iEvent ;
        if(iEvent == null){
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.info("分析器:当前事件为无效事件，结束分析任务");
            return ;
        }
        // String tname = myEvent.getWebPageTbleName(myEvent.eventID);
        // 获取当前事件对应的数据表；
        WordManageDBO loader = new WordManageDBO();
        //为当前事件建立分析表
        if(myEvent.getEventID() != "") {
            if(!loader.createWordInfoTable(myEvent.getAnaWordCountTbleName(myEvent.getEventID()))) {
                MDC.put("eventID", myEvent.getEventID());
                LogWriter.logger.info("分析器:建立事件" + myEvent.getEventID() + "分析表错误，退出当前任务");
                return ;
            }
            if (!loader.createPageCountTable(myEvent.getAnaPageCountTbleName(myEvent.getEventID()))) {
                MDC.put("eventID", myEvent.getEventID());
                LogWriter.logger.info("分析器:建立事件" + myEvent.getEventID() + "篇频信息表错误，退出当前任务");
                return;
            }
            if(!loader.createSpaceInfoTable(myEvent.getAnaSpaceInfoTbleName(myEvent.getEventID()))) {
                MDC.put("eventID", myEvent.getEventID());
                LogWriter.logger.info("分析器:建立事件" + myEvent.getEventID() + "空间特征向量信息表错误，退出当前任务");
                return;
            }
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.info("分析器:为事件" + myEvent.getEventID() + "创建分析表");

        }
        try {
            doAnalysisWork() ;
        } catch(Exception exp) {
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.warn("分析器:分析器" + myEvent.getEventID() + "发生异常:重新开始工作");
            exp.printStackTrace() ;
            //doAnalysisWork();
        }
        running = false;
        MDC.put("eventID", myEvent.getEventID());
        LogWriter.logger.info("分析器:结束当前事件" + myEvent.getEventID() + "分析活动");
    }

    public void doAnalysisWork() {
        String tname = myEvent.getWebPageTbleName(myEvent.getEventID());
        int pagePackNum = 10 ;
        WordManageDBO loader = new WordManageDBO();
        while(running) {
            //设置起始时间
            //测试用
            pageCache = loader.loadtPageInfos(tname, workTimeStartString);
            if(pageCache == null  || pageCache.size() == 0) {
                //System.out.println("pageCache is null!");
//					MDC.put("eventID", myEvent.getEventID());
//					LogWriter.logger.info("分析器:获取网页文本对象信息条目为空,等待开展下一次分析活动");
//					try {
//						Thread.sleep(3*1000);
//					} catch(Exception exp) {
//						MDC.put("eventID", myEvent.getEventID());
//						LogWriter.logger.warn("分析器:分析器线程" + myEvent.getEventID() + "发生异常:重新开始工作");
//					}
                continue ;
            }

            if(pageCache.size() <= 0 || pageCache.isEmpty())
                continue ;
            //System.out.println("1111!~~`~~~~~~");
            WebPageInfo lastinfo = pageCache.get(pageCache.size()-1) ;
            //System.out.println("2222!~~`~~~~~~");
            workTimeStartString = lastinfo.getLastWriteTime().toString() ;
            //System.out.println("3333!~~`~~~~~~");
            //代码抽取函数, 简化代码
            analySpaceInfo(myEvent);
            //System.out.println("4444!~~`~~~~~~");
            //取出若干文章合并后进行分析
            int upper = pagePackNum ;
            int startIdx = 0 ;
            while(true) {
                try {
                    System.out.println("开展当前分析集分析任务");
                    MDC.put("eventID", myEvent.getEventID());
                    LogWriter.logger.info("分析器:开展事件" + myEvent.getEventID() + "当前分析集分析任务");
                    if(upper >= pageCache.size()){
                        upper = pageCache.size() ;
                        //System.out.println("upper is "+upper);
                    }
                    if(startIdx >= pageCache.size()) {
                        System.out.println("完成当前分析集分析任务");
                        MDC.put("eventID", myEvent.getEventID());
                        LogWriter.logger.info("分析器:完成事件" + myEvent.getEventID() + "当前分析集分析任务");
                        break ;
                    }
                    // System.out.println("5555!~~`~~~~~~");
                    //简化代码，抽取函数
                    String pageContentMap = pagesContentMap(startIdx, upper);
                    // System.out.println("6666!~~`~~~~~~");
                    //计算pageContentMap对nonRegular增长贡献
                    pageContentMap = countMapContributationToNonRegular(pageContentMap, myEvent);
                    // System.out.println("7777!~~`~~~~~~");
                    //从pageContentMap中提取3个新词，并存入数据库
                    ArrayList<NonRegularWord> newWords =  extractNewNoneRegularWordFromStringMap(pageContentMap, myEvent);
                    //System.out.println("8888!~~`~~~~~~");
                    for(int i = startIdx ; i < upper; i ++) {
                        if(i >=  pageCache.size())
                            break;
                        //System.out.println("9999!~~`~~~~~~");
                        WebPageInfo pinfo = pageCache.get(i) ;
                        //抽取代码，简化函数
                        computeWordInfo(myEvent, pinfo, newWords);
                        // System.out.println("aaaa!~~`~~~~~~");
                    }
                    newWords.clear();
                    newWords = null ;
                    //System.out.println("dddd!~~`~~~~~~");
                    //计算空间增长变化情况
                    analySpaceChange(myEvent);
                }
                catch(Exception exp1){
//		       			MDC.put("eventID", myEvent.getEventID());
//		       			LogWriter.logger.warn("分析器:分析器线程" + myEvent.getEventID() + "发生异常:重新开始工作");
                }
                startIdx = upper ;
                upper += pagePackNum ;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pageCache.clear();
            pageCache = null ;
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.info("分析器:完成分析任务" + myEvent.getEventID() + "，准备10秒钟后进行下一次分析工作");
        }
    }

    //对高频生词向量的词频进行统计
    //计算当前网页文本信息对高频生词向量的篇频贡献度
    public String countMapContributationToNonRegular(String map, EventInfo myEvent) {
        //对nonregular词进行统计，计算当前文本对nonregular词篇频贡献度
        //在处理过程中将已有的nonReg词过滤掉
        if(map == null) {
            // System.out.println("return null");
            return map ;
        }
        // System.out.println("spaces.size() is "+spaces.size());
        for(int i = 0 ; i < spaces.size() ; i ++) {
            KeyWordMgr space =spaces.get(i);
            //System.out.println("第"+i+"个space size is "+space.getKwords().size());
            for(int j = 0 ; j <space.getKwords().size(); j ++) {
                map = map.replace(space.getKwords().get(j).getMainWord(), "") ;
                //System.out.println("i="+i+" j="+j+": map is "+map+"   ");
                KeyWord kw = space.getKwords().get(j) ;
                //System.out.println("rrrrr!~~`~~~~~~");
                //System.out.println("kw size is "+kw.getSameWords().size());
                for(int k = 0 ; k < kw.getSameWords().size() ; k++ ){
                    // System.out.println("k is "+k+" rrrrr!~~`~~~~~~");
                    map =  map.replace(kw.getSameWords().get(k).getWord(), "") ;
                }
            }
        }

        //读入所有的nonregular词
        List<NonRegularWord> nones = WordManageDBO.loadPageCountWords(myEvent) ;
        if(nones == null )
            return map;

        WordVector nonRegularWordVec = new WordVector() ;
        //nonRegularWord词集中文章的向量值
        for(int i = 0 ; i < nones.size() ; i ++) {
            NonRegularWord nonRegular = nones.get(i) ;
            if(nonRegular == null )
                continue ;
            int num =  nonRegular.countWordNum(map,nonRegular.getMainWord() ) ;
            if(num > 0) {
                //System.out.println("num>0");
                float newWF = nonRegular.getPageCount()+num ;
                nonRegular.updatePageCount(myEvent.getAnaPageCountTbleName(myEvent.getEventID()), newWF) ;
                wordCount wc = nonRegularWordVec.creatNewCount();
                wc.setCount(num);
                wc.setPageId("_static_" + nonRegular.getMainWord());
                wc.setWord(nonRegular.getWord());
                nonRegularWordVec.getVectors().add(wc);
            }
            map = map.replaceAll(nonRegular.getMainWord(), "") ;
            //System.out.println("last map is "+map);
        }
        return map ;
        //更新当前词的词频统计
    }

    //提取当前本文的生词并计算当前事件空间对生词的增长贡献度
    public ArrayList<NonRegularWord> extractNewNoneRegularWordFromStringMap(String map, EventInfo myEvent) {
        //提取新的nonReg词
        for(int i = 0 ; i < spaces.size() ; i ++) {
            KeyWordMgr space =spaces.get(i);
            for(int j = 0 ; j <space.getKwords().size(); j ++) {
                map = map.replaceAll(space.getKwords().get(j).getMainWord(), "") ;
                KeyWord kw = space.getKwords().get(j) ;
                for(int k = 0 ; k <kw.getSameWords().size() ; k ++ )
                    map =  map.replaceAll(kw.getSameWords().get(k).getWord(), "") ;
            }
        }

        NonWordStats nws = new NonWordStats();
        List<NonWordStats> list = new ArrayList<NonWordStats>();
        ArrayList<String> newwords = nws.getKeyword(map);
        list = nws.sort(newwords, 3);

        WordVector newNonRegularWordVec = new WordVector() ;
        ArrayList<NonRegularWord> result = new ArrayList<NonRegularWord>() ;

        for (NonWordStats nonword : list) {
            wordCount wc = newNonRegularWordVec.creatNewCount();
            wc.setCount(nonword.getCount());
            wc.setPageId("_static_" + wc.getWord());// pinfo.pageId ;
            wc.setWord(nonword.getKey());
            newNonRegularWordVec.getVectors().add(wc);
            //新词写入数据库
            NonRegularWord newNone = new NonRegularWord() ;
            newNone.setMainWord(wc.getWord());
            newNone.setWord(wc.getWord());
            String amap = map ;
            int wlength = newNone.getMainWord() .length() ;
            amap = amap.replace(newNone.getMainWord() , "") ;
            int stringcount = map.length() - amap.length() ;

            newNone.setPageCount((int)((float)stringcount/ wlength));

            System.out.println("提取新词:" +  newNone.getMainWord() + "::" + String.valueOf(newNone.getPageCount()));
			   /*if(myEvent!=null)
			     newNone.rID = myEvent.eventID ;*/
            newNone.writeToDB(myEvent) ;
            result.add(newNone) ;
            // System.out.println(w);
        }
        return result;
    }

    //抽取代码，简化
    public void analySpaceInfo(EventInfo myEvent) {
        //添加提取死亡人数--------
        String deathInfo = null;
        String injureInfo = null;
        String buildingInfo = null;
        String url = null;
        String webName = null;
        DeathInfo death = new DeathInfo();
        eDeathInfo edeath = new eDeathInfo();
        long deathExtTime = 0;
        for(int i = 0 ; i <pageCache.size(); i ++) {
            WebPageInfo pinfo = pageCache.get(i) ;
            //添加提取死亡人数--------
            String deathInfobyPage = null;
            long deathTime = 0;
            String injureInfobyPage = null;
            long injureTime = 0;
            String buildingInfobyPage = null;
            long buildingTime = 0;
            System.out.println("getPageTime() is "+pinfo.getPageTime());
            if (Timestamp.valueOf(pinfo.getPageTime()).getTime() > deathExtTime) {
                String pageCon = pinfo.getPageContent() + pinfo.getPageTitle();
//					MDC.put("eventID", myEvent.getEvenID());
//					LogWriter.logger.info("pageCon is : "+pageCon);
                deathInfobyPage = death.extrDeathInfo(pageCon);
                if(deathInfobyPage!=null)
                    deathInfobyPage=deathInfobyPage.concat(edeath.extrDeathInfo(pageCon));
                else
                    deathInfobyPage = edeath.extrDeathInfo(pageCon);
                injureInfobyPage = death.extrInjureInfo(pageCon);
                if(injureInfobyPage!=null)
                    injureInfobyPage=injureInfobyPage.concat(edeath.extrInjureInfo(pageCon));
                else
                    injureInfobyPage = edeath.extrInjureInfo(pageCon);
                buildingInfobyPage = death.extrBuildingInfo(pageCon);
                if(buildingInfobyPage!=null)
                    buildingInfobyPage=buildingInfobyPage.concat(edeath.extrBuildingInfo(pageCon));
                else
                    buildingInfobyPage = edeath.extrBuildingInfo(pageCon);

            }
            if (deathInfobyPage != null && !deathInfobyPage.isEmpty() && !deathInfobyPage.equalsIgnoreCase("null") && deathInfobyPage.length() < 45 && Timestamp.valueOf(pinfo.getPageTime()).getTime() > deathTime){
                deathInfo = deathInfobyPage;
                deathTime = Timestamp.valueOf(pinfo.getPageTime()).getTime();
                url = pinfo.getUrl();
                webName = pinfo.getWebName();
            }
            if (injureInfobyPage != null && !injureInfobyPage.isEmpty() && !injureInfobyPage.equalsIgnoreCase("null") && injureInfobyPage.length() < 45 && Timestamp.valueOf(pinfo.getPageTime()).getTime() > injureTime){
                injureInfo = injureInfobyPage;
                injureTime = Timestamp.valueOf(pinfo.getPageTime()).getTime();
                url = pinfo.getUrl();
                webName = pinfo.getWebName();
            }
            if(buildingInfobyPage != null && !buildingInfobyPage.isEmpty() && !buildingInfobyPage.equalsIgnoreCase("null") && buildingInfobyPage.length() < 45 && Timestamp.valueOf(pinfo.getPageTime()).getTime() > buildingTime){
                buildingInfo = buildingInfobyPage;
                buildingTime = Timestamp.valueOf(pinfo.getPageTime()).getTime();
                url = pinfo.getUrl();
                webName = pinfo.getWebName();
            }
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.info("分析器：事件"+myEvent.getEventID()+"的伤亡情况/建筑物损毁情况为："+deathInfo+" "+injureInfo+" "+buildingInfo);
            deathExtTime = Timestamp.valueOf(pinfo.getPageTime()).getTime();

            //对现有的空间计算，当前文章增长度贡献与文章的向量
            for(int j = 0 ; j < spaces.size() ; j ++) {
                KeyWordMgr space =spaces.get(j);
                WordVector vec1 = space.anaPageContributionToSpace(pinfo);
                pinfo.addWordVec(vec1, space.getID()) ;
                space.writeToDB(myEvent, "true");
                MDC.put("eventID", myEvent.getEventID());
                LogWriter.logger.info("分析器:更新事件空间" + myEvent.getEventID() + "特征向量信息");
            }
        }
        //添加提取死亡人数--------
        if (deathInfo != null || injureInfo != null || buildingInfo != null) {
            //death.writeToDB(deathInfo, injureInfo, url, webName, myEvent.getEvenID());
            death.writeToDB(deathInfo, injureInfo, buildingInfo,url, webName, myEvent.getEventID());
            MDC.put("eventID", myEvent.getEventID());
            LogWriter.logger.info("分析器:更新事件" + myEvent.getEventID() + "的灾情信息");
        }

    }

    //返回title+abstract或者title+content
    public String pagesContentMap(int startIdx, int upper) {
        String pageContentMap = null ;
        for(int i = startIdx ; i < upper; i++) {
            if(i >=  pageCache.size())
                break;
            WebPageInfo pinfo = pageCache.get(i) ;
            pageContentMap += pinfo.getPageTitle() ;
            String abs = pinfo.getAbstract() ;
            if(abs == null || abs.equals("")) {
                abs = pinfo.getPageContent() ;

                if(abs!=null && abs.length()> 300)
                    abs = abs.substring(0, 299) ;
            }
            if(abs!=null)
                pageContentMap += abs;
        }
        return pageContentMap;
    }

    //计算当前事件空间的增长贡献度
    public void computeWordInfo(EventInfo myEvent, WebPageInfo pinfo, ArrayList<NonRegularWord> newWords) {
        for(int j = 0 ; j < newWords.size() ; j ++) {
            if(j >=  newWords.size())
                break;
            NonRegularWord nw = newWords.get(j) ;
            float count = nw.countFrequencey(pinfo.getPageContent()) ;
            if(count< 1)
                continue;
            WordVector.wordCount wc = new  WordVector().creatNewCount() ;
            wc.setCount(count);
            wc.setWord(nw.getMainWord());
            wc.setPageId(pinfo.getPageId());
            wc.setPagetitle(pinfo.getPageTitle());
            wc.setPageurl(pinfo.getUrl());
            wc.setID(pinfo.getUrl()) ;
            wc.setInserttime(new Timestamp(new Date().getTime()));
            String tblName = myEvent.getAnaWordCountTbleName(myEvent.getEventID());
            //if(!KeyWord.isNumeric(wc.getVecWord()) && KeyWord.chineseValid(wc.getVecWord())) {
            if(!KeyWord.isNumeric(wc.getWord())) {
                MDC.put("eventID", myEvent.getEventID());
                if(wc.writeToDB(tblName, myEvent.getEventID()))
                    LogWriter.logger.info("分析器:更新事件" + myEvent.getEventID() + "词频统计信息");
                else
                    LogWriter.logger.info("分析器:更新事件" + myEvent.getEventID() + "词频统计信息不成功");
            }
        }
    }

    public void analySpaceChange(EventInfo myEvent) {
        //分析nonregular词集合导致的空间变化
        List<NonRegularWord> nones = WordManageDBO.loadPageCountWords(myEvent) ;
        if(nones == null )
            return ;
        float size = 0 ;
//		   NonRegularWord maxnonRegular = null ;
        for(int i = 0 ; i < nones.size() ; i ++) {
            NonRegularWord nonRegular = nones.get(i) ;
            if(size < nonRegular.getFrequency()){
                size = nonRegular.getFrequency() ;
//				   maxnonRegular = nonRegular ;
            }
            ArrayList<inetintelliprocess.bean.KeyWord> keyWords = new ArrayList<inetintelliprocess.bean.KeyWord>();
            for(int j = 0 ; j < spaces.size() ; j ++) {
                KeyWordMgr space = spaces.get(j);
                //判断词是否有效···························································
                if(space.isValidSpaceWordSpace(nonRegular)) {
                    if(nonRegular.getFrequency() <= 1) {
                        //  System.out.println("hi") ;
                        space.isValidSpaceWordSpace(nonRegular);
                    }

                    space.addNewWordVec(nonRegular) ;
                    //增加搜索关键词--------------------------------------------------------------------------

                    keyWords.add(inetintelliprocess.bean.KeyWord.converToKeyWord(nonRegular));

                    space.writeToDB(myEvent, "false") ;
                }
            }
            //更新搜索关键词集
            updateKwords(myEvent,this.parentMgr,keyWords);
        }
    }

    //更新关键词集函数
    public void updateKwords(EventInfo myEvent,SearcherManager parentMgr,ArrayList<inetintelliprocess.bean.KeyWord> keyWords){
        //读取spaceinfo加入主题词典中的词,全部加入keyWords
        List<inetintelliprocess.bean.KeyWord> dicWords = new ArrayList<inetintelliprocess.bean.KeyWord>();
        dicWords = WordManageDBO.loadSpaceInfoWords(myEvent) ;
        keyWords.addAll(dicWords);
        this.parentMgr.updateKwords(keyWords);
    }

    public static void main(String[] args){
        String str = "2014-02-14 12:20:23.0";
        String[] stt = str.split(".");
        for(String s : stt){
            System.out.println(s);
        }
        System.out.println(java.sql.Timestamp.valueOf(str).getTime());

    }

}
