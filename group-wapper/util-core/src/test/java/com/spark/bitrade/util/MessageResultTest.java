package com.spark.bitrade.util;

import com.spark.bitrade.enums.MessageCode;
import org.junit.Test;
import org.springframework.util.Assert;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *  MessageResult工具测试类
 *
 * @author yangch
 * @time 2019.03.05 10:49
 */
public class MessageResultTest {

    /**
     * {@link MessageResult#success()}
     *
     * @author yangch
     * @time 2019.03.05 14:32 
     */
    @Test
    public void success() throws Exception {
        MessageResult result = MessageResult.success();

        assertThat(result.getCode(), is(0));
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getMessage(), equalToIgnoringCase("SUCCESS"));
    }

    /**
     * {@link MessageResult#success(java.lang.String)}
     * @author yangch
     * @time 2019.03.05 14:32 
     */
    @Test
    public void success1() throws Exception {
        MessageResult result = MessageResult.success("SUCCESSED");

        assertThat(result.getCode(), is(0));
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getMessage(), is("SUCCESSED"));
    }

    /**
     * {@link MessageResult#success(java.lang.String, java.lang.Object)}
     *
     * @author yangch
     * @time 2019.03.05 14:32 
     */
    @Test
    public void success2() throws Exception {

        MessageResult result = MessageResult.success("SUCCESSED", new String("MessageResultData"));
        //MessageResult result = MessageResult.success("SUCCESSED", null);

        assertThat(result.getCode(), is(0));
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getMessage(), is("SUCCESSED"));
        Assert.notNull(result.getData(), "MessageResult 结果不能为空");
        assertThat(result.getData(), is("MessageResultData"));
    }

    /**
     * {@link MessageResult#error( java.lang.String)}
     * @author yangch
     * @time 2019.03.05 14:32 
     */
    @Test
    public void error() throws Exception {
        MessageResult result = MessageResult.error( "error 500");

        assertThat(result.getCode(), is(500));
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("error 500"));
    }

    /**
      * {@link MessageResult#error(int, java.lang.String)}
      * @author yangch
      * @time 2019.03.05 14:32 
      */
    @Test
    public void error1() throws Exception {
        MessageResult result = MessageResult.error(600, "error 600");

        assertThat(result.getCode(), is(600));
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("error 600"));
    }

    /**
      * {@link MessageResult#error(com.spark.bitrade.enums.MessageCode, java.lang.String)}
      * @author yangch
      * @time 2019.03.05 14:32 
      */
    @Test
    public void error2() throws Exception {
        MessageResult result = MessageResult.error(MessageCode.INVALID_AUTH_TOKEN, "error MessageCode");

        assertThat(result.getCode(), is(3011));
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("error MessageCode"));
    }

    /**
      * {@link MessageResult#error(com.spark.bitrade.enums.MessageCode)}
      * @author yangch
      * @time 2019.03.05 14:32 
      */
    @Test
    public void error3() throws Exception {
        MessageResult result = MessageResult.error(MessageCode.INVALID_AUTH_TOKEN);

        assertThat(result.getCode(), is(3011));
        assertThat(result.isSuccess(), is(false));
        assertThat(result.getMessage(), is("INVALID_AUTH_TOKEN"));
    }

    @Test
    public void getSuccessInstance() throws Exception {
    }

}