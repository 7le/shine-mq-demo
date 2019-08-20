package top.arkstack.shine.mq.demo.processor;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.processor.BaseProcessor;

/**
 * 消息处理器
 *
 * @author 7le
 */
@Slf4j
@Component
public class ProcessorTest extends BaseProcessor {

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
