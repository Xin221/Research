package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.bean.EventInfo;
import java.util.Date;
import java.util.Random;

//生词组织管理的业务类，负责判断有效事件空间向量、计算生词词向量相似度和更新主题词、生词对事件空间贡献度等相关的业务操作
public class NonRegularWord extends KeyWord {

    private String id = null ;
    private String word = null ;
    private float pageCount = 0.0f ;
    private boolean wordType= false ;
//	private float validFactor = 0.3f;

    public String getId() {
        return id;
    }

    public void setId(String rID) {
        this.id = rID;
    }

    public boolean isWordType() {
        return wordType;
    }

    public void setWordType(boolean wordType) {
        this.wordType = wordType;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public float getPageCount() {
        return pageCount;
    }

    public void setPageCount(float pageCount) {
        this.pageCount = pageCount;
    }

    public boolean writeToDB(EventInfo evtInfo) {
        String  tblname = evtInfo.getAnaPageCountTbleName(evtInfo.getEventID()) ;
        if(id == null || id.equals(""))
            id = String.valueOf(new Random().nextInt());//取个随机数
        WordManageDBO loader = new WordManageDBO() ;
        NonRegularWord noRegWord = loader.loadWord(tblname, word);
        //if(!NonRegularWord.isNumeric(word) && NonRegularWord.chineseValid(word)) {
        if(!NonRegularWord.isNumeric(word)) {
            if(noRegWord == null )
                loader.insertpageCount(tblname, id, word, pageCount,wordType);
            else{
                loader.updatepageCount(tblname, word, pageCount );
            }
        }
        return false ;
    }

    public void updateWordType(boolean isOriginal) {
        this.wordType = isOriginal ;
        new Date();
    }

    //时序统计用
    public KeyWord convertToKeyWord() {
        KeyWord kw = new KeyWord() ;
        kw.setMainWord(this.word);
        kw.setFrequency(this.pageCount);
        kw.setFrequency(this.pageCount);
        return kw;
    }

    public float countFrequencey(String txt) {
        int length = word.length() ;
        if(length == 0)
            return 0.0f;

        int tlen = txt.length();
        String txt2 = txt.replaceAll(word, "") ;
        int tlen2 = tlen - txt2.length() ;
        float time = (float)tlen2 / length ;
        return time;
    }

    public void updatePageCount(String tblname, float count) {
        //System.out.println("更新为:" + count) ;
        this.pageCount = count ;
        WordManageDBO loader = new WordManageDBO() ;
        loader.updatepageCount(tblname, word, this.pageCount);
        //update数据库这个对象的pagecount数值 为当前的this.pageCount ；
    }
}	
		