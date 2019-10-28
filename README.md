# shine-mq-demo 

> [shine-mq](https://github.com/7le/shine-mq) demo 🎥

English | [简体中文](./README-zh_CN.md)

### 🎈 Distributed transaction (based on reliable messaging service)

> Distributed transaction demo

To use a distributed transaction (annotation ``@DistributedTrans``), you need to enable the configuration:

```java
shine:
  mq:
    distributed:
      transaction: true
```

#### [Complete](https://github.com/7le/shine-mq-demo/tree/master/dt-complete)

In the **upstream service** (message producer) use the ``@DistributedTrans`` annotation to enable distributed transactions (supported with Spring's ``@Transactional``), as follows:

```java
/**
 * Service A's task
 * <p>
 * Can be implemented by yourself, or by default.
 * Annotation @DistributedTrans can be used with @Transactional
 */
@DistributedTrans(exchange = "route_config", routeKey = "route_config_key", bizId = "route_config",
        coordinator = "redisCoordinator")
@Transactional(rollbackFor = Exception.class)
public TransferBean transaction() {
    //Setting the check back id needs to be unique (you can use the id of the database) to prevent errors.
    Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
    
    //Prepare needs check back id to query service A task status, bizId, exchangeName and routingKey are necessary information for resending
    coordinator.setPrepare(new PrepareMessage(checkBackId.toString(), "route_config",
            "route_config", "route_config_key"));
    
    //Performing operations
    RouteConfig routeConfig = new RouteConfig(checkBackId, "/shine/**", "spring-mq",
            null, false, true, true, null);
    mapper.insert(routeConfig);
    
    //Used to simulate the success of task A, but not delivered to mq (that is, the compensation for testing the prepare message)
    //int i = 1 / 0;
    //Need to use the TransferBean wrapper, checkBackId is required, data can be null
    return new TransferBean(checkBackId.toString(), routeConfig.getPath());
}
```
> Here, by setting the check id, the atomicity of the service A task is guaranteed. The demo can use the timed task (other ways) to implement the check back.[Daemon](https://github.com/7le/shine-mq-demo/blob/master/dt-complete/dt-producer/src/main/java/top/arkstack/shine/mq/demo/daemon/Daemon.java)。


In addition ``shine-mq`` will be initialized in the settings **setConfirmCallback**, if you need to send a custom message to the callback after the MQ, you can implement the ``Coordinator`` ``confirmCallback`` interface.

In the downstream service, configure the queue corresponding to the upstream service and a dead letter queue.
```java
@PostConstruct
public void test() {
    //Service B configuration consumer
    factory.addDLX("route_config", "route_config",
            "route_config_key", new ProcessorTest(), SendTypeEnum.DISTRIBUTED);

    //Configure the dead letter queue to handle when it fails.
    factory.add(MqConstant.DEAD_LETTER_QUEUE, MqConstant.DEAD_LETTER_EXCHANGE,
            MqConstant.DEAD_LETTER_ROUTEKEY, new ProcessorException(), SendTypeEnum.DLX);
}

/**
 * Service B performs distributed transactions
 */
static class ProcessorTest extends BaseProcessor {

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        //Execute the task of service B. Here you can convert msg into TransferBean.
        if (!Objects.isNull(msg)) {
            TransferBean bean = JSONObject.parseObject(msg.toString(), TransferBean.class);
            //Here you can handle the task of Service B.
            log.info("(Route_config) Process task B : {}", bean.getData());
            log.info("(Route_config) CheckBackId : {}", bean.getCheckBackId());
        }
        //Distributed transaction message default automatic receipt
        return null;
    }
}

/**
 * Handling exceptions
 */
static class ProcessorException extends BaseProcessor {

    @Override
    public Object process(Object msg, Message message, Channel channel) {
        //If the failed task is executed, you can notify the manual processing or call back the original service A's rollback interface.
        log.info("Self-implementation Notification manual processing or callback of the original service A's rollback interface：" + msg);
        return null;
    }
}
```

The specific process is as follows:
![shine-mq](https://github.com/7le/7le.github.io/raw/master/image/dis/shine-mq_EN.jpg)

#### [Simple](https://github.com/7le/shine-mq-demo/tree/master/dt-simple)

> The simple version mainly eliminates the checkback mechanism and can be flexibly combined with other compensation methods to increase the reliability of the message, which is more convenient for integration and use. If you don't match it, you can use it directly, but there will be a small probability of losing the message (may be done after Task A has finished processing the task, sending a ready message to the Coordinator when an exception or downtime occurs, resulting in inconsistency), which can be neglected. Meet the general business scenario.

The consumer is the same as the complete, the producer is simplified as follows：
```java
/**
 * Service A's task
 * <p>
 * Can be implemented by yourself, or by default.
 * Annotation @DistributedTrans can be used with @Transactional
*/
@DistributedTrans(exchange = "simple_route_config", routeKey = "simple_route_config_key", bizId = "simple_route_config")
@Transactional(rollbackFor = Exception.class)
public TransferBean transaction() {
    //simple Do not verify the status of Service A. You may not set the Prepare status.
    Long checkBackId = SnowflakeIdGenerator.getInstance().nextNormalId();
    
    //Performing operations
    RouteConfig routeConfig = new RouteConfig(checkBackId,
            "/shine/simple/**", "spring-mq-simple", null, false, true,
            true, null);
    mapper.insert(routeConfig);
    return new TransferBean(checkBackId.toString(), routeConfig.getPath());
}
```

### 🎐 Mq operation package

#### [Independent](https://github.com/7le/shine-mq-demo/tree/master/mq-independent)

> Producers and consumers in different services

Need to be configured in the consumer's service:

```java
shine:
  mq:
    rabbit:
      listener-enable: true  # If the service order is just the message producer can be set to false, the default is false
```

#### [Mixed](https://github.com/7le/shine-mq-demo/tree/master/mq-mixed/mixed)

> Producer and consumer in the same service

When the producer and the consumer are in the same service, they need to be set：

```java
shine:
  mq:
    rabbit:
      listener-enable: true  # If the service order is just the message producer can be set to false, the default is false
```
