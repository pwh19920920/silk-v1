package com.spark.bitrade.envent.uc.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.EnventHandlerUtil;
import com.spark.bitrade.envent.uc.IChangePhoneEmailEnventHandler;
import com.spark.bitrade.envent.uc.ILoginEnventHandler;
import com.spark.bitrade.envent.uc.ILogoutEnventHandler;
import com.spark.bitrade.envent.uc.IUcEnventDispatch;
import org.springframework.stereotype.Service;

/***
 * 用户相关操作事件分发处理实现类
 * @author yangch
 * @time 2018.11.02 15:40
 */
@Service
public class UcEnventDispatchImpl implements IUcEnventDispatch {

    @Override
    public void loginEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ILoginEnventHandler.class, carrier);
    }

    @Override
    public void logoutEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ILogoutEnventHandler.class, carrier);
    }

    @Override
    public void changePhoneEmail(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IChangePhoneEmailEnventHandler.class, carrier);
    }
}
