# 说明

基于监听内容的回调

https://github.com/suisrc/jabus.git

## 例子
```java
@Component
public class TestNats implements ExternalSubscriber {
    
    @ExternalSubscribe(topic = "test")
    public void test1(String str){
        System.out.println("test1" + str);
    }

    @ExternalSubscribe(topic = "test",type = SubscribeType.SYNC)
    public String test2(String str){
        return str.replace("id", "newChar");
    }

    @ExternalSubscribe(topic = "test", queue = "q123")
    public void test3(String str){
        System.out.println("test3" + str);
    }

    @ExternalSubscribe(topic = "test", queue = "q123")
    public void test4(String str){
        System.out.println("test4" + str);
    }
}
```
```java
@Controller
public class DemoApi {

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

```