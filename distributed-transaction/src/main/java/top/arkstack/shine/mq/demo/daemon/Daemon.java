package top.arkstack.shine.mq.demo.daemon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.arkstack.shine.mq.RabbitmqFactory;
import top.arkstack.shine.mq.bean.EventMessage;
import top.arkstack.shine.mq.coordinator.Coordinator;
import top.arkstack.shine.mq.demo.dao.RouteConfigMapper;
import top.arkstack.shine.mq.demo.dao.model.RouteConfig;

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
    private RabbitmqFactory rabbitmqFactory;

    private static String coordinatorName = "redisCoordinator";

    @Scheduled(initialDelay = 5_000, fixedRate = 5_000)
    public void process() {
        try {
            //处理Prepare消息
            List prepare = coordinator.getPrepare();
            if (!Objects.isNull(prepare) && prepare.size() > 0) {
                prepare.forEach(p -> {
                    log.info("CheckBackId :{}", prepare);
                    //这里是用数据库id为回查id，这样就可以根据是否有这条记录来判断服务A的任务是否已经完成
                    //可以根据自己的业务场景采用其他方式，比如用缓存来缓存状态之类的
                    RouteConfig config = mapper.selectByPrimaryKey(Long.valueOf(p.toString()));
                    if (Objects.isNull(config)) {
                        log.info("服务A中任务并没有完成，id:{}", prepare);
                        //因为服务A的任务没有完成，所以这次操作就是失败了，可以记录下日志，这时候数据是一致的
                    } else {
                        log.info("服务A中任务已经完成，id:{}", prepare);
                        //服务A的任务已经完成，但是prepare消息没被删除，说明投递到mq失败了，那就继续进行投递或者将任务回滚
                        //TODO shine-mq 待设计提供一个prepare方法
                    }
                });
            }
            //处理ready消息
            List ready = coordinator.getReady();
            if (!Objects.isNull(ready) && ready.size() > 0) {
                ready.forEach(r -> {
                    //超时的ready的消息，就直接捞起发送到消息中间件，因为只要是ready消息持久化到协调者，那就说明服务A的任务已经完成。
                    try {
                        coordinator.compensateReady((EventMessage) r);
                    } catch (Exception e) {
                        log.error("Message failed to be sent : ", e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("daemon process error: ", e);
        }
    }
}
