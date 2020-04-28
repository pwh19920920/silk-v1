package com.spark.bitrade.envent;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.util.SpringContextUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * IEnventHandler接口的工具类
 *
 * @author yangch
 * @time 2018.12.13 17:18
 */
public class EnventHandlerUtil {
    public static <T> Map<String, T> getBeansOfType(Class<T> t){
      return SpringContextUtil.getApplicationContext().getBeansOfType(t);
    }

    /**
     * 获取实现IEnventHandler接口的的子类集合
     * @param t IEnventHandler接口的子接口
     * @return null 或 List集合
     */
    public static <T extends IEnventHandler> List<T> getBeans(Class<T> t){
        Map<String, T> map = getBeansOfType(t);
        if(map == null || map.size() == 0){
            return null;
        } else {
            return map.values().stream().sorted(Comparator.comparing(T::enventOrder)).collect(Collectors.toList());
        }
    }

    /**
     * 执行 IEnventHandler接口的实现方法
     * @param t IEnventHandler接口的子接口
     * @param carrier 事件源
     */
    public static  <T extends IEnventHandler> void callHandle(Class<T> t,final CollectCarrier carrier){
        List<T> lst = getBeans(t);
        if(lst != null) {
            lst.forEach(h->h.handle(carrier));
        }
    }
}
