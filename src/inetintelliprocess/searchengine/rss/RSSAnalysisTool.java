package inetintelliprocess.searchengine.rss;

import inetintelliprocess.bean.EventInfo;
import inetintelliprocess.bean.KeyWord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RSSAnalysisTool {

    /**
     *   中文字符检验   
     *   @param   s   String  待校验字串 
     *   @return   包含中文字符返回true,否则返回false
     */
    public boolean chineseValid(String s){
        int length = s.length();
        byte [] b;
        for(int i=0;i<length;i++){
            b=s.substring(i).getBytes();
            if((b[0]&0xff)>128)
                return true;
        }
        return false;
    }

    /**
     *   时间格式更改
     *   @param   d   Date
     *   @return  返回指定格式为yyyy-MM-dd kk:mm:ss的时间字符串
     */
    public String DateFormat(Date d){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  df.format(d);
    }

    /**
     *   判断条目中是否有关键词      区分事件是否存在的情况
     */
    public boolean isKeyWordContains(String rssContent, List<KeyWord> keyWords, List<KeyWord> eventKeywords) {
        int flag = 0;
        int keyWordSize = keyWords.size();
        for(int j = 0; j < keyWordSize; j++){
            if(rssContent.contains(keyWords.get(j).getWord())){
                flag++;
            }
        }
        if(eventKeywords == null || eventKeywords.isEmpty()) {
            if((flag >= keyWordSize / 2))
                return true;
        } else {
            boolean containsEventKeys = false ;
//	 	    for (int j = 0; j < this.getParentMgr().getKwords().size(); j++) {
            int count = 0;
            for (int j = 0; j < eventKeywords.size(); j++) {
                String name = eventKeywords.get(j).getWord();
                if (rssContent.contains(name)) {
                    count++;
                }
            }
            int hitcount = eventKeywords.size() / 2;
            if (count - 1 >= hitcount)
                containsEventKeys = true;

            if((flag >= keyWordSize / 2) && containsEventKeys)
                return true;
        }
        return false;
    }

    public List<KeyWord> getEventKeyWords(EventInfo eventInfo) {
        //RSSDBO rssDBO = new RSSDBO();
        List<KeyWord> eventKeyWords = eventInfo.getAddrKeyWords();
        //rssDBO.getKeyWords(eventInfo);
        return eventKeyWords;
    }

    /**
     *订阅提取正文
     * String s=entry.getDescription().getValue()
     * 起始标志符：中文或1个数字后接中文或2个数字后接中文或4个数字后接中文
     * 结束标志 符：<
     * */
    public String findContent(String pageContent) {
        char [] b = null;
        boolean bool = false;
        b = pageContent.toCharArray();
        try {
            for(int j=0;j<b.length;j++) {
                if(!bool) {
                    if(isChinese(b[j])
                            ||(Character.isDigit(b[j])&&isChinese(b[j+1]))
                            ||(Character.isDigit(b[j])&&Character.isDigit(b[j+1])&&isChinese(b[j+2]))
                            ||(Character.isDigit(b[j])&&Character.isDigit(b[j+1])&&Character.isDigit(b[j+2])&&Character.isDigit(b[j+3])&&isChinese(b[j+4]))
                            ) {
                        int k=j;
                        bool = true;
                        char[] d = new char[10000];
                        int n=0;
                        while(k < b.length && !(b[k] == '<')) {
                            d[n]=b[k];
                            n++;
                            k++;
                        }
                        StringBuffer sb = new StringBuffer();
                        for(int ii=0; ii < n; ii++) {
                            sb. append(d[ii]);
                        }
                        pageContent = sb.toString();
                        return pageContent;
                    }
                }
            }
        } catch(NullPointerException e) {
            //LogWriter.logger.error(e);
            e.printStackTrace();
        } catch(ArrayIndexOutOfBoundsException e) {
            //LogWriter.logger.error(e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断中文字符
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public String cleanTag(String rssPageContent) {
        rssPageContent = rssPageContent.replaceAll("<br>", "");
        rssPageContent = rssPageContent.replaceAll("<p>", "");
        rssPageContent = rssPageContent.replaceAll("</p>", "");
        rssPageContent = rssPageContent.replaceAll("</br>", "");
        rssPageContent = rssPageContent.replaceAll("<strong>", "");
        rssPageContent = rssPageContent.replaceAll("</strong>", "");
        rssPageContent = rssPageContent.replaceAll("&#160;", "");
        rssPageContent = rssPageContent.replaceAll("media_span_url\\('", "");
        rssPageContent = rssPageContent.replaceAll("'\\)", "");
        rssPageContent = rssPageContent.replaceAll(" ", "");
        rssPageContent = rssPageContent.trim();
        return rssPageContent;
    }

    /*public static void main(String[] args) {
        DBManager dm = new DBManager();
        dm.connect();
        dm.getkeywords(111);
 //	   System.out.println(rat.keywordContain("揭秘：谁是解放军中唯一一位独腿开国中将', '2012-04-07 14:44:12', ' 核心提示：  钟赤兵（1914~1975）出生于湖南平江县城郊一个贫苦码头工人家庭，两岁丧父，12岁先后在毛笔店和织布厂当学徒，生活非常苦。解放后历任中央军委民航局局长，军委防空部队政委、总后营房管理部部长、……<ahref=\"http://news.ifeng.com/history/zhongguoxiandaishi/detail_2012_04/07/13716939_0.shtml\">[查看全文]', 'http://phtv.ifeng.com/program/sskj/detail_2012_03/23/13397559_0.shtml', '朝',' 鲜核心提示：  钟赤兵（1914~1975）出生于湖南平江县城郊一个贫苦码头工人家庭，两岁丧父，12岁先后在毛笔店和织布厂当学徒，生活非常苦。解放后历任中央军委民航局局长，军委防空部队政委、总后营房管理部部长、……<ahref=\"http://news.ifeng.com/history/zhongguoxiandaishi/detail_2012_04/07/13716939_0.shtml\">[查看全文]'", dm.rs));
        dm.close();
    }*/
    public static void main(String[] args) {
        ArrayList<KeyWord> keywords = new ArrayList<KeyWord>();
        keywords.add(new KeyWord("揭秘"));
        keywords.add(new KeyWord("ren"));
        ArrayList<KeyWord> eventkeywords = new ArrayList<KeyWord>();
        System.out.println(eventkeywords);
//		eventkeywords.add("解放军");
//		eventkeywords.add("women");
        RSSAnalysisTool dm = new RSSAnalysisTool();
        System.out.println(dm.isKeyWordContains("揭秘：谁是解放军中唯一一位独腿开国中将", keywords, eventkeywords));
        String s = "<adsn>ewe</adfdd>";
        if(s.length() > 25)
            System.out.println("sub:"+s.substring(0,25));
        else
            System.out.println(s);
        RSSAnalysisTool rs = new RSSAnalysisTool();
        System.out.println(rs.chineseValid(s));
    }
}
