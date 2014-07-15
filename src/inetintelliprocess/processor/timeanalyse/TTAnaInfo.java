package inetintelliprocess.processor.timeanalyse;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.processor.wordfeaturemanage.KeyWord;
import inetintelliprocess.processor.wordfeaturemanage.NonRegularWord;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TTAnaInfo {
    private String pageId = null;
    private String word = null;
    private String pageContent = null;
    private String pageTime = null;
    private String pageTitle = null;
    private String url = null;
    private String webName = null;
    private int count = 0;

    public String getPageId(){
        return pageId;
    }

    public void setPageId(String pageId){
        this.pageId = pageId;
    }

    public String getWord(){
        return this.word;
    }

    public Date getTimeDate(){
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date da = null;
        try {
            da = df1.parse(pageTime);
        } catch (ParseException e) {
        }
        return da;
    }

    public void setWord(String word){
        this.word = word;
    }

    public String getPageCount(){
        return this.pageContent;
    }

    public void setPageContent(String pageContent){
        this.pageContent = pageContent;
    }

    public String getPageTime(){
        return this.pageTime;
    }

    public void setPageTime(String pageTime){
        this.pageTime = pageTime;
    }

    public String getPageTitle(){
        return this.pageTitle;
    }

    public void setPageTitle(String pageTitle){
        this.pageTitle = pageTitle;
    }

    public String getUrl(){
        return this.url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getWebName(){
        return this.webName;
    }

    public void setWebName(String webName){
        this.webName = webName;
    }


    public int getWordCount(String content, String w) {
        int len1 = content.length() ;
        int len2 = w.length() ;
        content = content.replace(w, "") ;
        int len3 = len1 - content.length() ;
        int result = len3 / len2 ;
        return result;
    }

    public List<TTAnaInfo> getPagesFromList(List<TTAnaInfo> pages, Date ti, Date tj) {
        List<TTAnaInfo> durPages = new ArrayList<TTAnaInfo>();
        for(int i = 0; i < pages.size(); i++){
            if (pages.get(i).getTimeDate().getTime() >= ti.getTime() && pages.get(i).getTimeDate().getTime() < tj.getTime() )
                durPages.add(pages.get(i));
        }
        return durPages;
    }
	
	/*public ArrayList<WebPageInfo> getPagesByWord(String eventID, String word) {
		TimeAnalyseDBO timeAnalyDBO = new TimeAnalyseDBO();
		ArrayList<WebPageInfo> result = new ArrayList<WebPageInfo>() ;
		result =  timeAnalyDBO.loadPagesByWord(eventID, word) ;
		for(int i = 0 ; i < result.size() ; i ++) {
			WebPageInfo wb = result.get(i) ;
			System.out.println("wb:" + wb.getPageTitle() + "::" + wb.getLastWriteTime().toString()) ;
		}
		return result ;
	}*/

    public List<String> getEventWords(EventInfo myEvent) {
        TimeAnalyseDBO timeAnalyDBO = new TimeAnalyseDBO();
        List<String> result = new ArrayList<String>() ;
        List<KeyWord> kws = timeAnalyDBO.loadEventKeyWords(myEvent, 12) ;
        List<NonRegularWord> nons = timeAnalyDBO.loadTopNonRegWords(myEvent, 3) ;
        if(nons!=null){
            for(int i = 0 ; i < nons.size() ; i++) {
                NonRegularWord newWord = nons.get(i) ;
                for(int j = 0 ; j < kws.size() ; j++) {
                    KeyWord keyWord = kws.get(j) ;
                    if(keyWord.getMainWord()!=null&&keyWord.getMainWord().equals(newWord.getWord())) {
                        keyWord.setFrequency(keyWord.getFrequency()	+ newWord.getPageCount());
                        nons.remove(i) ;
                        i-- ;
                        break;
                    }
                }
            }

            for(int i = 0; i < nons.size(); i ++) {
                NonRegularWord newWord = nons.get(i) ;
                for(int j = 0; j < kws.size(); j ++) {
                    KeyWord keyWord = kws.get(j) ;
                    if(keyWord.getFrequency() < newWord.getPageCount()) {
                        KeyWord newKey = newWord.convertToKeyWord();
                        kws.add(j, newKey) ;
                        break ;
                    }
                }
            }
        }
        if(kws != null){
            for(int j = 0; j < kws.size(); j ++) {
                KeyWord keyWord = kws.get(j) ;
                for(int i = j + 1 ; i < kws.size() ; i ++) {
                    KeyWord keyWord2 = kws.get(i) ;
                    if(keyWord.getFrequency() < keyWord2.getFrequency()) {
                        kws.set(i, keyWord) ;
                        kws.set(j, keyWord2) ;
                        //交换之后要把位置信息更新
                        j = i;
                    }
                }
            }
            for(int j = 0 ; j < kws.size() ; j ++) {
                KeyWord keyWord = kws.get(j) ;
                result.add(keyWord.getMainWord());
                //System.out.println("k:" + keyWord.getMainWord() + "::" + String.valueOf(keyWord.getFrequency())) ;
            }
        }
        return result ;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPageContent() {
        return pageContent;
    }
	
	/*public static void main(String[] args){
		wordinfoload loader = new wordinfoload() ;
		ArrayList<String> words = null;
		words = loader.loadtWords(5);
		for(String ww : words){
			System.out.println("----------------------------------------------------------------------------------------------------------------");
			System.out.println("word:"+ww);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time1 = null;
			Date time2 = null; 
			try {
				time1 = format.parse("2012-02-24 10:17:24");
				time2 = format.parse("2012-02-24 10:17:36");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			ArrayList<TTAnaInfo> pageCache = null ;
			pageCache = loader.loadtWordInfos(time1,time2,ww);
			System.out.println(new AnalysisFrame().doTimeTrace(pageCache, time1, time2, ww).toString());
			System.out.println(new AnalysisFrame().doTimeTrace(pageCache, time1, time2, ww).getTxt());
			
		}
	 }*/

}
