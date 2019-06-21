package top.arkstack.shine.mq.demo.simple.init;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.demo.simple.util.SnowflakeIdGenerator;

/**
 * spring 容器加载完毕后调用
 *
 * @author 7le
 * @version 1.0.0
 */
@Component
public class ApplicationInit implements ApplicationContextAware {


    /**
     * 初始化SnowflakeIdGenerator
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //如果是集群下，可以用分布式锁保证各节点workId自增唯一
        Long workId = 1L;
        SnowflakeIdGenerator.init(workId);
    }
}
