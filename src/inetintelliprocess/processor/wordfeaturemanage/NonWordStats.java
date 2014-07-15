package inetintelliprocess.processor.wordfeaturemanage;

import inetintelliprocess.util.LogWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.LockObtainFailedException;
import org.wltea.analyzer.lucene.IKQueryParser;

//生词提取及其排序组织管理的业务类
//负责使用IKQuery- Parser查询分析器提取生词、计算生词词频和生词按照词频排序等相关的业务操作
public class NonWordStats{

    private String key = null;
    private Integer count = 0;

    public NonWordStats () {

    }

    public NonWordStats (String key, Integer count) {
        this.key = key;
        this.count = count;
    }

    public String toString() {
        return key + " 出现的次数为：" + count;
    }

    public String getKey() {
        return key;
    }

    public Integer getCount() {
        return count;
    }

    private Map<String,Integer> map = new HashMap<String,Integer>();

    public ArrayList<String> getKeyword(String title) {
        ArrayList<String> keyword = new ArrayList<String>();
        try {
            BooleanQuery.setMaxClauseCount(10000);
            //使用IKQueryParser查询分析器构造Query对象
            if(title.length() > 500 ){
                title = title.substring(0 , 499) ;
                for(int i = 0 ; i < 10 ; i ++){
                    String idxString = String.valueOf(i);
                    title = title.replace( idxString, "") ;
                }
            }

            Query query1 = IKQueryParser.parse("", title);
            String str = query1.toString();
            str = str.replace(" ", "+");
            str = str.replace("(", "");
            str = str.replace(")", "");
            str = str.replace("++", "+");

            String[] words = str.split("\\+");/** \\+ **/
            for(int j = 0; j < words.length; j++){
                if(words[j].length() > 1){
                    keyword.add(words[j]);
                    getCount(title,words[j]);
                }
            }
        } catch (CorruptIndexException e) {
            LogWriter.logger.warn("分析器:org.apache.lucene.index.CorruptIndexException");
            e.printStackTrace();
        } catch (LockObtainFailedException e) {
            LogWriter.logger.warn("分析器: org.apache.lucene.store.LockObtainFailedException");
            e.printStackTrace();
        } catch (IOException e) {
            LogWriter.logger.warn("分析器:java.io.IOException");
            e.printStackTrace();
        }
        return keyword;
    }

    public int getCount(String words, String key) {
        int count = 0;
        int temp;
        if(words.length() >= key.length()) {
            for(int i = 0; i <= words.length(); i++) {
                temp = words.indexOf(key);
                if(temp >= 0) {
                    ++count;
                    words = words.substring(temp+1);
                }
            }
        }
        map.put(key,count);
        return count;
    }

    public void reset() {
        map.clear();
    }

    public List<NonWordStats> sort(ArrayList<String> pageWordList, int number) {

        List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        //排序
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        //排序后
        List<NonWordStats> list = new ArrayList<NonWordStats>();
        for (int i = 0; i < number; i++) {

            String key = infoIds.get(i).getKey();
            int count = infoIds.get(i).getValue();
            list.add(new NonWordStats(key,count));
			/*newWordVector nwv = new newWordVector(key, count);
					list.add(nwv);*/
        }
        return list;
    }

    public ArrayList<inetintelliprocess.bean.KeyWord> generateKeys(String eventDes) {
        NonWordStats nonWord = new NonWordStats();
        ArrayList<String> nonwords = nonWord.getKeyword(eventDes);
        ArrayList<inetintelliprocess.bean.KeyWord> word = new ArrayList<inetintelliprocess.bean.KeyWord>();
        for(String key : nonwords){
            inetintelliprocess.bean.KeyWord keys = new inetintelliprocess.bean.KeyWord();
            keys.setWord(key);
            word.add(keys);
        }
        return word;
    }

    public static void main(String[] args) {
        //----中文分词----
        String docs = "北京市环保局上午11时召开新闻通报会，联合相关委办局一起，介绍《北京市空气重污染应急方案》实施情况，以及近几天空气质量状况及预报信息。以下为现场文字直播。　　方力（北京市环保局副局长）：尊敬的各位媒体朋友大家好，上周四下午由于受到大范围空气污染影响，我市空气质量逐渐转差，部分地区在周五达到严重污染，按照北京市重污染日应急预案的有关规定，我时分级、分区实施了该方案。今天通气会主要向各位介绍目前空气质量情况，今后几天的趋势以及重污染日应急方案的实施情况。　　出席今天通气会的有市经信委李洪副主任，以及市交委、市住建委、市卫生局、市交管局、市城管执法局以及市环保局相关处室负责人。首先，我们请市环保监测中心张大伟主任介绍我市空气质量现状以及发展趋势。　　张大伟（北京市环保局环保监测中心主任）：下面我把近期空气重污染有关情况做一个介绍。1月10日到13日，基于国家新的环境空气质量标准，本市空气质量连续四天污染级别维持在重度和严重污染的水平。其中重度污染日从11日到13日都是严重污染。本次污染过程具有影响范围广，持续时间长，浓度水平高三个显著的特点。近年来是比较罕见的。　　第一，区域性污染特征明显。全国中东部地区整体大范围污染，环保地区尤其明显，多个城市空气质量达到严重污染级别。　　第二，持续时间长，1月9日北京空气质量还是二级，良好。10日直接转入五级重污染，连续跨过三级和四级两个级别。从1月10日开始到昨天是四天的重污染日，我们预计14日、15日两天，空气污染水平仍然较高，仍然是重度污染，这样整个重污染持续时间达到了6天。　　第三，浓度水平高。从10日傍晚起，本市城区和城南部地区大部分空气质量监测小时PM2.5浓度超过300微克每立方米，此后污染水平持续上升。到12日达到峰值，很多子站小时PM2.5浓度超过700微克每立方米，在此之后浓度有所波动，缓慢下降。目前情况有所好转。我们最新的监测数据今天上午10点钟，城区和城南地区PM2.5小时浓度仍然维持在250微克每立方米以上，还属于重度污染，但是整个北部地区，密云、延庆、怀柔，浓度水平下降到100多微克每立方米，有所好转。　　造成此次重污染的原因，我们初步分析有三个方面。第一，我们污染物排放量大，燃煤、机动车、工业、扬尘，这些污染源排放量大，造成本次严重污染的根本原因。北京是特大型城市，城市运行带来的各类污染物排放量非常大，当污染扩散条件不利，污染源排放污染物难以扩散，空气污染随之加重。特别是2012年12月以来，整个华北地区处于极端低温天气，同比温度比往年要低，由于低温导致燃煤采暖排放量相应增加，这个影响也比较大。根本原因还是污染物排放大。　　第二，扩散条件不利。近期极端不利的污染扩散条件是形成本次污染过程的直接原因。10日到13日，北京地区地面闭合低压控制，地面风速减少，湿度加大，并且逐渐形成了静风逆温和大雾极端天气扩散，造成持续积累造成本次污染过程。　　第三，区域污染和本地污染贡献叠加，由于PM2.5污染区域性以及相关联区域污染传输，也是形成本次重污染的重要因素。近期，北京地区西南部、东南部，以及向南的周边地区污染水平明显高于我们城区，特别是北部地区，大范围，大区域尺度内污染物的输送和我们北京本地排放污染物相叠加，使PM2.5污染物浓度水平进一步升高，也客观上加重了我们北京地区的污染水平。　　最新的监测数据和气象资料表明，14日到15日，我们华北地区整体上仍然是维持在低压系统控制形态，目前严重空气污染状况难以根本好转，重污染过程仍将持续。15日夜间，本市将受到一股较为明显的冷空气影响，地面转为冷高压前部，偏北风达到三到四级，我们北京以及周边区域的污染物可以得到有效的清除，本次大范围重污染的过程基本结束。16日、17日我们预计空气质量可以达到优良的级别。";
        //String docs = "Hello, ni hao!";
        NonWordStats non = new NonWordStats();
        ArrayList<inetintelliprocess.bean.KeyWord> word = non.generateKeys(docs);
        for(inetintelliprocess.bean.KeyWord s : word){
            System.out.println("生词:"+s.getWord()+"；词频:"+s.getCount());
        }

//		long end = System.currentTimeMillis();
//		System.out.println((end-start)+"毫秒");
    }


}
