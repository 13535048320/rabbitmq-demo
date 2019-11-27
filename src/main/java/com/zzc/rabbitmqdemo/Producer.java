package com.zzc.rabbitmqdemo;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单队列
 *
 * 更多例子参考
 * https://blog.csdn.net/hellozpc/article/details/81436980#8SpringbootRabbitMQ_1267
 * https://blog.csdn.net/aa1215018028/article/details/81325082
 */
@Component
public class Producer {
	@Autowired
	private AmqpTemplate rabbitTemplate;

	public void send() {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());// 24小时制
		String context = "hello " + date;
		System.out.println("Sender : " + context);
		this.rabbitTemplate.convertAndSend("demo", context);
	}
}
