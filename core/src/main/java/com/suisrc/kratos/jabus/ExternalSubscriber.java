package com.suisrc.kratos.jabus;

import com.suisrc.kratos.jabus.annotation.ExternalSubscribe.SubscribeType;

public interface ExternalSubscriber {

    /**
     * 控制事件总线的名称
     * 
     * @return
     */
    default String getTopic() {
        return null;
    }

    /**
     * 是否使用同步事件总线，该内容谨慎或不推荐使用
     * 
     * 除非特殊情况，否者严禁使用同步事件总线
     * 
     * @return
     */
    default SubscribeType getSubscribeType() {
        return SubscribeType.ASYNC;
    }

}
