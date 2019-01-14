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

[服务A](https://github.com/7le/shine-mq-demo/tree/master/distributed-transaction)

[服务B](https://github.com/7le/shine-mq-demo/tree/master/distributed-transaction-consumer)

### 🎐 mq操作封装

#### Producer Consumer 在不同服务

[Producer](https://github.com/7le/shine-mq-demo/tree/master/msg-encapsulate-1)

需要在消费者的服务配置
```
shine:
  mq:
    rabbit:
      listener-enable: true  # 若服务单单只是消息生产者可以设为false
```
[Consumer](https://github.com/7le/shine-mq-demo/tree/master/msg-encapsulate-2)

#### Producer Consumer 在同一服务

[Producer&Consumer](https://github.com/7le/shine-mq-demo/tree/master/msg-encapsulate)

