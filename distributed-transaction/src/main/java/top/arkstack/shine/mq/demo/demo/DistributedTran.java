package top.arkstack.shine.mq.demo.demo;

import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.annotation.DistributedTrans;

/**
 * 分布式事务demo 自行配置对应参数
 *
 * @author 7le
 */
@Component
public class DistributedTran {

    @DistributedTrans(exchange = "dis_test", routeKey = "dis_test", bizId = "ccc", coordinator = "redisCoordinator")
    public String transaction() {
        System.out.println("DistributedTran");
        return "DistributedTran";
    }
}
