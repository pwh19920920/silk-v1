package com.spark.bitrade.messager.config;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author ww
 * @time 2019.10.08 14:22
 */
public class ApplicationProperties implements ApplicationListener<ApplicationStartedEvent> {

    public static Map<String, String> propertiesMap = new HashMap<>();
    private String propertyFileName;



    public ApplicationProperties(String propertyFileName) {
        this.propertyFileName = "application.properties";
    }
    public static void loadAllProperties(String propertyFileName) {
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties(propertyFileName);
            processProperties(properties);
            String active = "spring.profiles.active";
            if(properties.containsKey(active)){
                properties = PropertiesLoaderUtils.loadAllProperties(propertyFileName.replaceFirst("(\\.\\w+)$","-"+properties.get(active)+"$1"));
                processProperties(properties);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String getProperty(String name) {
        return propertiesMap.get(name).toString();
    }

    public static Map<String, String> getAllProperty() {
        return propertiesMap;
    }

    private static void processProperties(Properties props) throws BeansException {
        propertiesMap = new HashMap<String, String>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            try {
                // PropertiesLoaderUtils的默认编码是ISO-8859-1,在这里转码一下
                propertiesMap.put(keyStr, new String(props.getProperty(keyStr).getBytes("ISO-8859-1"), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ApplicationProperties.loadAllProperties(propertyFileName);
    }
}