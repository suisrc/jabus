package com.suisrc.kratos.jabus.core;

import java.util.List;

public interface ExternalSubscribeHandler {

  /**
   * 获取订阅的对象
   * @return
   */
  List<Object> getSubscribers();
    
}
