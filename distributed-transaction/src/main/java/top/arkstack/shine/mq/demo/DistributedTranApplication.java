package top.arkstack.shine.mq.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 7le
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class DistributedTranApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedTranApplication.class, args);
    }
}
