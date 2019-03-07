package top.arkstack.shine.mq.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.annotation.DistributedTrans;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.coordinator.Coordinator;

/**
 * 分布式事务demo 自行配置对应参数
 *
 * @author 7le
 */
@Component
public class DistributedTran {

    @Autowired
    private Coordinator coordinator;

    /**
     * 服务A 的任务
     */
    @DistributedTrans(exchange = "dis_test", routeKey = "dis_test_key", bizId = "ccc", coordinator = "redisCoordinator")
    public TransferBean transaction() {
        //设置回查id 需要唯一 以防出现错误
        String checkBackId="123456789";
        coordinator.setPrepare(checkBackId);
        //需要用TransferBean包装下，checkBackId是必须的，data可以为null
        return new TransferBean(checkBackId,"所需要传输的数据");
    }
}
