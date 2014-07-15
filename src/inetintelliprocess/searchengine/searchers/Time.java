package inetintelliprocess.searchengine.searchers;
/**
 * searchers搜索核心包
 * 搜索实现
 */


import java.lang.String;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Time {
    ArrayList<String> stimeList = new ArrayList<String>();

    public int countChar(String s, String c) {
        int count = 0;
        String ss = s;
        while (true) {
            int idx = ss.indexOf(c);
            if (idx < 0)
                return count;
            count++;
            ss = ss.substring(idx + 1);
        }

    }

    public ArrayList<String> regParser(String str) throws IOException {

        String s = str;

        Pattern pattern1 = Pattern
                .compile("(((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})[-])(((10|12|0?[13578])[-](3[01]|[12][0-9]|0?[1-9]))|((11|0?[469])[-]((30|[12][0-9]|0?[1-9])))|((0?2)[-](0[1-9]|[1][0-9]|2[0-8]))))|(((([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)[-])(0?2)[-]29))(\\s)?((((0?|1)\\d)|2[0-3]):([0-5]?[0-9])(:([0-5]?[0-9]))?)?");
        Pattern pattern2 = Pattern
                .compile("(((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})[/])(((10|12|0?[13578])[/](3[01]|[12][0-9]|0?[1-9]))|((11|0?[469])[/]((30|[12][0-9]|0?[1-9])))|((0?2)[/](0[1-9]|[1][0-9]|2[0-8]))))|(((([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)[/])(0?2)[/]29))(\\s)?((((0?|1)\\d)|2[0-3]):([0-5]?[0-9])(:([0-5]?[0-9]))?)?");
        Pattern pattern3 = Pattern
                .compile("(((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})年)?(((10|12|0?[13578])月((3[01]|[12][0-9]|0?[1-9])日))|((11|0?[469])月((30|[12][0-9]|0?[1-9])日))|((0?2)月((0[1-9]|[1][0-9]|2[0-8])日))))|(((([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)年)?((0?2)月29日)))(\\s)?((((0?|1)\\d|2[0-3])时)(([0-5]?[0-9])分)(([0-5]?[0-9])秒)?)?");
        Pattern pattern4 = Pattern
                .compile("(((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})年)?(((10|12|0?[13578])月((3[01]|[12][0-9]|0?[1-9])日))|((11|0?[469])月((30|[12][0-9]|0?[1-9])日))|((0?2)月((0[1-9]|[1][0-9]|2[0-8])日))))|(((([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)年)?((0?2)月29日)))(\\s)?((((0?|1)\\d)|2[0-3]):([0-5]?[0-9])(:([0-5]?[0-9]))?)?");
        Pattern pattern5 = Pattern
                .compile("(((((3[01]|[12][0-9]|0?[0-9])((\\s)|-|,)*?(January|March|May|July|August|October|December))|((30|[12][0-9]|0?[1-9])((\\s)|-|,)*?(April|June|September))|((0?[1-9]|[1][0-9]|2[0-8])(\\s|-|,)*?February))(\\s|-|,)*?(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})))|(29(\\s|-|,)*?February(([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)))(\\s|-|,)*?((((0?|1)\\d)|2[0-3]):([0-5]?[0-9]))?");
        //pattern5为英文网站时间格式，例如6 June 2013
        Pattern pattern6 = Pattern
                .compile("(((((January|March|May|July|August|October|December)((\\s)|-|,)*?(3[01]|[12][0-9]|0?[0-9]))" +
                        "|((April|June|September)((\\s)|-|,)*?(30|[12][0-9]|0?[1-9]))" +
                        "|(February(\\s|-|,)*?(0?[1-9]|[1][0-9]|2[0-8])))(\\s|-|,)*?([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3}))" +
                        "|(February(\\s|-|,)*?29(\\s|-|,)*?(([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)))(\\s|-|,)*?((((0?|1)\\d)|2[0-3]):([0-5]?[0-9]))?");

        if(s != null){
            Matcher matcher1 = pattern1.matcher(s);
            Matcher matcher2 = pattern2.matcher(s);
            Matcher matcher3 = pattern3.matcher(s);
            Matcher matcher4 = pattern4.matcher(s);
            Matcher matcher5 = pattern5.matcher(s);
            Matcher matcher6 = pattern6.matcher(s);

            while (matcher1.find()) {
                stimeList.add(matcher1.group());
            }
            while (matcher2.find()) {
                stimeList.add(matcher2.group());
            }
            while (matcher3.find()) {
                stimeList.add(matcher3.group());
            }
            while (matcher4.find()) {
                stimeList.add(matcher4.group());
            }
            while (matcher5.find()) {
                stimeList.add(matcher5.group());
            }
            while (matcher6.find()) {
                stimeList.add(matcher6.group());
            }
        }

        return stimeList;
    }

    public ArrayList<String> getAllTime(WebPageInfo info, String newHtml/*Parser parser*/) {

        try {
            regParser(info.getPageTitle());
            regParser(info.getKeyWords());
            regParser(info.getAbstract());
            regParser(newHtml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stimeList;
    }

    public String getTime(ArrayList<String> timeList) {
        if (timeList == null || timeList.size() == 0)
            return null;
        String minTime = null;
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowTime = new Date();
        String nTime = df1.format(nowTime);
        try {
            nowTime = df1.parse(nTime);
            int count = 0;
            while (timeList != null
                    && timeList.size() > count
                    && (timeList.get(count).equals("") || timeList.get(count)
                    .equals(null))
                    && this.formateDate(timeList.get(count)) == null) {
                count++;
            }
            minTime = timeList.get(count);
            Date time = null;
            time = formateDate(minTime);
            for (int i = count + 1; i < (timeList.size()); i++) {
                Date pasTime = null;

                String str1 = timeList.get(i);
                if (str1 != null && !str1.equals(null)
                        && (pasTime = this.formateDate(str1)) != null
                        && minTime != null && !minTime.equals(null)
                        && time != null) {
                    long diff1 = pasTime.getTime();
                    long diff2 = time.getTime();
                    time = (diff1 > diff2) ? pasTime : time;
                    if (diff1 == diff2) {
                        // df1转换
                        if (df1.format(pasTime).length() >= df1.format(time)
                                .length()) {
                            time = pasTime;
                        }
                    }
                }
            }// for
            if(time!=null){
                minTime = df1.format(time);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return minTime;
    }

    //修改之后的版本7月4日
    boolean isValidTime(Calendar cal,Calendar calTime){
        boolean flag = true;
        int yearValue1 = cal.get(Calendar.YEAR);
        int yearValue2 = calTime.get(Calendar.YEAR);
        if (yearValue2 > yearValue1)
            flag = false;
        else if (yearValue2 == yearValue1){
            int monthValue1 = cal.get(Calendar.MONTH);
            int monthValue2 = calTime.get(Calendar.MONTH);
            if (monthValue2 > monthValue1)
                flag = false;
            else if (monthValue2 == monthValue1) {
                int dayValue1 = cal.get(Calendar.DATE);
                int dayValue2 = calTime.get(Calendar.DATE);
                if (dayValue2 > dayValue1)
                    flag = false;
                else if (dayValue2 == dayValue1) {
                    int hourValue1 = cal.get(Calendar.HOUR);
                    int hourValue2 = calTime.get(Calendar.HOUR);
                    if (hourValue2 > hourValue1)
                        flag = false;
                    else if (hourValue2 == hourValue1) {
                        int minValue1 = cal.get(Calendar.MINUTE);
                        int minValue2 = calTime.get(Calendar.MINUTE);
                        if (minValue2 > minValue1)
                            flag = false;
                        else if (minValue2 == minValue1) {
                            int secValue1 = cal.get(Calendar.SECOND);
                            int secValue2 = calTime
                                    .get(Calendar.SECOND);
                            if (secValue2 > secValue1)
                                flag = false;
                        }
                    }
                }
            }
        }
        return flag;
    }


    public Date formateDate(String str) {
        Date dateTime = null;
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df7 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        SimpleDateFormat df3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat df4 = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        SimpleDateFormat df5 = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
        SimpleDateFormat df6 = new SimpleDateFormat("MM dd yyyy HH:mm:ss");
        try {
            if (!"".equals(str)) {
                Calendar cal = Calendar.getInstance();
                Calendar calTime = Calendar.getInstance();
                cal.setTime(new Date());
                // 判断日期的时、分、秒是否为空
                if (str.contains("月") && str.contains("日")) {
                    if (!str.contains("年")) {
                        str = String.valueOf(cal.get(Calendar.YEAR)) + "年"
                                + str;
                    }
                    str = str.replace("\n","");
                    str = str.replace("\r","");
                    str = str.replace("  ", " ");
                    if (this.countChar(str, ":") == 1) {
                        str += ":00";
                        dateTime = df4.parse(str);
                    }else if(this.countChar(str, ":") == 2){
                        dateTime = df4.parse(str);
                    }
                    else if (this.countChar(str, ":") == 0) {
                        if (!str.contains("时")) {
                            str += "00时00分00秒";

                        } else if (!str.contains("分")) {
                            str += "00分00秒";
                        } else if (!str.contains("秒")) {
                            str += "00秒";
                        }
                        str = str.replace("\n","");
                        str = str.replace("\r","");
                        str = str.replace("  ", " ");
                        dateTime = df2.parse(str);
                    }
                    if(dateTime!=null){
                        calTime.setTime(dateTime);
                        if(!isValidTime(cal,calTime)){
                            str = str.substring(str.indexOf("年")+1);
                            str = String.valueOf(cal.get(Calendar.YEAR)-1) + "年"
                                    + str;
                            str = str.replace("\n","");
                            str = str.replace("\r","");
                            str = str.replace("  ", " ");
                            if(countChar(str, ":") == 0){
                                dateTime = df2.parse(str);
                            }else{
                                dateTime = df4.parse(str);
                            }
                        }
                    }
                } else if (str.contains("-")) {
                    if (str.matches("\\d{1,2}-\\d{1,2}")) {
                        str = String.valueOf(cal.get(Calendar.YEAR)) + "-"
                                + str;
                    }
                    if (this.countChar(str, ":") == 1)
                        str += ":00";
                    else if (this.countChar(str, ":") == 0)
                        str += " 00:00:00";
                    str=str.replaceAll("January", "01");
                    str=str.replaceAll("February", "02");
                    str=str.replaceAll("March", "03");
                    str=str.replaceAll("April", "04");
                    str=str.replaceAll("May", "05");
                    str=str.replaceAll("June", "06");
                    str=str.replaceAll("July", "07");
                    str=str.replaceAll("August", "08");
                    str=str.replaceAll("September", "09");
                    str=str.replaceAll("October", "10");
                    str=str.replaceAll("November", "11");
                    str=str.replaceAll("December", "12");
                    str = str.replaceAll("[a-zA-Z]", " ");
                    str = str.replace("\n","").trim();
                    str = str.replace("\r","").trim();
                    str = str.replace("  ", " ").trim();
                    if(str.matches("\\d{1,2}-\\d{1,2}-\\d{4}"))
                        dateTime = df7.parse(str);
                    else if(str.matches("\\d{1,2}-\\d{1,2} \\d{4} \\d{2}:\\d{2}:\\d{2}"))
                        dateTime = new SimpleDateFormat("dd-MM yyyy HH:MM:ss").parse(str);
                    else
                        dateTime = df1.parse(str);
                    calTime.setTime(dateTime);
                    if(!isValidTime(cal,calTime)){
                        str = str.substring(str.indexOf("-")+1);
                        str = String.valueOf(cal.get(Calendar.YEAR)-1) + "-" + str;
                        str = str.replace("\n","");
                        str = str.replace("\r","");
                        str = str.replace("  ", " ");
                        dateTime = df1.parse(str);
                    }
                } else if (str.contains("/")&&!str.matches(".*[a-zA-z].*")) {
                    if (str.matches("\\d{1,2}/\\d{1,2}")) {
                        str = String.valueOf(cal.get(Calendar.YEAR)) + "/" + str;
                    }
                    if (this.countChar(str, ":") == 1)
                        str += ":00";
                    else if (this.countChar(str, ":") == 0)
                        str += " 00:00:00";
                    str = str.replace("\n","");
                    str = str.replace("\r","");
                    str = str.replace("  ", " ");
                    dateTime = df3.parse(str);
                    calTime.setTime(dateTime);
                    if(!isValidTime(cal,calTime)){
                        str = str.substring(str.indexOf("/")+1);
                        str = String.valueOf(cal.get(Calendar.YEAR)-1) + "/" + str;
                        str = str.replace("\n","");
                        str = str.replace("\r","");
                        str = str.replace("  ", " ");
                        dateTime = df3.parse(str);;
                    }
                }
                if (str.contains("January")||str.contains("February")||str.contains("March")||str.contains("April")||str.contains("May")||str.contains("June")||
                        str.contains("July")||str.contains("August")||str.contains("September")||str.contains("October")||str.contains("November")||str.contains("December")){
                    str=str.replace("-", " ");
                    str=str.replace(",", " ");
                    str=str.replace("/", " ");
                    str = str.replace("\n","");
                    str = str.replace("\r","");
                    str = str.replace("  ", " ");
                    String str11 = null,str22 = null;
                    if(str.matches("(((((3[01]|[12][0-9]|0?[0-9])((\\s)|-|,)*?(January|March|May|July|August|October|December))|((30|[12][0-9]|0?[1-9])((\\s)|-|,)*?(April|June|September))|((0?[1-9]|[1][0-9]|2[0-8])(\\s|-|,)*?February))(\\s|-|,)*?(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})))|(29(\\s|-|,)*?February(([0-9]{2})(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[3579][26])00)))(\\s|-|,)*?((((0?|1)\\d)|2[0-3]):([0-5]?[0-9]))?"))
                        str11=str;
                    else
                        str22=str;
                    if (str.matches("\\d{1,2}(\\s)?(January|February|March|April|May|June|July|August|September|October|November|December)")) {
                        str11 = str11 + " "	+ String.valueOf(cal.get(Calendar.YEAR));
                    }
                    if (str11!=null&&str11.matches("\\d{1,2}(January|February|March|April|May|June|July|August|September|October|November|December)\\d{4}")) {
                        if(String.valueOf(str11.charAt(1)).matches("[A-Za-z]")){
                            str11 = str11.substring(0, 1)+" "+str11.substring(1, str11.length()-4)+" "+str11.substring(str11.length()-4);
                        }
                    }
                    if(str.matches("(January|February|March|April|May|June|July|August|September|October|November|December)(\\s)?\\d{1,2}")){
                        str22 = str22 + " "+ String.valueOf(cal.get(Calendar.YEAR));
                    }
                    if (str22!=null&&str22.matches("(January|February|March|April|May|June|July|August|September|October|November|December)\\d{1,2}\\d{4}")) {
                        if(String.valueOf(str22.charAt(str22.length()-6)).matches("[A-Za-z]")){
                            str22 = str22.substring(0,str22.length()-6)+" "+str22.substring(str22.length()-6, str22.length()-4)+" "+str22.substring(str22.length()-4);
                        }
                    }

                    if(str11!=null){
                        str11=str11.replaceAll("January", "01");
                        str11=str11.replaceAll("February", "02");
                        str11=str11.replaceAll("March", "03");
                        str11=str11.replaceAll("April", "04");
                        str11=str11.replaceAll("May", "05");
                        str11=str11.replaceAll("June", "06");
                        str11=str11.replaceAll("July", "07");
                        str11=str11.replaceAll("August", "08");
                        str11=str11.replaceAll("September", "09");
                        str11=str11.replaceAll("October", "10");
                        str11=str11.replaceAll("November", "11");
                        str11=str11.replaceAll("December", "12");
                    }
                    if(str22!=null){
                        str22=str22.replaceAll("January", "01");
                        str22=str22.replaceAll("February", "02");
                        str22=str22.replaceAll("March", "03");
                        str22=str22.replaceAll("April", "04");
                        str22=str22.replaceAll("May", "05");
                        str22=str22.replaceAll("June", "06");
                        str22=str22.replaceAll("July", "07");
                        str22=str22.replaceAll("August", "08");
                        str22=str22.replaceAll("September", "09");
                        str22=str22.replaceAll("October", "10");
                        str22=str22.replaceAll("November", "11");
                        str22=str22.replaceAll("December", "12");
                    }

                    if(str11!=null){
                        if (this.countChar(str11, ":") == 1)
                            str11 += ":00";
                        else if (this.countChar(str11, ":") == 0)
                            str11 += " 00:00:00";
                        str11 = str11.replace("\n","");
                        str11 = str11.replace("\r","");
                        str11 = str11.replace("  ", " ");
                        dateTime = df5.parse(str11);
                        calTime.setTime(dateTime);
                        if(!isValidTime(cal,calTime)){
                            calTime.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
                            dateTime = calTime.getTime();
                        }
                    }
                    if(str22!=null){
                        if (this.countChar(str22, ":") == 1)
                            str22 += ":00";
                        else if (this.countChar(str22, ":") == 0)
                            str22 += " 00:00:00";
                        str22 = str22.replace("\n","").trim();
                        str22 = str22.replace("\r", "").trim();
                        str22 = str22.replace("  ", " ").trim();
                        dateTime = df6.parse(str22);
                        calTime.setTime(dateTime);
                        if(!isValidTime(cal,calTime)){
                            calTime.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
                            dateTime = calTime.getTime();
                        }
                    }

                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return dateTime;
    }

    public static void main(String[] args){
        Time time = new Time();
        try {
            //ArrayList<String> list = time.regParser("<span class=\"g\">baike.baidu.com/&nbsp;2013-12-11&nbsp;</span>");
            ArrayList<String> list = time.regParser("2013-12-11");
            System.out.println(list.size());
            for(String ti:list){
                System.out.println(ti);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
