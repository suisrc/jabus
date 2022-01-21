package com.suisrc.kratos.jabus.manager;

public interface ExternalBusManager {
    
    /**
     * 字符内容转换
     * @param str
     * @return
     */
    String spel(String method, String topic, String str);

    /**
     * 加载订阅
     */
    void load();

    /**
     * 
     * @param obj
     * @return
     */
    int subscribe(Object obj);

    /**
     * 
     * @param obj
     * @return
     */
    int unsubscribe(Object obj);
}