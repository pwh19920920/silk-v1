package com.spark.bitrade.util;

import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.exception.MessageCodeException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 *  断言工具类，与MessageCode和MessageCodeException类结合的断言工具类
 *
 * @author yangch
 * @time 2019.02.19 09:04
 */
public abstract class AssertUtil {
    public static void isTrue(boolean expression, MessageCode message) {
        if (!expression) {
            throw new MessageCodeException(message);
        }
    }

    public static void notNull(Object object, MessageCode message) {
        if (object == null) {
            throw new MessageCodeException(message);
        }
    }

    public static void hasLength(String text, MessageCode message) {
        if (!StringUtils.hasLength(text)) {
            throw new MessageCodeException(message);
        }
    }

    public static void hasText(String text, MessageCode message) {
        if (!StringUtils.hasText(text)) {
            throw new MessageCodeException(message);
        }
    }

    public static void doesNotContain(String textToSearch, String substring, MessageCode message) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring)) {
            throw new MessageCodeException(message);
        }
    }

    public static void notEmpty(Object[] array, MessageCode message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new MessageCodeException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, MessageCode message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new MessageCodeException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, MessageCode message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new MessageCodeException(message);
        }
    }
}
