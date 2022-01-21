package com.myfmes.github.fwk.web;

import com.suisrc.kratos.jabus.annotation.ExternalSubscribe;
import com.suisrc.kratos.jabus.annotation.ExternalSubscribe.SubscribeType;
import com.suisrc.kratos.jabus.core.ExternalSubscriber;

import org.springframework.stereotype.Component;

@Component
public class TestNats implements ExternalSubscriber {
    
    @ExternalSubscribe(topic = "test")
    public void test1(String str){
        System.out.println("test1 > " + str);
        throw new RuntimeException("test1");
    }

    @ExternalSubscribe(topic = "test",type = SubscribeType.SYNC)
    public String test2(String str){
        return str.replace("id", "newChar");
    }

    @ExternalSubscribe(topic = "test", queue = "q123")
    public void test3(String str){
        System.out.println("test3  > " + str);
    }

    @ExternalSubscribe(topic = "test", queue = "q123")
    public void test4(String str){
        System.out.println("test4  > " + str);
    }
}
