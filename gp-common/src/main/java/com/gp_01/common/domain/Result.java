package com.gp_01.common.domain;

import com.gp_01.common.enums.ErrorCode;
import io.micrometer.common.util.StringUtils;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import static com.gp_01.common.enums.ErrorCode.SUCCESS;


@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;
    private T data;

    private Result(){}

    private static <T> Result<T> build(Integer code, String msg, T data){
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }


    public static  <T> Result<T> success(){
        return success(null);
    }

    public static <T> Result<T> success(T data){
        return build(SUCCESS.getCode(), SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> error(ErrorCode errorCode){
        return build(errorCode.getCode(), errorCode.getMsg(), null);
    }


    public static Result<?> error(Integer code, String msg) {
        return build(code, msg, null);
    }

}
