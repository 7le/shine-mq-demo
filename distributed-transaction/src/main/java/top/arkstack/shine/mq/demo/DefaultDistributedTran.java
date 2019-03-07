package top.arkstack.shine.mq.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.annotation.DistributedTrans;
import top.arkstack.shine.mq.bean.TransferBean;
import top.arkstack.shine.mq.coordinator.Coordinator;

/**
 * 分布式事务demo 使用默认配置
 *
 * @author 7le
 */
@Component
public class DefaultDistributedTran {

    @Autowired
    private Coordinator coordinator;

    /**
     * 服务A 的任务
     */
    @DistributedTrans
    public TransferBean transaction(){
        //设置回查id 需要唯一 以防出现错误
        String checkBackId="987654321";
        coordinator.setPrepare(checkBackId);
        return new TransferBean(checkBackId,"所需要传输的数据");
    }
}