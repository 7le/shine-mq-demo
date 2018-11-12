package top.arkstack.shine.mq.demo.demo;


import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.bean.SendTypeEnum;
import top.arkstack.shine.mq.processor.BaseProcessor;

import javax.annotation.PostConstruct;

@Component
public class Test {

    @Autowired
    RabbitmqFactory factory;

    @Autowired
    DefaultDistributedTran defaultDistributedTran;

    @Autowired
    DistributedTran distributedTran;

    @PostConstruct
    public void test() throws Exception {

        factory.addDLX("distributed_transaction_exchange", "distributed_transaction_exchange",
                "distributed_transaction_routekey", new ProcessorTest(), SendTypeEnum.DISTRIBUTED);
        for (int i = 0; i < 10; i++) {
            defaultDistributedTran.transaction();
            distributedTran.transaction();
        }
    }

    static class ProcessorTest extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            System.out.println("distributed transaction process: " + msg);
            //分布式事务消息默认自动回执
            return null;
        }
    }
}
