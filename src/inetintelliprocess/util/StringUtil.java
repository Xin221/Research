package inetintelliprocess.util;
/**
 * util辅助工具类
 */
/**
 * 判断字符串是否为空的工具类
 * @author WQH
 */
public class StringUtil {
    /**
     * 判断字符串是否为空，为空返回true,否则false
     * @param str 字符串
     */
    public static boolean isNull(String str) {
        if(str == null || "".equals(str) || "null".equals(str)) {
            return true ;
        }
        return false ;
    }

    /**
     * 判断字符串是否为不为空，不为空返回true,否则false
     * @param str 字符串
     */
    public static boolean isNotNull(String str) {
        if(str != null && !"".equals(str) && !"null".equals(str)) {
            return true ;
        }
        return false ;
    }
}
