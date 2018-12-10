package top.arkstack.shine.mq.demo;

import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.annotation.DistributedTrans;

/**
 * 分布式事务demo 使用默认配置
 *
 * @author 7le
 */
@Component
public class DefaultDistributedTran {

    /**
     * 服务A 的任务
     */
    @DistributedTrans
    public String transaction(){
        System.out.println("DefaultDistributedTran");
        return "DefaultDistributedTran";
    }
}