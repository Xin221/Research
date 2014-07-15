package inetintelliprocess.processor.timeanalyse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeTrace {
    private String word = null ;
    private List<TTAnaInfo> pages = null ;
    private Date t1 = null;
    private Date t2 = null;
    private TimeTrace leftSon = null ;//左子划分
    private TimeTrace rightSon = null ;//右子划分
    private String representPageURL = null ;
    private String representWebName = null ;

    public TimeTrace(String w, Date ti, Date tj, List<TTAnaInfo> ps) {
        word = w ;
        t1 = ti ;
        t2 = tj ;
        pages = ps ;
        representPageURL = findRepresent().get(0) ;
        representWebName = findRepresent().get(1) ;
    }

    public List<String> findRepresent() {
        List<String> representPage = new ArrayList<String>();
        String url = null;// 从pages 中找到word词出现频率最高的文章url;
        String webName = null;
        int max = 0;
        for(TTAnaInfo ttai :pages){
            if(ttai.getCount() > max){
                max = ttai.getCount();
                url = ttai.getUrl();
                webName = ttai.getWebName();
            }
        }
        representPage.add(url);
        representPage.add(webName);
        return representPage ;
    }

    public TimeTrace merge(TimeTrace tt1 , TimeTrace tt2) {
        List<TTAnaInfo> t1pages = tt1.pages;
        List<TTAnaInfo> t2pages = tt2.pages;
        List<TTAnaInfo> pss = new ArrayList<TTAnaInfo>();
        for(TTAnaInfo ttai :t1pages ){
            pss.add(ttai);
        }
        for(TTAnaInfo ttai :t2pages ){
            pss.add(ttai);
        }
        TimeTrace result = new TimeTrace(tt1.word, tt1.t1, tt1.t2, pss);
        result.leftSon = tt1;
        result.rightSon = tt2 ;
        return result;
    }

    public void save(String tblName) {
        String XmlTxt = "<root>" + getTxt() + "</root>" ;
        TimeAnalyseDBO load = new TimeAnalyseDBO();
        boolean insertInfo = load.insertWordTimeStat(tblName,XmlTxt,word);
        if(!insertInfo)
            //信息插入不成功处理
            System.out.println("信息插入不成功");
        //save 存的时候先看当前词是否存在，如果不存在就插一条新的记录
        //如果存在把xmltxt和存在记录的xmltxt合并后更新
    }

    public String getTxt() {
        String txt = null;
        if(leftSon != null)
            txt += leftSon.getTxt() ;
        if(rightSon != null)
            txt += rightSon.getTxt() ;
        if(leftSon == null && rightSon == null ){
            txt =  "<span t1=\"" + t1.toString() + "\"  t2=\""+t2.toString() + "\" count=\"" + String.valueOf(getWordsCount(pages)) + "\" representPageURL=\"" + representPageURL + "\" representWebName=\"" + representWebName + "\">" + "</span>";
//        	 txt =  "<span><t1>" + t1.toString() + "</t1><t2>" +t2.toString() + "</t2><count>" + String.valueOf(getWordsCount(pages)) + "</count><representPageURL>" + representPageURL +"</representPageURL></span>";
        }
        txt = txt.replaceAll("null", "");
        return txt;
    }

    public int getWordsCount(List<TTAnaInfo> tList) {
        int count = 0;
        for (TTAnaInfo ttai: tList){
            count += ttai.getCount();
        }
        return count;
    }

    public String toString() {
        String info = new String();
        if(this.leftSon == null && this.rightSon==null){
            info += "[TimeTrace:word("+this.word +");count("+getWordsCount(pages)+");"
                    +"t1("+this.t1.toString()+");t2("+this.t2.toString()+");representPageURL("
                    +this.representPageURL+");webName("+this.representWebName+")]\n";
            return info;
        }
        if(this.leftSon != null)
            info += this.leftSon.toString();

        if(this.rightSon!=null)
            info += this.rightSon.toString();
        return info;

    }
}
