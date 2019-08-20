package top.arkstack.shine.mq.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.demo.processor.ProcessorTest;

import javax.annotation.PostConstruct;

@Component
public class Test {

    @Autowired
    RabbitmqFactory factory;

    @Autowired
    private ProcessorTest processorTest;


    @PostConstruct
    public void test() throws Exception {
        factory.add("shine-queue", "shine-exchange", "shine",
                processorTest);
        for (int i = 0; i < 10; i++) {
            factory.getTemplate().send("shine-exchange", "shine " + i, "shine");
        }
    }
}
