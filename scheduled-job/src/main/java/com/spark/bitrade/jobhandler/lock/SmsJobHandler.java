package com.spark.bitrade.jobhandler.lock;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.SmsSendStatus;
import com.spark.bitrade.dto.LockCoinDetailVo;
import com.spark.bitrade.service.LockCoinDetailService;
import com.spark.bitrade.service.SmsSendService;
import com.spark.bitrade.util.ValidateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**业务：
 *      短信批量发送功能
 * @author lingxing
 * @time 2018.08.02 09:57
 */
@JobHandler(value="smsJobHandler")
@Component
public class SmsJobHandler extends IJobHandler {
    @Autowired
    LockCoinDetailService lockCoinDetailService;
    @Autowired
    SmsSendService smsSendService;
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //定时任务传的页数
        int size=Integer.parseInt(s);
        XxlJobLogger.log("======批量发送短信job, 发送短信======");
        //分页查询数据
        PageInfo<LockCoinDetailVo>coinDetailVoPageInfo=lockCoinDetailService.getUserPlanUnlockTimeAndSmsSendStatus(0, 0, size);
        if(coinDetailVoPageInfo.getList().size()!=0 && coinDetailVoPageInfo.getList()!=null){
            sendSmsAndUpdate(coinDetailVoPageInfo);
        }
        //查询数据条数是否跟传进来相等，和总分页是否大于1，满足条件再次调用方法
        if(coinDetailVoPageInfo.getList().size()==size && coinDetailVoPageInfo.getPages()>1){
            execute(s);
        }
        XxlJobLogger.log("======批量发送短信job, 结束======");
        return SUCCESS;
    }

    /**
     *
     * @param pageInfo
     */
    private void sendSmsAndUpdate(PageInfo<LockCoinDetailVo> pageInfo){
        //云片格式要求 1301225555，1300055333 采用StringBuffer
        StringBuffer buffer=new StringBuffer();
        for (int i = 0; i < pageInfo.getList().size(); i++) {
            //判断手机号码是否为空和校验合法性
            if(StringUtils.isNotEmpty(pageInfo.getList().get(i).getMobilePhone()) && ValidateUtil.isChinaPhoneLegal(pageInfo.getList().get(i).getMobilePhone())){
                //等于1的时候说明只要一条不需要追加,
                if(pageInfo.getList().size()==1){
                    buffer.append(pageInfo.getList().get(i).getMobilePhone());
                }else {
                    //存在多条数据追加,
                    buffer.append(pageInfo.getList().get(i).getMobilePhone()).append(",");
                }
                //设置发送成功
                pageInfo.getList().get(i).setSmsSendStatus(SmsSendStatus.ALREADY_SMS_SEND);
            }else{
                //号码出现问题的设置失败
                pageInfo.getList().get(i).setSmsSendStatus(SmsSendStatus.FAIL_SEND_SMS);
            }
        }
        //数据条数大于1，后面有个append追加的，去除，
        if(pageInfo.getList().size()>1){
            buffer.deleteCharAt(buffer.length() - 1);
        }
        if(buffer!=null){
            //发送短信将返回 i=0 成功 i=-1失败
            int i=smsSendService.batchSend("【SilkTrader】亲爱的用户，您有一笔SLB投资锁仓将于明天解锁，请提前在“活动中心”的SLB投资中选择结算方式，如果不选择系统将按USDT进行结算。", buffer.toString());
            //根据返回的状态修改值
            lockCoinDetailService.batchUpdate( pageInfo.getList(),i);
        }
    }
}
