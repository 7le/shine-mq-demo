# shine-mq-demo 

> shine-mq 演示demo 🎥

### 🎈 分布式事务（基于可靠消息服务）

> 分布式事务demo

使用分布式事务（注解``@DistributedTrans``），需要开启配置：

```
shine:
  mq:
    distributed:
      transaction: true
```

服务A戳 [服务A](https://github.com/7le/shine-mq-demo/tree/master/distributed-transaction)

```
/**
 * 服务A 的任务
 */
@DistributedTrans(exchange = "dis_test", routeKey = "dis_test_key", bizId = "ccc", coordinator = "redisCoordinator")
public String transaction() {
    //设置回查id 需要唯一 以防出现错误
    String checkBackId="123456789";
    coordinator.setPrepare(checkBackId);
    return "DistributedTran";
}
```
这里通过设置回查id，来保证服务A任务的原子性。

具体流程如图：
![shine-mq](https://github.com/7le/7le.github.io/raw/master/image/dis/shine-mq.jpg)

服务B戳 [服务B](https://github.com/7le/shine-mq-demo/tree/master/distributed-transaction-consumer)

### 🎐 mq操作封装

#### Producer Consumer 在不同服务

生产者戳 [Producer](https://github.com/7le/shine-mq-demo/tree/master/msg-encapsulate-1)

需要在消费者的服务配置
```
shine:
  mq:
    rabbit:
      listener-enable: true  # 若服务单单只是消息生产者可以设为false
```
消费者戳 [Consumer](https://github.com/7le/shine-mq-demo/tree/master/msg-encapsulate-2)

#### Producer Consumer 在同一服务

生产者&消费者戳 [Producer&Consumer](https://github.com/7le/shine-mq-demo/tree/master/msg-encapsulate)

