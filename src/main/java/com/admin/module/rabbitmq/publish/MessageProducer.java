package com.admin.module.rabbitmq.publish;

import com.admin.module.rabbitmq.config.RabbitmqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试类，发送mq消息
 */
@Component
public class MessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;


    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME, RabbitmqConfig.ROUTING_KEY, message);
    }
}
