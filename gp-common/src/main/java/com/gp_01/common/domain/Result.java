package com.gp_01.common.domain;

import com.gp_01.common.enums.ResultCode;
import io.micrometer.common.util.StringUtils;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import static com.gp_01.common.enums.ResultCode.SERVER_ERROR;
import static com.gp_01.common.enums.ResultCode.SUCCESS;


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

    public static <T> Result<T> error(ResultCode resultCode){
        return build(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> Result<T> error(String msg){
        return build(SERVER_ERROR.getCode(), msg, null);
    }


    public static Result<Void> error(ResultCode resultCode, String message) {
        if(StringUtils.isNotEmpty(message)){
            return build(resultCode.getCode(), message,null);
        } else{
            return Result.error(resultCode);
        }
    }

}
