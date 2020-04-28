package com.spark.bitrade.util;

import org.apache.shiro.crypto.hash.SimpleHash;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * 主要提供常用类型转换、类型比较、为null判断的封装方法
 * @author fumy
 * @time 2018.11.07 18:09
 */
public class CommonUtils {

    /**
     * 比较两个对象是否相等。<br>
     * 相同的条件有两个，满足其一即可：<br>
     * 1. obj1 == null && obj2 == null; 2. obj1.equals(obj2)
     *
     * @param obj1
     *            对象1
     * @param obj2
     *            对象2
     * @return 是否相等
     */
    public static boolean equals(Object obj1, Object obj2) {
        return (obj1 != null) ? (obj1.equals(obj2)) : (obj2 == null);
    }

    /**
     * 计算对象长度，如果是字符串调用其length函数，集合类调用其size函数，数组调用其length属性，其他可遍历对象遍历计算长度
     *
     * @param obj
     *            被计算长度的对象
     * @return 长度
     */
    public static int length(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }

        int count;
        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            count = 0;
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            count = 0;
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        if (obj.getClass().isArray() == true) {
            return Array.getLength(obj);
        }
        return -1;
    }

    /**
     * 对象中是否包含元素
     *
     * @param obj
     *            对象
     * @param element
     *            元素
     * @return 是否包含
     */
    public static boolean contains(Object obj, Object element) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            if (element == null) {
                return false;
            }
            return ((String) obj).contains(element.toString());
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).contains(element);
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).values().contains(element);
        }

        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            while (iter.hasNext()) {
                Object o = iter.next();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj.getClass().isArray() == true) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(obj, i);
                if (equals(o, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 对象是否为空
     *
     * @param o
     *            String,List,Map,Object[],int[],long[]
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            if (o.toString().trim().equals("")) {
                return true;
            }
        } else if (o instanceof List) {
            if (((List) o).size() == 0) {
                return true;
            }
        } else if (o instanceof Map) {
            if (((Map) o).size() == 0) {
                return true;
            }
        } else if (o instanceof Set) {
            if (((Set) o).size() == 0) {
                return true;
            }
        } else if (o instanceof Object[]) {
            if (((Object[]) o).length == 0) {
                return true;
            }
        } else if (o instanceof int[]) {
            if (((int[]) o).length == 0) {
                return true;
            }
        } else if (o instanceof long[]) {
            if (((long[]) o).length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对象组中是否存在 Empty Object
     *
     * @param os
     *            对象组
     * @return
     */
    public static boolean isOneEmpty(Object... os) {
        for (Object o : os) {
            if (isEmpty(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对象组中是否全是 Empty Object
     *
     * @param os
     * @return
     */
    public static boolean isAllEmpty(Object... os) {
        for (Object o : os) {
            if (!isEmpty(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否为数字
     *
     * @param obj
     * @return
     */
    public static boolean isNumber(Object obj) {
        try {
            Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 格式化字符串 去掉前后空格
     *
     * @param str
     * @return
     */
    public static String format(Object str) {
        if (null == str) {
            return null;
        }
        return str.toString().trim();
    }

    /**
     * 强转->int
     *
     * @param obj
     * @return
     */
    public static int toInt(Object obj) {
        return Integer.parseInt(obj.toString());
    }

    /**
     * 强转->int
     *
     * @param obj
     * @param defaultValue
     * @return
     */
    public static int toInt(Object obj, int defaultValue) {
        try {
            if (isEmpty(obj)) {
                return defaultValue;
            }
            return toInt(obj);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * 强转->long
     *
     * @param obj
     * @return
     */
    public static long toLong(Object obj) {
        return Long.parseLong(obj.toString());
    }

    /**
     * 强转->long
     *
     * @param obj
     * @param defaultValue
     * @return
     */
    public static long toLong(Object obj, long defaultValue) {
        try {
            if (isEmpty(obj)) {
                return defaultValue;
            }
            return toLong(obj);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * 强转->double
     *
     * @param obj
     * @return
     */
    public static double toDouble(Object obj) {
        return Double.parseDouble(obj.toString());
    }

    /**
     * 强转->BigDecimal
     * @param obj
     * @return
     */
    public static BigDecimal toBigDecimal(Object obj){
        return new BigDecimal(obj.toString());
    }

    /**
     * url编码
     * @param url
     * @return
     */
    public static String encodeUrl(String url) {
        try {
            url = isEmpty(url) ? "" : url;
            url = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * url解码
     * @param url
     * @return
     */
    public static String decodeUrl(String url) {
        try {
            url = isEmpty(url) ? "" : url;
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static StringBuilder builder(String... strs) {
        final StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * 判断是否包含
     *
     * @param type
     * @param _type
     * @return boolean
     */
    public static boolean like(String type, String _type) {
        if (type.indexOf(_type) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static void builder(StringBuilder sb, String... strs) {
        for (String str : strs) {
            sb.append(str);
        }
    }

    /**
     * 获取根据盐值计算的md5加密密码
     * @param password
     * @param salt
     */
    public static String getMd5Password(String password,String salt){
        return  new SimpleHash("md5", password, salt, 2).toHex().toLowerCase();
    }

    /**
     * 得到 from 到 to 之间随机数字验证码
     * @param from
     * @param to
     * @return
     */
    public static String getVerifyCode(int from,int to){
       return String.valueOf(GeneratorUtil.getRandomNumber(from, to));
    }
}
