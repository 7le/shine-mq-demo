package top.arkstack.shine.mq.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;

import javax.annotation.PostConstruct;

/**
 * 生产者
 *
 * @author 7le
 */
@Component
public class Producer {

    @Autowired
    RabbitmqFactory factory;


    @PostConstruct
    public void test() throws Exception {
        factory.add("shine-queue-1", "shine-exchange-1", "shine-1",
                null,null);
        for (int i = 0; i < 10; i++) {
            factory.getTemplate().send("shine-exchange-1", "shine-1 " + i, "shine-1");
        }
    }
}
