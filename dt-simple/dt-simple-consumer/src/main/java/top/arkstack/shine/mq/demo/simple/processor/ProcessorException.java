package top.arkstack.shine.mq.demo.simple.processor;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.processor.BaseProcessor;

/**
 * 监听死信队列 处理异常
 *
 * @author 7le
 */
@Slf4j
@Component
public class ProcessorException extends BaseProcessor {

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        //执行失败的任务，可以自行实现 通知人工处理 或者回调原服务A的回滚接口
        log.info("自行实现 通知人工处理 或者回调原服务A的回滚接口：" + msg);
        return null;
    }
}
