package com.admin.module.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    public static final String EXCHANGE_NAME = "file-converter-pdf-exchange";
    public static final String QUEUE_NAME = "file-converter-pdf-queue";
    public static final String ROUTING_KEY = "file-converter-pdf-routing-key";


    /**
     * 定义交换机
     * @return
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /**
     * 定义队列
     * @return
     */
    @Bean
    public Queue fileConverterPdfQueue() {
        return new Queue(QUEUE_NAME, true);
    }


    /**
     * 绑定队列到交换机
     * @param directExchange
     * @param queue
     * @return
     */
    @Bean
    public Binding fileConverterPdfBinding(DirectExchange directExchange, Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with(ROUTING_KEY);
    }

}
