# shine-mq-demo 

> [shine-mq](https://github.com/7le/shine-mq) 演示demo 🎥

### 🎈 分布式事务（基于可靠消息服务）

> 分布式事务demo

使用分布式事务（注解``@DistributedTrans``），需要开启配置：

```
shine:
  mq:
    distributed:
      transaction: true
```

#### complete

在**上游服务（消息生产者）**使用``@DistributedTrans``注解可以开启分布式事务(支持与Spring的``@Transactional``共用)，具体如下：

```
/**
 * 服务A 的任务
 * <p>
 * coordinator 可以自行实现，或者使用默认提供的
 */
@DistributedTrans(exchange = "route_config", routeKey = "route_config_key", bizId = "route_config",
        coordinator = "redisCoordinator")
public TransferBean transaction() {
    //设置回查id 需要唯一 （可以用数据库的id） 以防出现错误，
    Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
    //prepare需要checkBackId（回查id）来查询服务A任务状态，bizId,exchangeName和routingKey是重发的必要信息
    coordinator.setPrepare(new PrepareMessage(checkBackId.toString(), "route_config",
            "route_config", "route_config_key"
    //执行操作
    RouteConfig routeConfig = new RouteConfig(checkBackId, "/shine/**", "spring-mq",
            null, false, true, true, null);
    mapper.insert(routeConfig);
    //用来模拟任务A成功，但是没有投递到mq(就是测试prepare消息的补偿)
    //int i = 1 / 0;
    //需要用TransferBean包装下，checkBackId是必须的，data可以为null
    return new TransferBean(checkBackId.toString(), routeConfig.getPath());
}
```
> 这里通过设置回查id，来保证服务A任务的原子性。demo中用定时任务（也可以其他方式）实现回查，具体可以看[daemon](https://github.com/7le/shine-mq-demo/blob/master/distributed-transaction/src/main/java/top/arkstack/shine/mq/demo/daemon/Daemon.java)。


另外``shine-mq``会在初始化设置**setConfirmCallback**，如果需要自定义消息发送到MQ后的回调，可以自行实现``Coordinator``的``confirmCallback``接口。

具体流程如图：
![shine-mq](https://github.com/7le/7le.github.io/raw/master/image/dis/shine-mq.jpg)

#### simple

> 简单版主要是省去了回查机制，可以灵活搭配其他的补偿方式来增加消息的可靠性，更方便集成和使用。不搭配也可以直接使用，只是会有小概率的消息丢失，基本满足一些业务场景了。

### 🎐 mq操作封装

#### independent

> 生产者和消费者在不同的服务内

需要在消费者的服务配置:

```
shine:
  mq:
    rabbit:
      listener-enable: true  # 若服务单单只是消息生产者可以设为false，默认为false
```

#### mixed

> 生产者和消费者在同一个服务

当生产者和消费者在同一个服务，需要设置：

```
shine:
  mq:
    rabbit:
      listener-enable: true  # 若服务单单只是消息生产者可以设为false，默认为false
```