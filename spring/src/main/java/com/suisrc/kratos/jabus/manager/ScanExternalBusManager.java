package com.suisrc.kratos.jabus.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.suisrc.kratos.jabus.ExternalBus;
import com.suisrc.kratos.jabus.annotation.ESConstant;
import com.suisrc.kratos.jabus.core.ExternalSubscribeHandler;
import com.suisrc.kratos.jabus.core.ExternalSubscriber;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.spel.EvaluationContextProvider;
import org.springframework.data.spel.ExtensionAwareEvaluationContextProvider;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 加载所有总线的订阅
 * 
 * @see ExternalSubscriber
 * @see ExternalSubscribeHandler
 */
@Configuration
public class ScanExternalBusManager extends AbstractBusManager implements ApplicationContextAware {
    public static final String PREFIX = "jabus.spring";

    private SpelExpressionParser parser = new SpelExpressionParser();
    private EvaluationContextProvider provider = EvaluationContextProvider.DEFAULT;

    private String desn;
    private final Environment environment;
    private final boolean enabled;

    private ExternalBus delegate;
    private ApplicationContext context;

    @Autowired
    public ScanExternalBusManager(Environment env) {
        environment = env; // jabus.spring.topics.%s
        desn = env.getProperty(PREFIX + ".destination", ESConstant.DESN);
        enabled = env.getProperty(PREFIX + ".enabled", Boolean.class, true);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!enabled) {
            return; // 禁用状态， 不执行初始化操作
        }
        this.context = applicationContext;
        this.provider = new ExtensionAwareEvaluationContextProvider(applicationContext);
        this.delegate = context.getBean(ExternalBus.class);
        if (this.delegate instanceof ExternalBusManagerAware) {
            ((ExternalBusManagerAware)this.delegate).setExternalBusManager(this); // 监听器捆绑
        }
        load(); // 加载所有的外部订阅内容
    }

    /**
     * 获取外部总线控制器
     */
    @Override
    public ExternalBus getExternalBus() {
        return delegate;
    }

    /**
     * 加载应用中的所有外部总线内容
     */
    @Override
    public List<Object> getSubscribers() {
        Set<Object> subscribers = new HashSet<>();

        Map<String, ExternalSubscriber> sub1 = context.getBeansOfType(ExternalSubscriber.class);
        subscribers.addAll(sub1.values());
        
        Map<String, ExternalSubscribeHandler> sub2 = context.getBeansOfType(ExternalSubscribeHandler.class);
        for ( ExternalSubscribeHandler sh : sub2.values()) {
            subscribers.addAll(sh.getSubscribers());
        }

        return new ArrayList<>(subscribers);
    }

    //=========================================================================================================

    @Override
    public String spel(String method, String topic, String str) {
        if (str.startsWith("$")) {
            str = getSpelByMethod(method, str);         // method
            str = getSpelByMarker(method, topic, str);  // marker
    
            if (str.startsWith("${") && str.endsWith("}")) {
                return getEnvProperty(str);
            }
        }
        if (!str.contains(ParserContext.TEMPLATE_EXPRESSION.getExpressionPrefix())) {
            return str; // 不包含spel语法
        }
        Expression expression = parser.parseExpression(str, ParserContext.TEMPLATE_EXPRESSION);
        return expression.getValue(provider.getEvaluationContext(null), String.class);
    }

    private String getSpelByMarker(String method, String topic, String str) {
        if (str.endsWith(ESConstant.SUFK)) { // '?}'
            str = str.substring(0, str.length() - ESConstant.SUFK.length()) + desn;
            // '${#xxxx?}' => '${#xxxx.destination}'
        } else if (str.equals(ESConstant.PK_GG) && topic.startsWith(ESConstant.PK_IN0)) { // '$$' & '$<'
            String topic0 = getSpelByMethod(method, topic); // 获取主题编码
            str = String.format("${#%s-in-0.group}", topic0.substring(ESConstant.PK_IN0.length()));
            // '$$' => '${#xxxx-in-0.group}'
        } else if (str.startsWith(ESConstant.PK_IN0)) { // '$<'
            str = String.format("${#%s-in-0%s", str.substring(ESConstant.PK_IN0.length()), desn);
            // '$<xxxx' => '${#xxxx-in-0.destination}'
        } else if (str.startsWith(ESConstant.PK_OUT0)) { // '$>'
            str = String.format("${#%s-out-0%s", str.substring(ESConstant.PK_OUT0.length()), desn);
            // '$>xxxx' => '${#xxxx-out-0.destination}'
        }
        return str;
    }

    private String getSpelByMethod(String method, String str) {
        if (str.equals(ESConstant.PK_GG)) {
            return str;
        }
        for (String tag : new String[]{"${##", ESConstant.PK_IN0 + "#", ESConstant.PK_OUT0 + "#"}) {
            if (str.startsWith(tag)) {
                str = tag.substring(0, tag.length() - 1) + method + str.substring(tag.length());
                break;
            } 
        }
        return str;
    }

    //=========================================================================================================

    /**
     * 通过系统环境变量获取
     * @param str
     * @return
     */
    protected String getEnvProperty(String str) {
        String key2 = str.substring(2, str.length() - 1);
        int idx = key2.indexOf(':');
        String key;
        String def;
        if (idx > 0) {
            key = key2.substring(0, idx);
            def = key2.substring(idx+1);
        } else {
            key = key2;
            def = "";
        }
        if (key.startsWith("#")) {
            key = String.format("%s.topics.%s", PREFIX, key.substring(1));
        }
        return environment.getProperty(key, def);
    }
// jabus.spring.topics:
//   task-create-0:
//     destination: com.suisrc.kratos.msg.task-create-00
//     group: task-create-00
}
