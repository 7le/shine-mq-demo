package top.arkstack.shine.mq.demo.simple.daemon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.bean.EventMessage;
import top.arkstack.shine.mq.coordinator.Coordinator;
import top.arkstack.shine.mq.coordinator.redis.RedisUtil;
import top.arkstack.shine.mq.demo.simple.dao.RouteConfigMapper;

import java.util.List;
import java.util.Objects;

/**
 * 守护线程
 * 这里的demo简单的将守护线程做到服务A中（使用定时任务）。
 * 也可以单独做一个服务或者其他方式。
 *
 * @author 7le
 * @version 1.0.0
 */
@Slf4j
@Component
public class Daemon {

    @Autowired
    private Coordinator coordinator;

    @Autowired
    private RouteConfigMapper mapper;

    @Autowired
    private RedisUtil redisUtil;

    @Scheduled(initialDelay = 5_000, fixedRate = 30_000)
    public void process() {
        try {
            //处理ready消息
            redisUtil.lock("redis_lock_ready", 90_000L, () -> {
                List<EventMessage> ready = coordinator.getReady();
                if (!Objects.isNull(ready) && ready.size() > 0) {
                    ready.forEach(r -> {
                        //超时的ready的消息，就直接捞起发送到消息中间件，因为只要是ready消息持久化到协调者，那就说明服务A的任务已经完成。
                        //因为消息到mq是异步通知的，所以补偿的频率过高会造成消息重复，下游服务最好能保证幂等性
                        try {
                            coordinator.compensateReady(r);
                            log.info("重新投递消息： {}", r);
                        } catch (Exception e) {
                            log.error("Message failed to be sent : ", e);
                        }
                    });
                }
                return null;
            });
        } catch (Exception e) {
            log.error("daemon process error: ", e);
        }
    }
}
