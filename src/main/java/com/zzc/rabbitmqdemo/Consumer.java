package com.zzc.rabbitmqdemo;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "demo")
public class Consumer {

	@RabbitHandler
	public void process(String hello) {
		System.out.println("Receiver  : " + hello);
	}
}
