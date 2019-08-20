package top.arkstack.shine.mq.demo.simple.processor;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.processor.BaseProcessor;

import java.util.Objects;

/**
 * 服务B 执行分布式事务（route）
 *
 * @author 7le
 */
@Slf4j
@Component
public class ProcessorTest extends BaseProcessor {

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        //执行服务B的任务  这里可以将msg转成TransferBean
        if (!Objects.isNull(msg)) {
            TransferBean bean = JSONObject.parseObject(msg.toString(), TransferBean.class);
            //这里就可以处理服务B的任务了
            log.info("(Simple_Route_config) Process task B : {}", bean.getData());
            log.info("(Simple_Route_config) CheckBackId : {}", bean.getCheckBackId());
        }
        //分布式事务消息默认自动回执
        return null;
    }
}
