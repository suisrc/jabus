package com.suisrc.kratos.jabus.manager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Level;

import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe.SubscribeType;
import com.suisrc.kratos.jabus.core.ExternalSub;

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

  public String spel(Method method, String str) {
    if (str.startsWith("$>#")) {
      str = "$>" + method.getName() + str.substring(3);
    } else if (str.startsWith("${##")) {
      str = "${#" + method.getName() + str.substring(4);
    } 
    return spel(str);
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

    Method[] methods = obj.getClass().getMethods();
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : methods) {
      methodMap.put(method.getName(), method);
    }

    for (Method method : methods) {
      if (subscribe(methodMap, obj, method)) {
        count++;
      }
    }
    return count;
  }

  protected boolean subscribe(Map<String, Method> methodMap, Object obj, Method method) {
    ExternalSubscribe subscribe = method.getAnnotation(ExternalSubscribe.class);
    if (subscribe == null) {
      return false;
    }
    Method finallyMethod = null; // 清理回调方法
      if (!subscribe.finallyMethod().isEmpty()) {
        finallyMethod = methodMap.get(subscribe.finallyMethod());
        if (finallyMethod == null) {
          throw new RuntimeException(String.format("external subscribe method error, clear method not found: %s::%s", //
            obj.getClass().getSimpleName(), method.getName()));
        }
      }

      String topic0 = subscribe.topic();
      String queue0 = subscribe.queue();
      if (queue0.startsWith("${#$") && queue0.endsWith("}")) {
        // ${#$destination:group}, topic中destination替换成group， 特殊处理
        String[] keys = queue0.substring(4, queue0.length() - 1).split(":", 2);
        if (keys.length != 2) {
          throw new RuntimeException(String.format("external subscribe method error, queue config err: %s::%s", //
            obj.getClass().getSimpleName(), method.getName()));
        }
        queue0 = topic0.replace(keys[0], keys[1]);
      }

      boolean result = false;
      String topic1 = spel(method, topic0);
      String queue1 = "";
      if (isEmpty(topic1)) {
        topic1 = "未配置";
      } else {
        queue1 = spel(method, queue0);
        SubscribeType stype1 = subscribe.type();
  
        ExternalBus delegate = getExternalBus();
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
      }
      log.log(Level.INFO, "加载外部订阅器{4}：主题[{0}], 方法[{2}::{3}], 队列[{1}]", new Object[]{ //
        topic1, queue1, obj.getClass().getName(), method.getName(), result ? "成功" : "失败"});

      return result;
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

    ExternalBus delegate = getExternalBus();
    for (Method method : obj.getClass().getMethods()) {

      ExternalSubscribe subscribe = method.getAnnotation(ExternalSubscribe.class);
      if (subscribe != null) {

        String topic0 = subscribe.topic();
        String topic1 = spel(method, topic0);
        ExternalSub external = new ExternalSub(obj, method, null);
        if (delegate.unsubscribe(topic1, external)) {
          count++;
        }
      }
    }
    return count;
  }

}
