package com.gp_01.file.service.config;

import com.gp_01.common.constants.RabbitMqConstants;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
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



    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
        return new RepublishMessageRecoverer(rabbitTemplate, RabbitMqConstants.ERROR_EXCHANGE, RabbitmqFileConstants.ERROR_RK_FILE);
    }

    @Bean
    public Queue fileErrorQueue(){
        return new Queue(RabbitmqFileConstants.ERROR_QUEUE_FILE);
    }
    @Bean
    public TopicExchange globalErrorExchange(){
        return new TopicExchange(RabbitMqConstants.ERROR_EXCHANGE);
    }

    @Bean
    public Binding bindingFileErrorQueue(){
        return BindingBuilder.bind(fileErrorQueue()).to(globalErrorExchange()).with(RabbitmqFileConstants.ERROR_RK_FILE);
    }



}
