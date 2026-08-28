package com.gp_01.gateway.handler;


import com.gp_01.common.enums.RequestHeaderEnum;
import com.gp_01.gateway.domain.RequestHeaderParseResult;
import org.springframework.http.HttpHeaders;

public interface RequestHeaderHandler {

//    RequestHeaderEnum supportedHeader();

    RequestHeaderParseResult handle(HttpHeaders headers);

    String ErrorMessage();

}
