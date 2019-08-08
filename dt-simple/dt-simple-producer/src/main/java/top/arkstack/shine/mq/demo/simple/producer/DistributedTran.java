package top.arkstack.shine.mq.demo.simple.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.arkstack.shine.mq.annotation.DistributedTrans;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.coordinator.Coordinator;
import top.arkstack.shine.mq.demo.simple.dao.RouteConfigMapper;
import top.arkstack.shine.mq.demo.simple.dao.model.RouteConfig;
import top.arkstack.shine.mq.demo.simple.util.SnowflakeIdGenerator;

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
     * 注解@DistributedTrans可以和@Transactional共用
     */
    @DistributedTrans(exchange = "simple_route_config", routeKey = "simple_route_config_key", bizId = "simple_route_config")
    @Transactional(rollbackFor = Exception.class)
    public TransferBean transaction() {
        //simple 不校验服务A的状态 可以不设置Prepare状态
        Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
        //执行操作
        RouteConfig routeConfig = new RouteConfig(checkBackId,
                "/shine/simple/**", "spring-mq-simple", null, false, true,
                true, null);
        mapper.insert(routeConfig);
        return new TransferBean(checkBackId.toString(), routeConfig.getPath());
    }
}
