package com.myfmes.github.fwk.web;

import java.util.function.Consumer;
import java.util.function.Function;

import com.suisrc.kratos.jabus.ExternalBus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author <a href="mailto:suisrc@outlook.com">Y13</a>
 */
@Controller
public class DemoApi {

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello, world!";
    }

    @Autowired ExternalBus bus;


    @GetMapping("/test2")
    @ResponseBody
    public String test() {
        return bus.request("test", "id: 333", String.class).orElse("none");
    }

    @GetMapping("/test3")
    @ResponseBody
    public String test3() {
        bus.subscribeAsync("test", consumer);
        return "test3";
    }


    @GetMapping("/test4")
    @ResponseBody
    public String test4() {
        bus.unsubscribe("test", consumer);
        return "test4";
    }

    @GetMapping("/test5")
    @ResponseBody
    public String test5() {
        bus.subscribe("test", function);
        return "test3";
    }


    @GetMapping("/test6")
    @ResponseBody
    public String test6() {
        bus.unsubscribe("test", function);
        return "test4";
    }

    Consumer<String> consumer = message -> {
        System.out.println("DemoAPI:" + message);
    };

    Function<String, String> function = message -> {
        return message + " hello2";
    };
}

