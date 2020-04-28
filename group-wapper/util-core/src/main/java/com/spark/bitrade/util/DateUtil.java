package com.spark.bitrade.util;

//import com.sparkframework.lang.Convert;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;

public class DateUtil {
    public static final DateFormat YYYY_MM_DD_MM_HH_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat HHMMSS = new SimpleDateFormat("HH:mm:ss");
    public static final DateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat YYYYMMDDMMHHSSSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static final DateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final DateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    //add by yangch 时间： 2018.06.01 原因：增强日期处理
    private static final String[] PARSEPATTERNS = new String[] {
            "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
            "yyyy.MM.dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm",
            "yyyy.MM.dd HH:mm", "yyyy-MM-dd HH", "yyyy/MM/dd HH",
            "yyyy.MM.dd HH", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd" };
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";


    /**
     * 日期转字符串 yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        return YYYY_MM_DD_MM_HH_SS.format(date);
    }

    public static String dateToStringDate(Date date) {
        return YYYY_MM_DD.format(date);
    }

    public static String YYYYMMDDMMHHSSSSS(Date date) {
        return YYYYMMDDMMHHSSSSS.format(date);
    }

    public static String  dateToYYYYMMDDHHMMSS(Date date) {
        return YYYYMMDDHHMMSS.format(date);
    }

    /**
     * 获取当时日期时间串 格式 yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getDateTime() {
        return YYYY_MM_DD_MM_HH_SS.format(new Date());
    }

    /**
     * 获取当时日期串 格式 yyyy-MM-dd
     *
     * @return
     */
    public static String getDate(Date date) {
        return YYYY_MM_DD.format(date);
    }
    public static String getDate() {
        return getDate(new Date());
    }

    public static String getDateYMD() {
        return YYYYMMDD.format(new Date());
    }

    public static String getDateYMD(Date date) {
        return YYYYMMDD.format(date);
    }

    public static Date strToDate(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date strToYYMMDDDate(String dateString) {
        Date date = null;
        try {
            date = YYYY_MM_DD.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static long diffDays(Date startDate, Date endDate) {
        long days = 0L;
        long start = startDate.getTime();
        long end = endDate.getTime();
        days = (end - start) / 86400000L;
        return days;
    }

    public static Date dateAddMonth(Date date, int month) {
        return add(date, 2, month);
    }

    public static Date dateAddDay(Date date, int day) {
        return add(date, 6, day);
    }

    public static Date dateAddYear(Date date, int year) {
        return add(date, 1, year);
    }

    public static String dateAddDay(String dateString, int day) {
        Date date = strToYYMMDDDate(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(6, day);
        return YYYY_MM_DD.format(calendar.getTime());
    }

    public static String dateAddDay(int day) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(6, day);
        return YYYY_MM_DD.format(calendar.getTime());
    }

    public static String dateAddMonth(int month) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(2, month);
        return YYYY_MM_DD.format(calendar.getTime());
    }

    public static String remainDateToString(Date startDate, Date endDate) {
        StringBuilder result = new StringBuilder();
        if (endDate == null) {
            return "过期";
        }
        long times = endDate.getTime() - startDate.getTime();
        if (times < -1L) {
            result.append("过期");
        } else {
            long temp = 86400000L;

            long d = times / temp;

            times %= temp;
            temp /= 24L;
            long m = times / temp;

            times %= temp;
            temp /= 60L;
            long s = times / temp;

            result.append(d);
            result.append("天");
            result.append(m);
            result.append("小时");
            result.append(s);
            result.append("分");
        }
        return result.toString();
    }

    private static Date add(Date date, int type, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, value);
        return calendar.getTime();
    }

    public static String getLinkUrl(boolean flag, String content, String id) {
        if (flag) {
            content = "<a href='finance.do?id=" + id + "'>" + content + "</a>";
        }
        return content;
    }

    public static long getTimeCur(String format, String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.parse(sf.format(date)).getTime();
    }

    public static long getTimeCur(String format, Date date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.parse(sf.format(date)).getTime();
    }

    public static String getStrTime(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        long lcc_time = Long.valueOf(cc_time).longValue();
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;
    }

//    public static String getTimeCurS(String format, Date date) throws ParseException {
//        SimpleDateFormat sf = new SimpleDateFormat(format);
//        return Convert.strToStr(String.valueOf(sf.parse(sf.format(date)).getTime()), "");
//    }

    public static Date getCurrentDate(){
        return new Date();
    }

    public static String getFormatTime(DateFormat format, Date date) throws ParseException {
        return format.format(date);
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static long getTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getWeekDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case 1:
                return "周日";
            case 2:
                return "周一";
            case 3:
                return "周二";
            case 4:
                return "周三";
            case 5:
                return "周四";
            case 6:
                return "周五";
            case 7:
                return "周六";
        }
        return "";
    }

    public static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    /**
     * 得到当前时间与某个时间的差的分钟数
     *
     * @param date
     * @return
     */
    public static BigDecimal diffMinute(Date date) {
        return BigDecimalUtils.div(new BigDecimal(System.currentTimeMillis() - date.getTime()), new BigDecimal("60000"));
    }
    /**
     * 获取过去第几天的日期
     *
     * @param past
     * @return
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }

    /**
     * 获取未来 第 past 天的日期
     * @param past
     * @return
     */
    public static String getFetureDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }


    //----------------------------------
    //add by yangch 时间： 2018.06.01 原因：增强以下时间方法
    /**
     * 将字符串转换成日期类型,自动匹配格式
     *
     * @param date
     * @return
     */
    public static Date stringToDate(String date) {
        try {
            return DateUtils.parseDate(date, PARSEPATTERNS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串格式转日期
     *
     * @param date
     * @param parsePatterns
     * @return
     */
    public static Date stringToDate(String date, String... parsePatterns) {
        try {
            return DateUtils.parseDate(date, parsePatterns);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转字符串 根据给定日期格式，格式化日期
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String dateToString(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(date);
        }
        return "";
    }


    /**
     * 增加n天后的日期
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addDay(Date date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, n);// 增加n天
        return calendar.getTime();
    }

    /**
     * 增加n个月后的日期
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addMonth(Date date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, n);// 增加n个月
        return calendar.getTime();
    }

    /**
     * 获取当前月第一天
     *
     * @return
     */
    public static Date firstDayOfMonth() {
        return firstDayOfMonth(new Date());
    }

    /**
     * 获取指定日期的当月第一天
     * @param date
     * @return
     */
    public static Date firstDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        return c.getTime();
    }


    /**
     * 在日期上加分钟数，得到新的日期
     *
     * @return
     */
    public final static Date addMinToDate(Date date, int min) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MINUTE, min);
        return c.getTime();
    }

    /**
     * 在日期上加days天，得到新的日期
     *
     * @return
     */
    public final static Date addDaysToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    /**
     * 在日期上加months月，得到新的日期
     *
     * @return
     */
    public final static Date addMonthsToDate(Date date, int months) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, months);
        return c.getTime();
    }

    /**
     * 计算两日期之间的天数
     *
     * @return
     */
    public final static int getDaysBetweenDate(String date1, String date2) {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = null;
        try {
            d1 = sd.parse(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date d2 = null;
        try {
            d2 = sd.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c1 = Calendar.getInstance();

        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        long diff = (c2.getTimeInMillis() - c1.getTimeInMillis())
                / (1000 * 60 * 60 * 24);
        return ((Long) diff).intValue();
    }

    /**
     * 计算两日期之间的天数
     *
     * @return
     */
    public final static Integer getDaysBetweenDate(Date date1, Date date2) {
        Date d1 = date1;
        Date d2 = date2;
        Calendar c1 = Calendar.getInstance();

        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        long diff = (c2.getTimeInMillis() - c1.getTimeInMillis())
                / (1000 * 60 * 60 * 24);
        return ((Long) diff).intValue();
    }

    /**
     *
     * @param dateString
     *            日期字符串 如2011-01-03
     * @param dateFormate
     *            日期格式 如yyyy-MM-dd
     * @return 根据传入的日期字符串和日期格式返回指定格式的日期
     */
    public final static Date parseDate(String dateString, String dateFormate) {
        SimpleDateFormat sd = new SimpleDateFormat(dateFormate);
        Date date = null;
        try {
            date = sd.parse(dateString);
        } catch (Exception ex) {

        }
        return date;
    }

    /**
     * 计算两日期之间相隔月份和天数
     *
     * @return
     */
    public static Map<Integer, Integer> getMonthAndDaysBetweenDate(
            String date1, String date2) {
        Map<Integer, Integer> map = new HashMap();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = null;
        try {
            d1 = sd.parse(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date d2 = null;
        try {
            d2 = sd.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int months = 0;// 相差月份
        int days = 0;
        int y1 = d1.getYear();
        int y2 = d2.getYear();
        int dm1 = d2.getMonth();// 起始日期月份
        int dm2 = d2.getMonth();// 结束日期月份
        int dd1 = d1.getDate(); // 起始日期天
        int dd2 = d2.getDate(); // 结束日期天
        if (d1.getTime() < d2.getTime()) {
            months = d2.getMonth() - d1.getMonth() + (y2 - y1) * 12;
            if (dd2 < dd1) {
                months = months - 1;
            }
            days = getDaysBetweenDate(
                    getFormatDateTime(
                            addMonthsToDate(
                                    DateUtil.parseDate(date1, "yyyy-MM-dd"),
                                    months), "yyyy-MM-dd"), date2);
            map.put(1, months);
            map.put(2, days);
        }
        return map;
    }

    /**
     * @function 得到自定义 日期格式
     * @param dateFormat
     * @return
     */
    public final static String getFormatDateTime(Date date, String dateFormat) {

        SimpleDateFormat sf = null;
        try {
            sf = new SimpleDateFormat(dateFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return sf.format(date);
    }


    /**
     * @function 得到自定义 日期格式
     * @param dateFormat
     * @return
     */
    public final static String getFormatDateTime(String date, String dateFormat) {

        SimpleDateFormat sf = null;
        try {
            sf = new SimpleDateFormat(dateFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return sf.format(date);
    }

    /**
     * 返回字符串形式----当前时间的年月日时分秒
     *
     */
    public static String getNewDate() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date);
        return time;
    }


    /**
     * 时间按分钟加减
     */
    public static Date dateAddMinute(Date date,int minute){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minute);
        return cal.getTime();
    }
    public static Date dateAddSecond(Date date,int second){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND,second);
        return cal.getTime();
    }

    /**
     * 比较两个时间相差多少分钟
     */
    public static int compareDateMinute(Date date1,Date date2){
        Calendar dateOne=Calendar.getInstance();
        dateOne.setTime(date1);	//设置date1
        Calendar dateTwo=Calendar.getInstance();
        dateTwo.setTime(date2);	//设置date2
        long timeOne=dateOne.getTimeInMillis();
        long timeTwo=dateTwo.getTimeInMillis();
        long minute=(timeOne-timeTwo)/(1000*60);//转化minute
        return Long.valueOf(minute).intValue();
    }


    /**
     * 比较两个时间相差多少秒
     */
    public static int compareDateSec(Date date1,Date date2){
        Calendar dateOne=Calendar.getInstance();
        dateOne.setTime(date1);	//设置date1
        Calendar dateTwo=Calendar.getInstance();
        dateTwo.setTime(date2);	//设置date2
        long timeOne=dateOne.getTimeInMillis();
        long timeTwo=dateTwo.getTimeInMillis();
        long secend=(timeOne-timeTwo)/1000;//转化secend
        return Long.valueOf(secend).intValue();
    }


    /**
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s)*1000L;
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }


    /**
     * 上月当天
     *
     * @return
     */
    public static String getDayOfPrevMonth(String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        //过去一月
        c.setTime(new Date());
        c.add(Calendar.MONTH, -1);
        Date m = c.getTime();
        String mon = format.format(m);
        return mon;
    }

    /**
     * 获取上月当天，返回 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getDayOfPrevMonth(){
        return getDayOfPrevMonth("yyyy-MM-dd HH:mm:ss");
    }

    /**
     *
     */
    public static Integer  getBetweenYear(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now=new Date();
        String today=sdf.format(now);
        Integer int_today=Integer.valueOf(today);
        Integer int_date=Integer.valueOf(date);
        return (int_today-int_date);
    }

}