package top.arkstack.shine.mq.demo.rollback;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.demo.dao.RouteConfigMapper;
import top.arkstack.shine.mq.processor.BaseProcessor;

/**
 * route_config的异常回滚
 *
 * @author 7le
 * @version 2.2.0
 */
@Slf4j
@Component
public class RouteConfigRollback extends BaseProcessor {

    @Autowired
    private RouteConfigMapper mapper;

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        log.info("route_config rollback :{}", msg);
        TransferBean bean = JSONObject.parseObject(msg.toString(), TransferBean.class);
        //rollback transaction
        mapper.deleteByPrimaryKey(Long.valueOf(bean.getCheckBackId()));
        try {
            //手动回执ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }
}
