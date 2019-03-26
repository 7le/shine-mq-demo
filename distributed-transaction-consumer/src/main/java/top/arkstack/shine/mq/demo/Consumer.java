package top.arkstack.shine.mq.demo;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.bean.SendTypeEnum;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.constant.MqConstant;
import top.arkstack.shine.mq.processor.BaseProcessor;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * 服务B 消费者处理消息
 *
 * @author 7le
 * @version 1.0.0
 */
@Slf4j
@Component
public class Consumer {

    @Autowired
    RabbitmqFactory factory;

    @PostConstruct
    public void test() {
        //服务B 配置消费者
        factory.addDLX("route_config", "route_config",
                "route_config_key", new ProcessorTest(), SendTypeEnum.DISTRIBUTED);

        //配置死信队列 失败时候处理
        factory.add(MqConstant.DEAD_LETTER_QUEUE, MqConstant.DEAD_LETTER_EXCHANGE,
                MqConstant.DEAD_LETTER_ROUTEKEY, new ProcessorException(), SendTypeEnum.DLX);
    }

    /**
     * 服务B 执行分布式事务（route）
     */
    static class ProcessorTest extends BaseProcessor {

        @Override
        public Object process(Object msg, Message message, Channel channel) {
            //执行服务B的任务  这里可以将msg转成TransferBean
            if (!Objects.isNull(msg)) {
                TransferBean bean = JSONObject.parseObject(msg.toString(), TransferBean.class);
                //这里就可以处理服务B的任务了
                log.info("(Route_config) Process task B : {}", bean.getData());
                log.info("(Route_config) CheckBackId : {}", bean.getCheckBackId());
            }
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
            log.info("自行实现 通知人工处理 或者回调原服务A的回滚接口：" + msg);
            return null;
        }
    }
}
