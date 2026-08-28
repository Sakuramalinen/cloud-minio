package com.gp_01.gateway.handler;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class RequestHeaderHandleRegister {

    private final List<RequestHeaderHandler> handlers;

    public RequestHeaderHandleRegister(List<RequestHeaderHandler> handlers) {
        this.handlers = handlers;
    }


}
