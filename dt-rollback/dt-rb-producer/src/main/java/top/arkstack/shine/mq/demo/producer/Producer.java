package top.arkstack.shine.mq.demo.producer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.bean.SendTypeEnum;
import top.arkstack.shine.mq.demo.rollback.RouteConfigRollback;

import javax.annotation.PostConstruct;

/**
 * @author 7le
 * @version 1.0.0
 */
@Component
public class Producer {

    @Autowired
    RabbitmqFactory factory;

    @Autowired
    DistributedTran distributedTran;

    @Autowired
    private RouteConfigRollback rollback;

    @PostConstruct
    public void test() throws Exception {
        //服务A 执行任务
        for (int i = 0; i < 1; i++) {
            distributedTran.transaction();
        }
        //增加对回滚队列的监听
        factory.add("route_config_rollback", "route_config",
                "route_config_rollback", rollback, SendTypeEnum.ROLLBACK);
    }
}
