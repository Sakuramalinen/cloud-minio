package com.gp_01.gateway.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.CommonException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Data
@Slf4j
public class RequestHeaderParseResult {

    private ResultEnums resultEnums;

    private String headerKey;
    private String headerValue;

    public RequestHeaderParseResult(ResultEnums resultEnums) {
        this.resultEnums = resultEnums;
    }

//    public RequestHeaderParseResult(String headerKey, String headerValue) {
//        this.resultEnums = ResultEnums.SUCCESS;
//        this.headerKey = headerKey;
//        this.headerValue = headerValue;
//    }

    public RequestHeaderParseResult(String headerKey, Object headerValue) {
        this.resultEnums = ResultEnums.SUCCESS;
        this.headerKey = headerKey;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.headerValue = objectMapper.writeValueAsString(headerValue);
        } catch (JsonProcessingException e) {
            log.error("对象转json失败，object: {}", headerValue);
            throw new CommonException(ErrorCode.SERVICE_ERROR);
        }
    }
}
