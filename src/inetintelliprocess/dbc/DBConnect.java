package inetintelliprocess.dbc;

import inetintelliprocess.util.Config;

import java.beans.PropertyVetoException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnect {
    private static ComboPooledDataSource searchdataSource = new ComboPooledDataSource("search");
    private static ThreadLocal<Connection> local = new ThreadLocal<Connection>();
    static{
        searchdataSource.setJdbcUrl("jdbc:mysql://"+Config.read("DATABASE_IP")+":"+Config.read("DATABASE_PORT")+"/"+Config.read("DATABASE_SID")+"?useUnicode=true&characterset=utf-8&autoReconnect=true");
        searchdataSource.setUser(Config.read("DATABASE_USERNAME"));
        searchdataSource.setPassword(Config.read("DATABASE_PASSWORD"));
        try {
            searchdataSource.setDriverClass(Config.read("DATABASE_DRIVER"));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource(){
        return (DataSource)searchdataSource;
    }

    public static Connection getConnection() throws SQLException {
        if (getDataSource() == null) {
            throw new SQLException("DataSource is null.");
        }
        if(local.get() == null){
            local.set(getDataSource().getConnection());
        }
        return local.get();
    }



    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> excuteQuery(Class<T> beanClass, String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (List<T>) runner.query(getConnection(), sql, new BeanListHandler(beanClass));
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> excuteQuery(Class<T> beanClass, String sql, Object...params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (List<T>) runner.query(getConnection(), sql,new BeanListHandler(beanClass), params);
    }

    /**
     * 读取某个对象中的属性值
     * param sql
     * param params
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T excuteReadOne(String sql, Object...params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (T) runner.query(getConnection(), sql,new ScalarHandler(),params);
    }

    /**
     * 读取某个对象中的属性值
     * param sql
     * param params
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T excuteReadOne(Class<T> beanClass, String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (T) runner.query(getConnection(), sql,new ScalarHandler());
    }

    /**
     * 读取某个对象
     * param sql
     * param params
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T excuteReadOneRow(Class<T> beanClass, String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (T) runner.query(getConnection(), sql,new BeanHandler(beanClass));
    }

    /**
     * 读取某个对象
     * param sql
     * param params
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T excuteReadOneRow(Class<T> beanClass, String sql, Object...params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (T) runner.query(getConnection(), sql,new BeanHandler(beanClass), params);
    }


    /**
     * 读取某列
     * param sql
     * param params
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> excuteReadOneColumn(String columnName, String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (List<T>) runner.query(getConnection(), sql,new ColumnListHandler(columnName));
    }

    /**
     * 读取某列
     * param sql
     * param params
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> excuteReadOneColumn(String columnName, String sql, Object...params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return (List<T>) runner.query(getConnection(), sql,new ColumnListHandler(columnName), params);
    }

    /**
     * 执行统计查询语句，语句的执行结果必须只返回一个数值
     * @param <T>
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static long stat(String sql) throws SQLException{
        ScalarHandler handler = new ScalarHandler(){
            public Object handle(ResultSet rs) throws SQLException {
                Object obj = super.handle(rs);
                if(obj instanceof BigInteger)
                    return ((BigInteger)obj).longValue();
                return obj;
            }
        };
        QueryRunner runner = new QueryRunner();
        //因为Number是AtomicInteger, AtomicLong, BigDecimal, BigInteger,  
        //Byte, Double, Float, Integer, Long, Short的超类。 
        //不管返回哪种数字类型 都可以转换为long类型。
        @SuppressWarnings("unchecked")
        Number num = (Number)runner.query(getConnection(), sql, handler);
        return (num!=null)?num.longValue():-1;
    }

    /**
     * 执行统计查询语句，语句的执行结果必须只返回一个数值
     * @param <T>
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static long stat(String sql, Object...params) throws SQLException{
        ScalarHandler handler = new ScalarHandler(){
            public Object handle(ResultSet rs) throws SQLException {
                Object obj = super.handle(rs);
                if(obj instanceof BigInteger)
                    return ((BigInteger)obj).longValue();
                return obj;
            }
        };
        QueryRunner runner = new QueryRunner();
        //因为Number是AtomicInteger, AtomicLong, BigDecimal, BigInteger,  
        //Byte, Double, Float, Integer, Long, Short的超类。 
        //不管返回哪种数字类型 都可以转换为long类型。
        @SuppressWarnings("unchecked")
        Number num = (Number)runner.query(getConnection(), sql, handler, params);
        return (num!=null)?num.longValue():-1;
    }


    /**
     * 执行INSERT/UPDATE/DELETE语句 (有参数)
     * @param sql
     * @param params
     * @return
     */

    public static int excuteUpdate(String sql,Object...params) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.update(getConnection(),sql,params);
    }

    /**
     * 执行INSERT/UPDATE/DELETE语句 (无参数)
     * @param sql
     * @param params
     * @return
     */

    public static int excuteUpdate(String sql) throws SQLException {
        QueryRunner runner = new QueryRunner();
        return runner.update(getConnection(),sql);
    }


    /**
     * 批量执行指定的SQL语句
     * @param sql
     * @param params
     * @return
     */
    public static int[] batch(String sql, Object[][] params) throws SQLException{
        QueryRunner runner = new QueryRunner();
        return runner.batch(getConnection(), sql, params);
    }



    /**
     * @param args
     */
    public static void main(String[] args) {
        //test for excuteQuery
//		List<WebPageInfo> list;
//		String sql = "SELECT * FROM n30670e10406020140305181541 where sendTo='0'";
//		try {
//			list = DBConnect.excuteQuery(WebPageInfo.class,sql,(Object[])null);
//		
//			if(list==null){
//				System.out.println("list is null!");
//			}else if (list.isEmpty()) {
//				System.out.println("list is empty!");
//			}else{
//				System.out.println("list size is "+list.size());
//				System.out.println("PageId\tPageTitle\tPageTime\tUrl\tKeyWords");
//				for (WebPageInfo info : list) {
//					System.out.println(info.getPageId()+"\t"+info.getPageTitle()+"\t"+info.getPageTime()+"\t"+info.getUrl()+"\t"+info.getKeyWords());
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}

        List<Integer> list;
        //String sql = "select kwords from eventinfo where eventID='n30670e10406020140305181541'";
        String sql = "select * from N30670E10406020140305181541";
        try {
            list = DBConnect.excuteReadOneColumn("pageId",sql);

            if(list==null){
                System.out.println("list is null!");
            }else if (list.isEmpty()) {
                System.out.println("list is empty!");
            }else{
                //System.out.println("list is "+list);
                //System.out.println("PageId\tPageTitle\tPageTime\tUrl\tKeyWords");
                for (Integer info : list) {
                    //System.out.println(info.getEventName()+"\t"+info.getEventTime()+"\t"+info.getLocx()+"\t"+info.getLocy()+"\t"+info.getKwords());
                    System.out.println(info.toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}