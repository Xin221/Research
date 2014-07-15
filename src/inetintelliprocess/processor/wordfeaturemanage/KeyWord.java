package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.util.LogWriter;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KeyWord {

    protected String mainWord = null ;
    //不能使用的时候初始化**********************
    private ArrayList<sameWord> sameWords = new ArrayList<sameWord>();
    private float frequency = 0.0f ;
    private String wID = null;

    public class sameWord {

        private KeyWord parent = null ;
        private String word = null ;
        private float factor = 1.0f;

        public KeyWord getParent() {
            return parent;
        }

        public String getWord() {
            return word;
        }

        public sameWord(KeyWord p) {
            this.parent = p ;
        }

        //得到node
        public boolean loadFromXml(Node xml) {
            if(xml == null )
                return false ;
            this.word = xml.getTextContent();
            //读取相似度
            return true ;
        }
    }



    public String getMainWord() {
        return mainWord;
    }

    public void setMainWord(String mainWord) {
        this.mainWord = mainWord;
    }

    public ArrayList<sameWord> getSameWords() {
        return sameWords;
    }

    public void setSameWords(ArrayList<sameWord> sameWords) {
        this.sameWords = sameWords;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public KeyWord() {

    }

    public KeyWord(Element xmlNode) {
        if(xmlNode == null )
            return ;
        loadFromXml(xmlNode) ;
    }

    //时序统计用
    public boolean loadFromDB(ResultSet rs) {
        if(rs == null )
            return false ;
        try {
            this.wID = rs.getString("id");
            this.mainWord = rs.getString("word");
            this.frequency = rs.getFloat("count");
            rs.getDate("createTime");
            rs.getDate("lastUpdateTime");

            return true ;
        } catch (Exception exp) {
            LogWriter.logger.warn("分析器:空间特征向量加载异常");
            return false ;
        }
    }

    //得到xml中node中的元素
    public boolean loadFromXml(Element xmlNode) {
        if(! xmlNode.getNodeName().equals("WORD"))
            return false ;

        mainWord = null ;
        sameWords.clear();
        frequency = 0 ;

        this.frequency = 0;
        NodeList nlist = xmlNode.getChildNodes();
        if(nlist == null)
            return true ;
        for(int i = 0 ; i < nlist.getLength() ; i ++) {
            Node cn = nlist.item(i) ;
            if(cn == null ) continue ;
            if(cn.getNodeName().equals("MAINWORD")) {
                this.mainWord = cn.getTextContent() ;
                //System.out.println( cn.getTextContent()  ) ;
            }
            else if(cn.getNodeName().equals("SAMEWORD")) {
                sameWord sw = new sameWord(this) ;
                if(sw.loadFromXml(cn))
                    sameWords.add(sw) ;
            }
        }
        return true ;
    }

    public int calculateWordNum(String pinfo) {

        int counter = countWordNum(pinfo, this.mainWord) ;

        for(int i = 0 ; i < this.sameWords.size(); i ++) {
            sameWord sw =this.sameWords.get(i) ;
            int sameCount = countWordNum(pinfo, sw.word) ;
            counter += sameCount ;
        }
        return counter ;
    }

    public void writeToDB(String tblName, String isThemeWord) {
        //首先在tblname判断是否当前词已经存在？
        //如果有话就是update 判断词
        //如果没有insert
        //表（ID， word， count， createTime， lastUpdateTime,）
        if(wID == null || wID.equals(""))
            wID = String.valueOf(new Random().nextInt());//取个随机数
        WordManageDBO loader = new WordManageDBO();
        loader.updatespaceInfo(tblName,wID, mainWord,frequency,isThemeWord);
    }

    public float calculatewordSimFacotr(String s) {
        if(s.equals(this.mainWord))
            return 1 ;
        for(int i = 0 ; i <sameWords.size() ; i ++ ) {
            if(sameWords.get(i).equals(s))
                return sameWords.get(i).factor ;
        }
        return 0 ;
    }

    public int countWordNum(String page, String word) {
        String txt = page ;
        int wlength = word.length() ;
        if(wlength == 0 )
            return 0;

        int tlength = txt.length() ;

        txt = txt.replaceAll(word, "") ;
        int tlength2 = txt.length();

        int vl = tlength - tlength2 ;
        float num = (float)vl / wlength ;
        if(num < 0 ) {
            MDC.put("eventID", wID);
            LogWriter.logger.warn("分析器:词频统计信息异常");
            //       		System.out.print("hoo") ;
        }
        return (int) num ;
    }

    public void addWeight(int count) {
        frequency ++ ;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("(([0-9]*)|(\\d+.\\d+)|(-[0-9]*)|(-\\d+.\\d+))");
        return pattern.matcher(str).matches();
    }

    /**
     *   中文字符检验
     *   @param   s   String  待校验字串
     *   @return   包含中文字符返回true,否则返回false
     */
    public static boolean chineseValid(String s){
        int length = s.length();
        byte [] b;
        for(int i=0;i<length;i++){
            b=s.substring(i).getBytes();
            if((b[0]&0xff)>128)
                return true;
        }
        return false;
    }

    public static void main(String [] args){
        System.out.println("isNum: " + KeyWord.isNumeric("-1.2"));

    }

    public static ArrayList<KeyWord> load(String fname) {
        ArrayList<KeyWord> result = new ArrayList<KeyWord>() ;
        Document doc = inetintelliprocess.util.XmlProcessor.getXmlDoc(fname);
        NodeList nlist = doc.getElementsByTagName("WORD");//doc.getChildNodes() ;
        for(int i = 0 ; i < nlist.getLength() ; i ++) {
            Element cn =(Element) (nlist.item(i));
            if(cn == null)
                continue ;
            KeyWord kw = new KeyWord(cn) ;
            if(! kw.getMainWord().equals(""))
                result.add(kw) ;
        }
        return result ;
    }
}
