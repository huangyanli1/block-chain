package com.block.chain.utils;

import cn.hutool.core.lang.Assert;
import com.block.chain.utils.ResponseCode;
import com.block.chain.utils.ResultCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 统一数据返回 可根据实际需要修改和扩编
 */
@ApiModel(value = "统一数据返回对象", description = "所有数据经此包装")
public class ResponseData<T> {

    private static final long serialVersionUID = 1L;

    public boolean success;
    /**
     * 状态码
     */
    @ApiModelProperty(required = true, value = "返回状态码", dataType = "int", example = "200", position = 0)
    public int code;

    /**
     * msg
     */
    @ApiModelProperty(required = true, value = "返回message 信息", dataType = "string", example = "success", position = 2)
    private String message;


    public String msg;
    /**
     * 返回的数据
     */
    @ApiModelProperty(required = true, value = "返回数据", dataType = "string", example = "data", position = 1)
    public T data;

    /**
     * 返回成功默认状态码200
     */
    public static <T> ResponseData<T> ok() {
        return returnResult(true, ResultCode.SUCCESS.getMessage(), ResponseCode.OK, null);
    }

    /**
     * 返回成功默认状态码200
     *
     * @Param data 返回的数据
     */
    public static <T> ResponseData<T> ok(T data) {
        return returnResult(true, ResultCode.SUCCESS.getMessage(), ResponseCode.OK, data);
    }

    /**
     * 返回成功 自定义状态码消息
     *
     * @Param msg  返回的消息
     */
    public static <T> ResponseData<T> ok(String msg) {
        return returnResult(true, msg, ResponseCode.OK, null);
    }

    /**
     * 返回成功 自定义状态码消息
     *
     * @Param data 返回的数据
     * @Param code 返回的状态码
     * @Param msg  返回的消息
     */
    public static <T> ResponseData<T> ok(String msg, T data) {
        return returnResult(true, msg, ResponseCode.OK, data);
    }

    /**
     * 返回成功 自定义状态码消息
     *
     * @Param data 返回的数据
     * @Param code 返回的状态码
     * @Param msg  返回的消息
     */
    public static <T> ResponseData<T> ok(String msg, int code, T data) {
        return returnResult(true, msg, code, data);
    }

    /**
     * 操作失败
     */
    public static <T> ResponseData<T> fail() {
        return returnResult(false, ResultCode.FAI.getMessage(), ResponseCode.ERROR, null);
    }

    /**
     * 失败返回结果
     * @param msg 返回失败的消息
     */
    public static <T> ResponseData<T> fail(String msg) {
        return returnResult(false, msg, ResponseCode.ERROR, null);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     */
    public static <T> ResponseData<T> fail(ErrorCode errorCode) {
        return returnResult(false, errorCode.getMessage(), errorCode.getCode(), null);
    }

    /**
     * 参数验证失败返回结果
     * @param msg 提示信息
     */
    public static <T> ResponseData<T> validateFailed(String msg) {
        return returnResult(false, msg, ResultCode.ERROR_PARAM.getCode(), null);
    }

    /**
     * 返回失败
     *
     * @Param msg 返回失败的消息 必须
     * @Param code 返回失败的错误码 必须
     */
    public static <T> ResponseData<T> fail(String msg, int code) {
        return returnResult(false, msg, code,  null);
    }

    public static <T> ResponseData<T> fail(int code, String msg) {
        return returnResult(false,   msg,code, null);
    }

    /**
     * 返回失败
     *
     * @Param data 返回的数据
     * @Param code 返回的状态码
     * @Param msg  返回的消息
     */
    public static <T> ResponseData<T> fail(String msg, int code, T data) {
        return returnResult(false, msg, code, data);
    }

    private static <T> ResponseData<T> returnResult(boolean success, String msg, int code, T data) {
        ResponseData<T> rspData = new ResponseData<>();
        rspData.setSuccess(success);
        rspData.setCode(code);
        rspData.setMsg(msg);
        rspData.setData(data);
        return rspData;
    }


    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
