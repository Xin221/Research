package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.util.LogWriter;

import java.util.ArrayList;

import org.apache.log4j.MDC;

//主题词管理的业务类，负责判断事件空间畸变、调用事件信息写入数据表、计算主题词向量相似度、添加新词向量和计算文本信息对事件空间贡献度等相关的业务操作
//对搜索器引擎包提取的文本对象内容进行信息分拣归类和事件空间提取特征
public class KeyWordMgr {

    private ArrayList<KeyWord> kwords = null ;
    private String configString = inetintelliprocess.util.XmlProcessor.getDicFilename() ; //文件名
    private String ID = null ;
    private float validateFactorSetting = 0.3f;
//	  private float uglyTransFormFactor = 0.3f ;

    public ArrayList<KeyWord> getKwords() {
        return kwords;
    }

    public String getID() {
        return ID;
    }

    public boolean load() {
        kwords = KeyWord.load(configString) ;
        if(kwords == null )
            return false ;
        return true ;
    }

    public boolean writeToDB(EventInfo evtInfo, String isThemeWord) {
        String  tblname = evtInfo.getAnaSpaceInfoTbleName(evtInfo.getEventID()) ;
        for(int i = 0 ;  i< kwords.size() ; i ++) {
            KeyWord kw =kwords.get(i) ;
            //if(!KeyWord.isNumeric(kw.getMainWord()) && KeyWord.chineseValid(kw.getMainWord())) {
            if(!KeyWord.isNumeric(kw.getMainWord())) {
                kw.writeToDB(tblname, isThemeWord);
            }
        }
        return true ;
    }

    //生词是否为空间向量成员有效性判断
    public Boolean isValidSpaceWordSpace(NonRegularWord nw ) {
        if(nw == null )
            return false ;
        KeyWord longestVec = getSpaceLongesttvec();
        if(longestVec == null || longestVec.getFrequency() == 0 )
            return true ;

        if(nw.getPageCount()/longestVec.getFrequency() >= validateFactorSetting ) {
            return true ;
            //可能会导致变形
            //在wordinfo中寻找与当前nw最相关文章的词
            //nw出现词频最高的三篇文章
			/*	  List<wordVector>	pageInfoCounts = getRelaventPages(nw);
		  if(pageInfoCounts == null || pageInfoCounts.size()<3)
			  return false ;
		  for(int i = 0 ; i < pageInfoCounts.size() ; i ++) {
		       float simFactor = calculateSimFactor(pageInfoCounts.get(i)) ;
		       if(simFactor > vecAndSpcarSimFactor) 
		    	   return true ;
		  }*/
        }
        KeyWord leastVec = this.getSpaceLeastvec();
        if(nw.getPageCount() >= (longestVec.getFrequency() + leastVec.getFrequency())/2)
            return true ;
        if(longestVec.getFrequency() / leastVec.getFrequency() > 5 && //leastVec.frequency > 20 &&
                nw.getPageCount()/ leastVec.getFrequency() > 5	)
            return true ;
        return false ;
    }


    public KeyWord getSpaceLeastvec() {
        KeyWord res = null ;
        float count = 0 ;
        if(kwords.size()> 0 ) {
            res = kwords.get(0) ;
            count = res.getFrequency() ;
        }

        for(int i = 0 ; i < kwords.size() ; i ++) {
            if(kwords.get(i).getFrequency() < count) {
                count = kwords.get(i).getFrequency();
                res = kwords.get(i) ;
            }
        }
        return res ;
    }

    public KeyWord getSpaceLongesttvec() {
        KeyWord res = null ;
        float count = 0 ;
        if(kwords.size()> 0 ) {
            res = kwords.get(0) ;
            count = res.getFrequency() ;
        }

        for(int i = 0 ; i < kwords.size() ; i ++) {
            if(kwords.get(i).getFrequency() > count) {
                count = kwords.get(i).getFrequency();
                res = kwords.get(i) ;
            }
        }
        return res ;
    }

    public KeyWordMgr addNewWordVec (NonRegularWord word) {
        if(word == null )
            return null;

        for(int i = 0 ; i < kwords.size() ; i ++) {
            if(kwords.get(i).getMainWord().equals(word.getMainWord()))
                return null;

        }
        KeyWord kw = word.convertToKeyWord();
        kw.setFrequency(word.getPageCount() * 1);
        this.kwords.add(kw);
        word.updateWordType(true);
        return null ;
    }


    //对当前事件空间进行分析，提取当前事件空间向量，同时计算当前特征向量增长贡献度
    public WordVector anaPageContributionToSpace(WebPageInfo pinfo) {
        if(pinfo == null )
            return null ;

        WordVector vec = getPageVector(pinfo) ;
        return vec ;
    }

    //获取网页文本信息特征向量
    public WordVector getPageVector(WebPageInfo pinfo) {
        if(pinfo == null )
            return null ;

        WordVector vec = new WordVector() ;

        for(int i = 0 ; i <kwords.size() ; i ++) {
            KeyWord kw =  kwords.get(i) ;
            if(kw == null || kw.getMainWord().equals(""))
                continue ;

            int countNum = kw.calculateWordNum(pinfo.getPageTitle()) ;
            countNum += kw.calculateWordNum(pinfo.getPageContent()) ;
            if(countNum <  0) {
                MDC.put("eventID", this.getID());
                LogWriter.logger.warn("分析器:词频统计信息异常");
            }
            if(countNum <=  0) {
                continue ;
            }

            kw.setFrequency(kw.getFrequency() + 1);// (int)((float)countNum/kwords.size();
            WordVector.wordCount wc = vec.creatNewCount();
            wc.setCount(countNum);
            wc.setPageId(pinfo.getPageId());
            if(countNum >0 )
                kw.addWeight(countNum);
            wc.setWord(kw.getMainWord());
            vec.getVectors().add(wc) ;
        }
        return vec ;
    }



    public static void main(String[] args){
        KeyWordMgr mgr = new KeyWordMgr() ;
        if(mgr.load())
            mgr.toString() ;
        else System.out.println("load error") ;
    }
}
