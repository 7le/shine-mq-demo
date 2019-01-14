package top.arkstack.shine.mq.demo;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
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

    @PostConstruct
    public void test() throws Exception {
        factory.add("shine-queue-1", "shine-exchange-1", "shine-1",
                new ProcessorTest(),null);
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
