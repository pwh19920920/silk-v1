package com.spark.bitrade.envent.otc;

import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.IEnventDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
  * C2C事件分发接口
  * @author yangch
  * @time 2018.11.02 13:57
  */
public interface IOtcEnventDispatch extends IEnventDispatch {
    Logger log = LoggerFactory.getLogger(IOtcEnventDispatch.class);

    default void dispatch(CollectCarrier carrier) {
        log.info("消费进入IOtcEnventDispatch-------------------------2--1--");
        CollectActionEventType key = carrier.getCollectType();
        switch (key) {
            case OTC_ADD_ORDER:
                addOrderEnvent(carrier);
                break;
            case OTC_APPEAL_ORDER:
                appealOrderEnvent(carrier);
                break;
            case OTC_CANCEL_ORDER:
                cancelOrderEnvent(carrier);
                break;
            case OTC_PAY_CASH:
                payCashEnvent(carrier);
                break;
            case OTC_PAY_COIN:
                payCoinEnvent(carrier);
                break;
            case EXPIRE_REMIND_ORDER:
                expireRemindOrder(carrier);
                break;
            case OTC_APPEAL_ORDER_COMPLETE:
                appealOrderFinished(carrier);
                break;
            default:
                log.info("消费进入IOtcEnventDispatch，为找到相关key-------------------------2--2--");
        }
    }

    void addOrderEnvent(CollectCarrier carrier);

    void appealOrderEnvent(CollectCarrier carrier);

    void cancelOrderEnvent(CollectCarrier carrier);

    void payCashEnvent(CollectCarrier carrier);

    void payCoinEnvent(CollectCarrier carrier);

    void expireRemindOrder(CollectCarrier carrier);

    void appealOrderFinished(CollectCarrier carrier);
}
