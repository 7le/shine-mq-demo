package top.arkstack.shine.mq.demo.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.arkstack.shine.mq.annotation.DistributedTrans;
import top.arkstack.shine.mq.bean.PrepareMessage;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.coordinator.Coordinator;
import top.arkstack.shine.mq.demo.dao.RouteConfigMapper;
import top.arkstack.shine.mq.demo.dao.model.RouteConfig;
import top.arkstack.shine.mq.demo.util.SnowflakeIdGenerator;

/**
 * 分布式事务demo 自行配置对应参数
 *
 * @author 7le
 * @version 1.0.0
 */
@Component
public class DistributedTran {

    @Autowired
    private Coordinator coordinator;

    @Autowired
    private RouteConfigMapper mapper;

    /**
     * 服务A 的任务
     * <p>
     * coordinator 可以自行实现，或者使用默认提供的
     */
    @DistributedTrans(exchange = "route_config", routeKey = "route_config_key", bizId = "route_config",
            coordinator = "redisCoordinator", rollback = "route_config_rollback")
    @Transactional(rollbackFor = Exception.class)
    public TransferBean transaction() {
        //设置回查id 需要唯一 （可以用数据库的id） 以防出现错误，
        Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
        //prepare需要checkBackId（回查id）来查询服务A任务状态，bizId,exchangeName和routingKey是重发的必要信息
        coordinator.setPrepare(new PrepareMessage(checkBackId.toString(), "route_config",
                "route_config", "route_config_key"));

        //执行操作
        RouteConfig routeConfig = new RouteConfig(checkBackId, "/shine/**", "spring-mq",
                null, false, true, true, null);
        mapper.insert(routeConfig);
        //用来模拟任务A成功，但是没有投递到mq(就是测试prepare消息的补偿)
        //int i = 1 / 0;
        //需要用TransferBean包装下，checkBackId是必须的，data可以为null
        return new TransferBean(checkBackId.toString(), routeConfig.getPath());
    }
}
