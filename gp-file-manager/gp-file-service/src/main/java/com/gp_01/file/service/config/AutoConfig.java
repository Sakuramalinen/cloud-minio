package com.gp_01.file.service.config;

import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.product.MinioDownloader;
import io.minio.MinioClient;
import org.apache.tika.Tika;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfig {

    @Bean
    public Downloader downloader(MinioClient minioClient){
        return new MinioDownloader(minioClient);
    }

    @Bean
    public Tika tika (){
        return new Tika();
    }

    /**
     * 配置amqp消息转换器
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }
}
