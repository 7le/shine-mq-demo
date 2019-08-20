package top.arkstack.shine.mq.demo;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.demo.processor.ProcessorTest;
import top.arkstack.shine.mq.processor.BaseProcessor;

import javax.annotation.PostConstruct;

/**
 * 消费者
 *
 * @author 7le
 */
@Component
public class Consumer {

    @Autowired
    RabbitmqFactory factory;

    @Autowired
    private ProcessorTest processorTest;

    @PostConstruct
    public void test() throws Exception {
        factory.add("shine-queue-1", "shine-exchange-1", "shine-1",
                processorTest,null);
    }
}
