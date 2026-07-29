package com.gp_01.file.service.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean("batchListenerFactory")
    public SimpleRabbitListenerContainerFactory batchListenerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 开启批量消费核心开关
        factory.setBatchListener(true);
        // 单次最多拉取多少条消息（攒批阈值）
        factory.setBatchSize(100);
        // 超时时间：即使没凑够100条，超时500ms强制消费
        factory.setBatchReceiveTimeout(500L);
        // 手动确认消息（必须，防止丢消息）
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 并发消费者数量，根据服务器性能调整
        factory.setConcurrentConsumers(5);
        return factory;
    }
}
