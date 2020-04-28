package com.spark.bitrade.envent.uc;

import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.IEnventDispatch;

/***
  * 用户相关操作事件分发接口
  * @author yangch
  * @time 2018.11.02 13:57
  */
public interface IUcEnventDispatch extends IEnventDispatch {
    /**
     * 事件分发
     *
     * @param carrier
     */
    default void dispatch(CollectCarrier carrier) {
        CollectActionEventType key = carrier.getCollectType();
        switch (key) {
            case LOGIN:
                loginEnvent(carrier);
                break;
            case LOGOUT:
                logoutEnvent(carrier);
                break;
            case CHANGE_PHONE_EMAIL:
                changePhoneEmail(carrier);
                break;

        }
    }

    void loginEnvent(CollectCarrier carrier);

    void logoutEnvent(CollectCarrier carrier);

    void changePhoneEmail(CollectCarrier carrier);
}
