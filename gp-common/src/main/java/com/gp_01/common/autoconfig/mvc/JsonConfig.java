package com.gp_01.common.autoconfig.mvc;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.time.format.DateTimeFormatter;

import static com.gp_01.common.constants.DateConstants.DEFAULT_LOCAL_DATE_TIME_FORMAT;
import static com.gp_01.common.constants.DateConstants.TIME_ZONE_8;

@Configuration
public class JsonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> {
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern(DEFAULT_LOCAL_DATE_TIME_FORMAT);
            jacksonObjectMapperBuilder.serializers(new LocalDateTimeSerializer(pattern));
            jacksonObjectMapperBuilder.deserializers(new LocalDateTimeDeserializer(pattern));
            jacksonObjectMapperBuilder.timeZone(TIME_ZONE_8);
            //long转String
            jacksonObjectMapperBuilder.serializerByType(Long.class, ToStringSerializer.instance);
            //BigInteger转String
            jacksonObjectMapperBuilder.serializerByType(BigInteger.class, ToStringSerializer.instance);

        };
    }
}
