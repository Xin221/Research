package inetintelliprocess.bean;
/**
 * bean基本类（处理、搜索两部分用的共同的属性信息）
 */
import inetintelliprocess.processor.wordfeaturemanage.NonRegularWord;
import inetintelliprocess.processor.wordfeaturemanage.WordManageDBO;
import invocation.GisServiceInv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * 搜索层的业务类
 * 主要负责记录搜索关键字信息
 * 关键字基本属性and基本操作
 *
 * 搜索条件中搜索过滤条件的关键词元素
 *
 */
public class KeyWord {
    private String word = "";
    private float count = 0.0f;

    public KeyWord(){
        this.word = "";
        this.count = 0.0f;
    }
    public KeyWord(String word){
        this.word = word;
        this.count = 0.0f;
    }
    public KeyWord(String word,float count){
        this.word = word;
        this.count = count;
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
    /**
     * 将生词转化为搜索过滤条件的词元素
     * @param nonRegular 生词
     */
    public static KeyWord converToKeyWord(NonRegularWord nonRegular){
        KeyWord Kw = new KeyWord();
        Kw.setWord(nonRegular.getMainWord());
        Kw.setCount(nonRegular.getPageCount());
        return Kw;
    }
    /**
     * 判断警告事件中的生词是否为有效话题词，是则加入搜索过滤条件中
     * @param eventInfo 警告事件
     */
    public boolean isValidNewWord(EventInfo eventInfo){
        boolean flag = false;
        //求count/countNum > 0.1f
        WordManageDBO loader = new WordManageDBO();
        float countNum = loader.getSpaceInfoCount(eventInfo);
        if(this.getCount()/countNum > 0.1f)
            flag = true;
        return flag;
    }
    /**
     * 提取省市地区情况信息
     * @param evtLoc 省市地区信息
     */
    public ArrayList<KeyWord> extrRegionInfo(String evtLoc) {
        ArrayList<KeyWord> keys = new ArrayList<KeyWord>();
        String region = null;
        String city = null;
        String city1 = null;
        String county = null;
        String street = null;
        if(evtLoc.length() <= 4) {
            KeyWord key = new KeyWord();
            key.setWord(evtLoc);
            System.out.println(key.word);
            keys.add(key);
        } else {
            Pattern patPrefix = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((岛)|(附近)|(维吾尔自治区)|(壮族自治区)|(回族自治区)|(自治区)|(省)|(自治州))");
            Matcher matPrefix = patPrefix.matcher(evtLoc);
            while(matPrefix.find()){
                region = matPrefix.group();
            }
            if (region != null){
                region = region.replace("岛", "");
                region = region.replace("附近", "");
                region = region.replace("维吾尔自治区", "");
                region = region.replace("壮族自治区", "");
                region = region.replace("回族自治区", "");
                region = region.replace("自治区", "");
                region = region.replace("自治州", "");
                region = region.replace("省", "");
                System.out.println(String.valueOf(region));
                KeyWord key1 = new KeyWord();
                key1.setWord(region);
                keys.add(key1);
                evtLoc = evtLoc.replace(region, "");
                evtLoc = evtLoc.replace("岛", "");
                evtLoc = evtLoc.replace("附近", "");
                evtLoc = evtLoc.replace("维吾尔自治区", "");
                evtLoc = evtLoc.replace("壮族自治区", "");
                evtLoc = evtLoc.replace("回族自治区", "");
                evtLoc = evtLoc.replace("自治区", "");
                evtLoc = evtLoc.replace("自治州", "");
                evtLoc = evtLoc.replace("省", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patMid = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((地区))");
            Matcher matMid = patMid.matcher(evtLoc);
            while(matMid.find()){
                city = matMid.group();
            }
            if (city != null){
                city = city.replace("地区", "");
                System.out.println(String.valueOf(city));
                KeyWord key2 = new KeyWord();
                key2.setWord(city);
                keys.add(key2);
                evtLoc = evtLoc.replace(city, "");
                evtLoc = evtLoc.replace("地区", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patMid1 = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((市))");
            Matcher matMid1 = patMid1.matcher(evtLoc);
            while(matMid1.find()){
                city1 = matMid1.group();
            }
            if (city1 != null){
                city1 = city1.replace("市", "");
                System.out.println(String.valueOf(city));
                KeyWord key4 = new KeyWord();
                key4.setWord(city1);
                keys.add(key4);
                evtLoc = evtLoc.replace(city1, "");
                evtLoc = evtLoc.replace("市", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patSuffix = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((县))");
            Matcher matSuffix = patSuffix.matcher(evtLoc);
            while(matSuffix.find()){
                county = matSuffix.group();
            }
            if (county != null){
                county = county.replace("县", "");
                System.out.println(String.valueOf(county));
                KeyWord key3 = new KeyWord();
                key3.setWord(county);
                keys.add(key3);
                evtLoc = evtLoc.replace(county, "");
                evtLoc = evtLoc.replace("县", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patSuffix1 = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((区))");
            Matcher matSuffix1 = patSuffix1.matcher(evtLoc);
            while(matSuffix1.find()){
                street = matSuffix1.group();
            }
            if (street != null){
                street = street.replace("区", "");
                System.out.println(String.valueOf(street));
                KeyWord key5 = new KeyWord();
                key5.setWord(street);
                keys.add(key5);
                evtLoc = evtLoc.replace(street, "");
                evtLoc = evtLoc.replace("区", "");
                System.out.println(String.valueOf(evtLoc));
            }
            if(region==null&&city==null&&county==null&&street==null){
                KeyWord key4 = new KeyWord();
                key4.setWord(evtLoc);
                keys.add(key4);
                evtLoc = null;
            }
        }
        return keys;
    }

    //5月30日新添加的函数
    public ArrayList<KeyWord> multiRegion_Gis(double locxd,double locyd){
        ArrayList<KeyWord> multikeys = new ArrayList<KeyWord>();
        //LogWriter.logger.info("去找国家吧！");
        List<Map<String, String>> maplist = GisServiceInv.cityQuery(locxd,locyd);
        //LogWriter.logger.info("得出国家和首都啦！");
        KeyWord key1 = new KeyWord(),key2 = new KeyWord();
        for(int i=0;i<maplist.size();i++){
            key1.setWord(maplist.get(i).get("CAPITAL"));
            System.out.println(i+" "+key1.getWord());
            if(key1.getWord()!=null)
                multikeys.add(key1);
            key2.setWord(maplist.get(i).get("COUNTRY"));
            //System.out.println(i+"国家 "+key2.getWord());
            if(key2.getWord()!=null)
                multikeys.add(key2);
        }
        int kk=0;
        for(KeyWord k : multikeys){
            System.out.println(kk+++":"+k.getWord());
        }
        return multikeys;
    }

    /**
     * 提取省市地区情况信息,包含交界信息情况
     * @param evtLoc 省市地区信息
     */
    //修改中......
    public ArrayList<KeyWord> multiRegionInfo(String evtLoc) {
        ArrayList<KeyWord> multikeys = new ArrayList<KeyWord>();
        String[] evtLocs = evtLoc.split(",");
        for(int i = 0; i < evtLocs.length; i++){
            if(evtLocs[i].isEmpty()||evtLocs[i].matches("\\s*"))
                continue;
            System.out.println("multiRegionInfo "+i+" : "+evtLocs[i]);
            ArrayList<KeyWord> keys = new ArrayList<KeyWord>();
            KeyWord key = new KeyWord();
            keys = key.extrRegionInfo(evtLocs[i]);
            //System.out.println(keys);
            multikeys.addAll(keys);

        }
        return multikeys;
    }

    public ArrayList<KeyWord> multiRegionLocInfo(String evtLocation){
        ArrayList<KeyWord> kwords = new ArrayList<KeyWord>();
        KeyWord word = new KeyWord();
        String[] strLoc = evtLocation.split(",");
        if (strLoc.length > 1 && strLoc[1] != null && !inetintelliprocess.processor.wordfeaturemanage.KeyWord.chineseValid(strLoc[1])){
            int start = strLoc[1].indexOf(" ");
            if(start!=-1){
                word.setWord(strLoc[1].substring(start+1));
                if(word.getWord()!=null&&!word.getWord().isEmpty()){
                    kwords.add(new KeyWord(strLoc[1].substring(start+1)));
                    System.out.println("first 1 add "+word.getWord());
                }
            }
            else{
                word.setWord(strLoc[1]);
                if(word.getWord()!=null&&!word.getWord().isEmpty()){
                    kwords.add(new KeyWord(strLoc[1]));
                    System.out.println("first 2 add "+word.getWord());
                }
            }
            String[] str = strLoc[0].split(" of ");
            if(str.length > 1 && str[1] != null){
                word.setWord(str[1]);
                //System.out.println("加入名称关键词1"+word4.getWord());
                if(word.getWord()!=null&&!word.getWord().isEmpty()){
                    kwords.add(new KeyWord(str[1]));
                    System.out.println("second add "+word.getWord());
                }
            }
        }
        else{
            String[] str = strLoc[0].split(" of ");
            if(str.length > 1 && str[1] != null){
                word.setWord(str[1]);
                //System.out.println("加入名称关键词1"+word4.getWord());
                if(word.getWord()!=null&&!word.getWord().isEmpty()){
                    kwords.add(new KeyWord(str[1]));
                    System.out.println("second add "+word.getWord());
                }
            }
            else{
                int start = strLoc[0].indexOf(" ");
                if(start!=-1){
                    word.setWord(strLoc[0].substring(start+1));
                    if(word.getWord()!=null&&!word.getWord().isEmpty()){
                        kwords.add(new KeyWord(strLoc[0].substring(start+1)));
                        System.out.println("third add "+word.getWord());
                    }
                }
                else if(!inetintelliprocess.processor.wordfeaturemanage.KeyWord.chineseValid(strLoc[0])){
                    word.setWord(strLoc[0]);
                    if(word.getWord()!=null&&!word.getWord().isEmpty()){
                        kwords.add(new KeyWord(strLoc[0]));
                        System.out.println("forth add "+word.getWord());
                    }
                }	else{
                    System.out.println("Now multiRegionInfo("+strLoc[0]+")");
                    this.multiRegionInfo(strLoc[0]);
                }
            }

        }
        int j = 0;
        for(KeyWord s:kwords){
            System.out.println(++j+"  "+s.getWord());

        }
        return kwords;
    }

    //原来的函数
//	public ArrayList<KeyWord> multiRegionInfo(String evtLoc) {
//		ArrayList<KeyWord> multikeys = new ArrayList<KeyWord>();
//		String[] evtLocs = evtLoc.split(","); 
//		for(int i = 0; i < evtLocs.length; i++){
//			   System.out.println(evtLocs[i]);
//			   ArrayList<KeyWord> keys = new ArrayList<KeyWord>();
//			   KeyWord key = new KeyWord();
//			   keys = key.extrRegionInfo(evtLocs[i]);
//			   //System.out.println(keys);
//			   multikeys.addAll(keys);
//		}
//		return multikeys;
//	}

    /**
     * 提取省市地区情况信息
     * @param evtLoc 省市地区信息
     */
    public ArrayList<String> extrRegionInfoString(String evtLoc) {
        ArrayList<String> keys = new ArrayList<String>();
        String region = null;
        String city = null;
        String city1 = null;
        String county = null;
        String street = null;
        if(evtLoc.length() <= 4) {
            keys.add(evtLoc);
        } else {
            Pattern patPrefix = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((岛)|(附近)|(维吾尔自治区)|(壮族自治区)|(回族自治区)|(自治区)|(省)|(自治州))");
            Matcher matPrefix = patPrefix.matcher(evtLoc);
            while(matPrefix.find()){
                region = matPrefix.group();
            }
            if (region != null){
                region = region.replace("岛", "");
                region = region.replace("附近", "");
                region = region.replace("维吾尔自治区", "");
                region = region.replace("壮族自治区", "");
                region = region.replace("回族自治区", "");
                region = region.replace("自治区", "");
                region = region.replace("自治州", "");
                region = region.replace("省", "");
                System.out.println(String.valueOf(region));
                keys.add(region);
                evtLoc = evtLoc.replace(region, "");
                evtLoc = evtLoc.replace("岛", "");
                evtLoc = evtLoc.replace("附近", "");
                evtLoc = evtLoc.replace("维吾尔自治区", "");
                evtLoc = evtLoc.replace("壮族自治区", "");
                evtLoc = evtLoc.replace("回族自治区", "");
                evtLoc = evtLoc.replace("自治区", "");
                evtLoc = evtLoc.replace("自治州", "");
                evtLoc = evtLoc.replace("省", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patMid = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((地区))");
            Matcher matMid = patMid.matcher(evtLoc);
            while(matMid.find()){
                city = matMid.group();
            }
            if (city != null){
                city = city.replace("地区", "");
                System.out.println(String.valueOf(city));
                keys.add(city);
                evtLoc = evtLoc.replace(city, "");
                evtLoc = evtLoc.replace("地区", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patMid1 = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((市))");
            Matcher matMid1 = patMid1.matcher(evtLoc);
            while(matMid1.find()){
                city1 = matMid1.group();
            }
            if (city1 != null){
                city1 = city1.replace("市", "");
                System.out.println(String.valueOf(city));
                keys.add(city1);
                evtLoc = evtLoc.replace(city1, "");
                evtLoc = evtLoc.replace("市", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patSuffix = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((县))");
            Matcher matSuffix = patSuffix.matcher(evtLoc);
            while(matSuffix.find()){
                county = matSuffix.group();
            }
            if (county != null){
                county = county.replace("县", "");
                System.out.println(String.valueOf(county));
                keys.add(county);
                evtLoc = evtLoc.replace(county, "");
                evtLoc = evtLoc.replace("县", "");
                System.out.println(String.valueOf(evtLoc));
            }
            Pattern patSuffix1 = Pattern.compile("([\\u4e00-\\u9fa5])*(:)?((区))");
            Matcher matSuffix1 = patSuffix1.matcher(evtLoc);
            while(matSuffix1.find()){
                street = matSuffix1.group();
            }
            if (street != null){
                street = street.replace("区", "");
                System.out.println(String.valueOf(street));
                keys.add(street);
                evtLoc = evtLoc.replace(street, "");
                evtLoc = evtLoc.replace("区", "");
                System.out.println(String.valueOf(evtLoc));
            }
        }
        return keys;
    }

    /**
     * 提取省市地区情况信息,包含交界信息情况
     * @param evtLoc 省市地区信息
     */
    public ArrayList<String> multiRegionInfoString(String evtLoc) {
        ArrayList<String> multikeys = new ArrayList<String>();
        String[] evtLocs = evtLoc.split(",");
        for(int i = 0; i < evtLocs.length; i++){
            System.out.println(evtLocs[i]);
            multikeys = extrRegionInfoString(evtLocs[i]);
        }
        return multikeys;
    }



    /**
     * 测试将描述性文字分解成词形式的省市地区
     */
    public static void main(String args[]) {
        //String evtLoc = "新疆维吾尔自治区塔城地区乌苏市,伊犁哈萨克自治州尼勒克县交界";
        //String evtLoc = "c000gss9 67km SSW of Kiska Volcano, Alaska";
        //String evtLoc = "c000gsqe South of Africa";
        String evtLoc = "c000gruw 101km N of Visokoi Island, ";
        //String evtLoc = "c000gqfk Mid-Indian Ridge";
        //String evtLoc = "伊朗";
        ArrayList<KeyWord> kwords = new ArrayList<KeyWord>();
        //keys = region.multiRegionInfo(evtLoc);
//		keys = region.multiRegion_Gis(124, 45);
        KeyWord KWord = new KeyWord();
        ArrayList<KeyWord> word1 = KWord.multiRegionInfo(evtLoc);
        System.out.println("eventLocation is : "+evtLoc);
        for(KeyWord k: word1){
            System.out.println("加入地点关键词"+k.getWord());
            kwords.add(k);
        }
        //kwords.addAll(word1);
        int i=0;
        for(KeyWord s:word1){
            if(s.getWord()!=null&&!s.getWord().isEmpty())
                System.out.println(++i+" : "+s.getWord());
        }
        word1 = KWord.multiRegionLocInfo(evtLoc);
        for(KeyWord k: word1){
            System.out.println("加入地点关键词"+k.getWord());
            kwords.add(k);
        }
        i=0;
        for(KeyWord s:kwords){
            System.out.println(++i+"  "+s.getWord());

        }
    }

}
