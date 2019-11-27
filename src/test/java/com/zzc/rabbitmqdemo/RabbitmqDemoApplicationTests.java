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
        producer.send();
    }



}
