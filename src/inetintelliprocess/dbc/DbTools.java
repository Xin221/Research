package inetintelliprocess.dbc;
/**
 * dbc数据库公用类
 */
import inetintelliprocess.bean.KeyWord;
import inetintelliprocess.searchengine.searchers.WebPageInfo;
import inetintelliprocess.util.LogWriter;
import inetintelliprocess.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.MDC;

/**
 *
 * 辅助业务类
 * 主要负责搜索引擎与数据库相关的操作
 * 包括获取数据库连接、清空数据列表等
 * 对数据表的一些基本操作
 *
 */
public class DbTools {

    public synchronized static boolean insertKeyWord(ArrayList<KeyWord> kwords,String eventID){
        boolean flag = false;
        String sql = null;
        if(kwords==null||kwords.isEmpty() )
            return false;
        try {
            sql = " INSERT INTO eventinfo (kwords)VALUES( '" ;
            for(int i=0; i<kwords.size()-1; i++){
                sql=sql+kwords.get(i).getWord()+",";
            }
            sql=sql+kwords.get(kwords.size()-1).getWord()+"' ) where eventID='"+eventID+"'";
            System.out.println("insert keyword sql::" + sql);
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        }  catch (Exception e) {
            MDC.put("eventID", eventID);
            LogWriter.logger.warn("关键词信息插入事件信息表异常");
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 获取数据表eventinfo中的某事件的关键字
     * @param eventID事件ID
     * @return 关键字列表
     */
    public synchronized static String queryKeyWords(String eventID){
        String kwords = null;
        String sql = new String();
        try{
            sql = "select kwords from eventinfo where eventID=?;";
            kwords = DBConnect.excuteReadOne(sql, new Object[]{eventID});
            System.out.println("sql is "+sql);
            System.out.println("kwords is "+kwords);
        } catch (Exception e) {
            MDC.put("eventID",eventID);
            LogWriter.logger.error("查询eventinfo表中事件"+eventID+"的关键词不成功");
            e.printStackTrace();
        }
        return kwords;
    }

    /**
     * 查询数据表tbname中的记录条数
     * @param tbname数据表名称
     * @return 记录条数
     */
    public synchronized static long querySumItem(String tbname){
        long sum = 0;
        String sql = new String();
        try{
            sql = "select count(*) from " + tbname;
            sum = DBConnect.stat(sql);
        } catch (Exception e) {
            if(tbname!="generalwebinfotbl")
                MDC.put("eventID",tbname);
            else
                MDC.put("eventID","日常搜索");
            LogWriter.logger.error("查询"+tbname+"表记录条数不成功");
            e.printStackTrace();
        }
        return sum;
    }

    /**
     * 查询数据表tbname中的满足条件的WebPageInfo
     * @param tbname数据表名称
     * @return ArrayList<WebPageInfo>
     */
    public synchronized static List<WebPageInfo> querySqlWeb(String tname,String sql){
        List<WebPageInfo> infoList = null;
        try{
            infoList = DBConnect.excuteQuery(WebPageInfo.class, sql);
        } catch (Exception e) {
            LogWriter.logger.error("查询"+tname+"表记录不成功");
            e.printStackTrace();
        }
        return infoList;
    }

    //删除tbname表中相应事件ID的记录
    public synchronized static boolean deleteItemExist(String tbname,String ID){
        String sql = null;
        boolean flag = false;
        try {
            // 检查是否已存在记录
            sql = "SELECT count(*) FROM "+tbname+" WHERE eventID='"
                    + ID + "'";
            if ( DBConnect.stat(sql)>0) {
                // 删除记录
                sql = " DELETE FROM "+tbname+" WHERE eventID='"
                        + ID + "'";
                ;
                if (DBConnect.excuteUpdate(sql)>=0) {
                    flag = true;
                    MDC.put("eventID", ID);
                    LogWriter.logger.info("从"+tbname+"表中删除 eventID=" + ID
                            + "的事件记录");
                }
            }else{
                MDC.put("eventID", ID);
                LogWriter.logger.info(tbname+"表中无事件ID为"+ID + "的事件记录！");
            }
        } catch (Exception e) {
            MDC.put("eventID", ID);
            LogWriter.logger.error("从"+tbname+"表中删除" + ID + "事件记录错误");
            e.printStackTrace();
        }
        return flag;
    }

    //从tbname数据表中删除按照属性attribute降序排列的前num条记录
    public synchronized boolean clearPartInfo(String tbname, String attribute, int num){
        boolean flag = false;
        String sql = null;
        try {
            sql = "delete from " + tbname + " order by "+attribute+" limit "+num;
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    //删除事件衍生表
    public synchronized static boolean dropETables(String ID){
        boolean flag = true;
        if(ID!=null){
            if(isTableExist(ID+"_pagecount"))
                flag=dropTable(ID+"_pagecount");
            if(isTableExist(ID+"_spaceinfo"))
                flag=dropTable(ID+"_spaceinfo");
            if(isTableExist(ID+"_wordinfo"))
                flag=dropTable(ID+"_wordinfo");
            if(isTableExist(ID+"_wordtimestat"))
                flag=dropTable(ID+"_wordtimestat");
        }
        return flag;
    }

    //删除数据表
    public synchronized static boolean dropTable(String tbname){
        boolean flag = false;
        String sql = null;
        try {
            sql = "DROP table "+tbname;
            if(DBConnect.excuteUpdate(sql)>=0){
                System.out.println("drop table "+tbname);
                flag = true;
            }
        } catch (Exception e) {
            //LogWriter.logger.error("删除"+tbname+"表不成功");
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 清除表中所有条目
     * @param tbname 表名
     */
    public synchronized static boolean clearTable(String tbname) {
        boolean flag = false;
        String sql = null;
        try {
            sql = "delete from " + tbname;
            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            //LogWriter.logger.error("清除"+tbname+"表内容不成功");
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 清理舆情信息表publicopinioninfo中警告事件的舆情信息
     * @param eventID 警告事件标识
     */
    public synchronized static boolean clearPubOpiTable(String eventID) {
        boolean flag = false;
        String sql = null;
        try {
            if(StringUtil.isNotNull(eventID))
                sql = "delete from publicopinioninfo where eventID = '" + eventID + "'";
            else
                sql = "delete from publicopinioninfo where eventID = 'generalSearch'";

            if(DBConnect.excuteUpdate(sql)>=0)
                flag = true;
        } catch (Exception e) {
            //LogWriter.logger.error("清除publicopinioninfo表内容不成功");
            e.printStackTrace();
        }
        return flag;
    }
    /**
     * 判断表是否存在
     * @param tbname 表名
     */
    public synchronized static Boolean isTableExist(String tbname) {
        boolean flag = false;
        //String sql = "SHOW TABLES LIKE '" + tbname + "' ";
        String sql = "select count(*) from information_schema.tables  where table_name='" + tbname + "' ";
        try {
            if(DBConnect.stat(sql)>0)
                flag = true;
        } catch (Exception e) {
            //LogWriter.logger.error("查询"+tbname+"表是否存在不成功");
            e.printStackTrace();
        }
        return flag;
    }

    public static void main(String[] args) {
//		String evID = "N30670E10406020140305181541";
        //DbTools dbt=new DbTools();
//		ArrayList<KeyWord> keyList = new ArrayList<KeyWord>();
//		keyList.add(new KeyWord("Hello"));
//		System.out.println(DbTools.insertKeyWord(keyList,evID));
//		System.out.println(DbTools.queryKeyWords(evID));

//		System.out.println(DbTools.querySumItem(evID));
        //System.out.println(DbTools.isTableExist(evID));
        System.out.println(DbTools.clearPubOpiTable("111"));
    }
}
