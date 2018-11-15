package top.arkstack.shine.mq.demo;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.processor.BaseProcessor;

import javax.annotation.PostConstruct;

@Component
public class Test {

    @Autowired
    RabbitmqFactory factory;

    @PostConstruct
    public void test() throws Exception {
        factory.add("shine-queue", "shine-exchange", "shine",
                new ProcessorTest(), null);
        for (int i = 0; i < 10; i++) {
            factory.getTemplate().send("shine-exchange", "shine " + i, "shine");
        }
    }

    static class ProcessorTest extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            System.out.println("shine queue process: " + msg);
            try {
                //如果选择了MANUAL模式 需要手动回执ack
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }
}
