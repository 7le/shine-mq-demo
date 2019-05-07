package top.arkstack.shine.mq.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 7le
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class DistributedTranApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedTranApplication.class, args);
    }
}
