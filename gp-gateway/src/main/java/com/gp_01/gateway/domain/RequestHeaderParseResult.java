package com.gp_01.gateway.domain;

import lombok.Data;

import java.util.HashMap;

@Data
public class RequestHeaderParseResult {

    private ResultEnums resultEnums;

    private HashMap<String, String> headers;

}
