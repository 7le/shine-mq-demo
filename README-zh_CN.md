# shine-mq-demo 

> [shine-mq](https://github.com/7le/shine-mq) 演示demo 🎥

[English](README.md) | 简体中文

### 🎈 分布式事务（基于可靠消息服务）

> 分布式事务demo

目前支持三种模式：

* [Complete 完整模式](#Complete)
* [Rollback 完整模式增加异常回滚](#Rollback)
* [Simple 简单模式](#Simple)

使用分布式事务（注解``@DistributedTrans``），需要开启配置：

```java
shine:
  mq:
    distributed:
      transaction: true
```

#### [Complete](https://github.com/7le/shine-mq-demo/tree/master/dt-complete)

在**上游服务**(消息生产者) 使用``@DistributedTrans``注解可以开启分布式事务(支持与Spring的``@Transactional``共用)，具体如下：

```java
/**
 * 服务A 的任务
 * <p>
 * coordinator 可以自行实现，或者使用默认提供的
 * 注解@DistributedTrans可以和@Transactional共用
 */
@DistributedTrans(exchange = "route_config", routeKey = "route_config_key", bizId = "route_config",
        coordinator = "redisCoordinator")
@Transactional(rollbackFor = Exception.class)
public TransferBean transaction() {
    //设置回查id 需要唯一 （可以用数据库的id） 以防出现错误，
    Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
    
    //prepare需要checkBackId（回查id）来查询服务A任务状态，bizId,exchangeName和routingKey是重发的必要信息
    coordinator.setPrepare(new PrepareMessage(checkBackId.toString(), "route_config",
            "route_config", "route_config_key"));
    
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
> 这里通过设置回查id，来保证服务A任务的原子性。demo中用定时任务（也可以其他方式）实现回查，具体可以看[daemon](https://github.com/7le/shine-mq-demo/blob/master/dt-complete/dt-producer/src/main/java/top/arkstack/shine/mq/demo/daemon/Daemon.java)。


另外``shine-mq``会在初始化设置**setConfirmCallback**，如果需要自定义消息发送到MQ后的回调，可以自行实现``Coordinator``的``confirmCallback``接口。

在下游服务，配置对应上游服务的队列和一条死信队列。
```java
@PostConstruct
public void test() {
    //服务B 配置消费者
    factory.addDLX("route_config", "route_config",
            "route_config_key", new ProcessorTest(), SendTypeEnum.DISTRIBUTED);

    //配置死信队列 失败时候处理
    factory.add(MqConstant.DEAD_LETTER_QUEUE, MqConstant.DEAD_LETTER_EXCHANGE,
            MqConstant.DEAD_LETTER_ROUTEKEY, new ProcessorException(), SendTypeEnum.DLX);
}

/**
 * 服务B 执行分布式事务
 */
static class ProcessorTest extends BaseProcessor {

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        //执行服务B的任务  这里可以将msg转成TransferBean
        if (!Objects.isNull(msg)) {
            TransferBean bean = JSONObject.parseObject(msg.toString(), TransferBean.class);
            //这里就可以处理服务B的任务了
            log.info("(Route_config) Process task B : {}", bean.getData());
            log.info("(Route_config) CheckBackId : {}", bean.getCheckBackId());
        }
        //分布式事务消息默认自动回执
        return null;
    }
}

/**
 * 处理异常
 */
static class ProcessorException extends BaseProcessor {

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        //执行失败的任务，可以自行实现 通知人工处理 或者回调原服务A的回滚接口
        log.info("自行实现 通知人工处理 或者回调原服务A的回滚接口：" + msg);
        return null;
    }
}
```

具体流程如图：
![shine-mq](https://github.com/7le/7le.github.io/raw/master/image/dis/shine-mq.jpg)

#### [Rollback](https://github.com/7le/shine-mq-demo/tree/master/dt-rollback)

在原先的Complete的基础上，增加了异常回滚功能，就是下游服务在处理分布式事务消息的时候出现异常，通过异常回滚将上游服务进行回滚。

上游服务在使用``@DistributedTrans``的时候增加**rollback**

```java
/**
 * 服务A 的任务
 * <p>
 * coordinator 可以自行实现，或者使用默认提供的
 * 注解@DistributedTrans可以和@Transactional共用
 */
@DistributedTrans(exchange = "route_config", routeKey = "route_config_key", bizId = "route_config",
        coordinator = "redisCoordinator", rollback = "route_config_rollback")
@Transactional(rollbackFor = Exception.class)
public TransferBean transaction() {
    //设置回查id 需要唯一 （可以用数据库的id） 以防出现错误，
    Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
    
    //prepare需要checkBackId（回查id）来查询服务A任务状态，bizId,exchangeName和routingKey是重发的必要信息
    coordinator.setPrepare(new PrepareMessage(checkBackId.toString(), "route_config",
            "route_config", "route_config_key"));
    
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
以及增加相应的队列监听：
```java
//增加对回滚队列的监听
factory.add("route_config_rollback", "route_config",
        "route_config_rollback", rollback, SendTypeEnum.ROLLBACK);
```

配置为：
```java
shine:
  mq:
    distributed:
      transaction: true
    rabbit:
      listener-enable: true
```

而下游服务与[Complete](#Complete)模式使用的方式一样，其中回滚的消息使用``coordinator``做了保障，保证消息能可靠投递到队列，而异常回滚的消息的回执由使用者来控制。

#### [Simple](https://github.com/7le/shine-mq-demo/tree/master/dt-simple)

> 简单版主要是省去了回查机制，可以灵活搭配其他的补偿方式来增加消息的可靠性，更方便集成和使用。不搭配也可以直接使用，只是会有小概率的消息丢失(可能会在任务A处理完任务，发送ready消息到Coordinator的时候出现异常或者宕机，导致出现不一致)，基本可以忽略不计，完全可以满足一般业务场景了。

消费者跟complete相同，生产者简化如下：
```java
/**
* 服务A 的任务
* <p>
* coordinator 可以自行实现，或者使用默认提供的
* 注解@DistributedTrans可以和@Transactional共用
*/
@DistributedTrans(exchange = "simple_route_config", routeKey = "simple_route_config_key", bizId = "simple_route_config")
@Transactional(rollbackFor = Exception.class)
public TransferBean transaction() {
    //simple 不校验服务A的状态 可以不设置Prepare状态
    Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
    
    //执行操作
    RouteConfig routeConfig = new RouteConfig(checkBackId,
            "/shine/simple/**", "spring-mq-simple", null, false, true,
            true, null);
    mapper.insert(routeConfig);
    return new TransferBean(checkBackId.toString(), routeConfig.getPath());
}
```

### 🎐 mq操作封装

#### [Independent](https://github.com/7le/shine-mq-demo/tree/master/mq-independent)

> 生产者和消费者在不同的服务内

需要在消费者的服务配置:

```java
shine:
  mq:
    rabbit:
      listener-enable: true  # 若服务单单只是消息生产者可以设为false，默认为false
```

#### [Mixed](https://github.com/7le/shine-mq-demo/tree/master/mq-mixed/mixed)

> 生产者和消费者在同一个服务

当生产者和消费者在同一个服务，需要设置：

```java
shine:
  mq:
    rabbit:
      listener-enable: true  # 若服务单单只是消息生产者可以设为false，默认为false
``` 