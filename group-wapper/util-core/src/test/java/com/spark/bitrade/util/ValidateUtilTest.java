package com.spark.bitrade.util;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *  常用验证工具类测试类
 *
 * @author yangch
 * @time 2019.03.05 17:17
 */
public class ValidateUtilTest {

    /**
     * {@link ValidateUtil#isChinaPhoneLegal(java.lang.String)}
     * @author yangch
     * @time 2019.03.05 17:17 
     */
    @Test
    public void isChinaPhoneLegal() throws Exception {
        /** 13+任意数
                * 15+除4的任意数
                * 18+任意数
                * 17+任意数
                * 147+任意数
                * 16+任意数*/
        assertThat(ValidateUtil.isChinaPhoneLegal("1310000000"), is(false));
        assertThat(ValidateUtil.isChinaPhoneLegal("10000000000"), is(false));
        assertThat(ValidateUtil.isChinaPhoneLegal("11000000000"), is(false));
        assertThat(ValidateUtil.isChinaPhoneLegal("12100000000"), is(false));
        assertThat(ValidateUtil.isChinaPhoneLegal("13100000000"), is(true));
        assertThat(ValidateUtil.isChinaPhoneLegal("15100000000"), is(true));
        assertThat(ValidateUtil.isChinaPhoneLegal("15400000000"), is(false));
        assertThat(ValidateUtil.isChinaPhoneLegal("18000000000"), is(true));
        assertThat(ValidateUtil.isChinaPhoneLegal("17000000000"), is(true));
        assertThat(ValidateUtil.isChinaPhoneLegal("14700000000"), is(true));
        assertThat(ValidateUtil.isChinaPhoneLegal("16000000000"), is(true));
    }

    /**
      * {@link ValidateUtil#isGeneralPhoneLegal(java.lang.String)}
      * @author yangch
      * @time 2019.03.05 17:17 
      */
    @Test
    public void isGeneralPhoneLegal() throws Exception {
        assertThat(ValidateUtil.isGeneralPhoneLegal(""), is(false)); //不能为空
        assertThat(ValidateUtil.isGeneralPhoneLegal("中文"), is(false)); //不能为中文
        assertThat(ValidateUtil.isGeneralPhoneLegal("123en"), is(false)); //不能为英文等字符
        assertThat(ValidateUtil.isGeneralPhoneLegal("18000000000"), is(true));
        assertThat(ValidateUtil.isGeneralPhoneLegal("086-18000000000"), is(true));
        assertThat(ValidateUtil.isGeneralPhoneLegal("086--18000000000"), is(false));
        assertThat(ValidateUtil.isGeneralPhoneLegal("086-180-00000000"), is(true));//支持多个“-”号
        assertThat(ValidateUtil.isGeneralPhoneLegal("+180"), is(true)); //支持“+”号开头，长度不限制
        assertThat(ValidateUtil.isGeneralPhoneLegal("+280+"), is(false)); //不支持 “+”号结尾
    }

    @Test
    public void isMobilePhone() throws Exception {
    }

    @Test
    public void isCard() throws Exception {
    }

    @Test
    public void isnull() throws Exception {
    }

    @Test
    public void isUrl() throws Exception {
    }

    @Test
    public void isEmail() throws Exception {
    }

    @Test
    public void isChineseName() throws Exception {
    }

}