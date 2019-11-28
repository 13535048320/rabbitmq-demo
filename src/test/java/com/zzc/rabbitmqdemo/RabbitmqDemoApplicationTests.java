package com.zzc.rabbitmqdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitmqDemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private Producer producer;

    @Test
    public void send() throws Exception {
        for (int i = 0; i < 5; i++) {
            producer.send();
            Thread.sleep(10);
        }
    }
}
