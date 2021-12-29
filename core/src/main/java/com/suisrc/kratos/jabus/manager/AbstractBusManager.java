package com.suisrc.kratos.jabus.manager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Level;

import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.ExternalSub;
import com.suisrc.kratos.jabus.ExternalSubscriber;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe.SubscribeType;

import lombok.extern.java.Log;

/**
 * 事件总线管理器, 默认实现
 * 
 * @author Y13
 *
 */
@Log
public abstract class AbstractBusManager implements ExternalBusManager {

  /**
   * 是否加载
   */
  private boolean isLoaded;

  /**
   * 
   */
  //@Override
  public abstract ExternalBus getExternalBus();

  /**
   * @see Subscriber
   * @see SubscribeHandler
   * @return
   */
  public abstract List<Object> getSubscribers();

  //  Spring Expression Language，SpEL
  @Override
  public String spel(String str) {
    return str;
  }

  /**
   * 
   */
  @Override
  public void load() {
    if (isLoaded) {
      throw new IllegalArgumentException(getClass() + " 已经执行加载load，不可重置执行");
    }
    isLoaded = true;

    List<Object> subscribes = getSubscribers();
    if (subscribes == null || subscribes.isEmpty()) {
      log.info("无外部消息订阅服务");
      return;
    }
    // 执行订阅
    int count = 0;
    for (Object subscribe : subscribes) {
      count += this.subscribe(subscribe);
    }
    // subscribes.forEach(this::subscribe)
    log.info("统计实际加载外部消息订阅服务数量：" + count);
  }

  // ====================================================================================================
  // ====================================================================================================
  // ====================================================================================================

  protected boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * 
   */
  @Override
  public int subscribe(Object obj) {
    if (obj == null) {
      return 0;
    }
    int count = 0;

    String topic0 = "default";
    SubscribeType stype0 = SubscribeType.ASYNC;

    if (obj instanceof ExternalSubscriber) {
      ExternalSubscriber subscribe = (ExternalSubscriber) obj;

      if (!isEmpty(subscribe.getTopic())) {
        topic0 = subscribe.getTopic();
      }
      if (subscribe.getSubscribeType() != SubscribeType.NONE) {
        stype0 = subscribe.getSubscribeType();
      }
    }

    Method[] methods = obj.getClass().getMethods();
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : methods) {
      methodMap.put(method.getName(), method);
    }

    ExternalBus delegate = getExternalBus();
    for (Method method : methods) {
      ExternalSubscribe subscribe = method.getAnnotation(ExternalSubscribe.class);
      if (subscribe == null) {
        continue;
      }
      Method finallyMethod = null; // 清理回调方法
      if (!subscribe.finallyMethod().isEmpty()) {
        finallyMethod = methodMap.get(subscribe.finallyMethod());
        if (finallyMethod == null) {
          throw new RuntimeException(String.format("external subscribe method error, clear method not found: %s::%s", //
            obj.getClass().getSimpleName(), method.getName()));
        }
      }

      String topic1 = !isEmpty(subscribe.topic()) ? subscribe.topic() : topic0;
      SubscribeType stype1 = subscribe.type() != SubscribeType.NONE ? subscribe.type() : stype0;
      String queue1 = subscribe.queue();
      //if ("${##".equals(queue1)) // 增加特殊内容

      queue1 = spel(queue1);
      topic1 = spel(topic1);
      boolean result = false;
      ExternalSub external = new ExternalSub(obj, method, finallyMethod);
      switch (stype1) {
        case SYNC:
          result = isEmpty(queue1) ? //
            delegate.subscribe(topic1, external) : //
            delegate.subscribe(topic1, queue1, external);
          break;
        case ASYNC:
          result = isEmpty(queue1) ? // 
            delegate.subscribeAsync(topic1, external) : //
            delegate.subscribeAsync(topic1, queue1, external);
          break;
        default:
          break;
      }
      if (result) {
        count++;
        log.log(Level.INFO, "已经加载外部订阅器：主题[{0}], 方法[{1}::{2}]", //
          new Object[]{topic1, method.getDeclaringClass().getName(),  method.getName()});
      }
    }
    return count;
  }

  /**
   * 
   */
  @Override
  public int unsubscribe(Object obj) {
    if (obj == null) {
      return 0;
    }
    int count = 0;

    String topic0 = "default";
    if (obj instanceof ExternalSubscriber) {

      ExternalSubscriber subscribe = (ExternalSubscriber) obj;
      topic0 = isEmpty(subscribe.getTopic()) ? subscribe.getTopic() : topic0;
    }

    ExternalBus delegate = getExternalBus();
    for (Method method : obj.getClass().getMethods()) {

      ExternalSubscribe subscribe = method.getAnnotation(ExternalSubscribe.class);
      if (subscribe != null) {

        String topic1 = isEmpty(subscribe.topic()) ? subscribe.topic() : topic0;
        ExternalSub external = new ExternalSub(obj, method, null);
        if (delegate.unsubscribe(topic1, external)) {
          count++;
        }
      }
    }
    return count;
  }

}
