package top.arkstack.shine.mq.demo;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.bean.SendTypeEnum;
import top.arkstack.shine.mq.constant.MqConstant;
import top.arkstack.shine.mq.processor.BaseProcessor;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class Consumer {

    @Autowired
    RabbitmqFactory factory;

    @PostConstruct
    public void test() {
        //服务B 配置消费者
        factory.addDLX("distributed_transaction_exchange", "distributed_transaction_exchange",
                "distributed_transaction_routekey", new ProcessorTest(), SendTypeEnum.DISTRIBUTED);

        factory.addDLX("dis_test", "dis_test",
                "dis_test_key", new ProcessorTest2(), SendTypeEnum.DISTRIBUTED);

        //配置死信队列 失败时候处理
        factory.add(MqConstant.DEAD_LETTER_QUEUE, MqConstant.DEAD_LETTER_EXCHANGE,
                MqConstant.DEAD_LETTER_ROUTEKEY, new ProcessorException(),SendTypeEnum.DLX);
    }

    /**
     * 服务B
     */
    static class ProcessorTest extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            //执行服务B的任务
            System.out.println("No1 distributed transaction process: " + msg);
            int a = 1 / 0;
            //分布式事务消息默认自动回执
            return null;
        }
    }

    /**
     * 服务B
     */
    static class ProcessorTest2 extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            //执行服务B的任务
            System.out.println("No2 distributed transaction process: " + msg);
            //分布式事务消息默认自动回执
            return null;
        }
    }

    /**
     * 处理异常
     */
    static class ProcessorException extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            //执行失败的任务，可以自行实现 通知人工处理 或者回调原服务A的回滚接口
            System.out.println("自行实现 通知人工处理 或者回调原服务A的回滚接口：" + msg);
            return null;
        }
    }
}
