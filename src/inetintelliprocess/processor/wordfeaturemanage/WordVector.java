package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.util.LogWriter;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

//主题词向量管理的业务类，负责写入词向量到词频信息表和添加生词到词频信息表的相关业务操作
public class WordVector {

    public static class wordCount{

        private String pageId = null ;
        private String word= null ;
        private float count = 0.0f ;
        private Timestamp inserttime;
        private String pagetitle = null;
        private String pageurl = null;
        private String ID = null;

        public void setPageId(String pageId) {
            this.pageId = pageId;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public float getCount() {
            return count;
        }

        public void setCount(float count) {
            this.count = count;
        }

        public void setInserttime(Timestamp inserttime) {
            this.inserttime = inserttime;
        }

        public void setPagetitle(String pageTitle) {
            this.pagetitle = pageTitle;
        }

        public void setPageurl(String pageUrl) {
            this.pageurl = pageUrl;
        }


        public void setID(String iD) {
            ID = iD;
        }

        public boolean writeToDB(String tblName, String eventID){
            //把wc作为一个记录插入tblName表中
            WordManageDBO loader = new WordManageDBO();
            boolean insertflag = false;
            try {
                insertflag = loader.insertwordInfos(tblName, pageId, word, count, ID, eventID, inserttime, pagetitle, pageurl);
            }
            catch(Exception exp) {
                LogWriter.logger.warn("分析器:词频统计信息写入异常");
            }
            return insertflag;
        }

        public boolean loadFromDB(ResultSet rs) {
            if(rs == null )
                return false ;
            try {
                this.pageId = String.valueOf(rs.getInt("pageId"));
                this.count =  rs.getInt("count");
                this.pagetitle = rs.getString("pagetitle") ;
                this.pageurl = rs.getString("pageurl");

                return true ;
            }
            catch (Exception exp) {
                LogWriter.logger.warn("分析器:文本对象信息加载异常");
                return false ;
            }
        }

//		  public static boolean isNumeric(String str) {
//			  Pattern pattern = Pattern.compile("(([0-9]*)|(\\d+.\\d+)|(-[0-9]*)|(-\\d+.\\d+))"); 
//			  return pattern.matcher(str).matches(); 
//		  }
    }

    private WebPageInfo parentInfo = null ;
    private ArrayList<wordCount> vectors = new ArrayList<wordCount>();

    public void setParentInfo(WebPageInfo parentInfo) {
        this.parentInfo = parentInfo;
    }

    public ArrayList<wordCount> getVectors() {
        return vectors;
    }

    public wordCount creatNewCount() {
        wordCount wc =  new wordCount();
        return wc;
    }

    public void writeToDB(EventInfo myEvent) {
        //把 vectors里的wordcount对象写入数据库wordinfo

        String tblName = myEvent.getAnaWordCountTbleName(myEvent.getEventID());
        for(int i = 0 ; i < vectors.size(); i ++) {
            wordCount wc = vectors.get(i) ;
            //把wc作为一个记录插入tblName表中
            wc.inserttime = new Timestamp(java.sql.Date.valueOf(this.parentInfo.getPageTime()).getTime());
            wc.pagetitle = this.parentInfo.getPageTitle();
            wc.pageurl = this.parentInfo.getUrl();
            boolean insertflag = false;
            //if(!KeyWord.isNumeric(wc.vecWord) && KeyWord.chineseValid(wc.vecWord))
            if(!KeyWord.isNumeric(wc.word))
                insertflag = wc.writeToDB(tblName, myEvent.getEventID());
            if(!insertflag)
                System.out.println("插入数据不成功");
        }
    }


}
