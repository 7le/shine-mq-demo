package top.arkstack.shine.mq.demo;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.bean.SendTypeEnum;
import top.arkstack.shine.mq.processor.BaseProcessor;

import javax.annotation.PostConstruct;

@Component
public class Consumer {

    @Autowired
    RabbitmqFactory factory;

    @PostConstruct
    public void test() {
        //服务B 配置消费者
        factory.add("distributed_transaction_exchange", "distributed_transaction_exchange",
                "distributed_transaction_routekey", new ProcessorTest(), SendTypeEnum.DISTRIBUTED);
        factory.add("dis_test", "dis_test",
                "dis_test_key", new ProcessorTest2(), SendTypeEnum.DISTRIBUTED);
    }

    /**
     * 服务B
     */
    static class ProcessorTest extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            //执行服务B的任务
            System.out.println("No1 distributed transaction process: " + msg);
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
}
