package com.block.chain.utils;



/*

 */
public enum ResultCode implements ErrorCode {

//    ############################### 常用状态码 ###########################################################

    //    操作成功
    SUCCESS(true, 200, "OK"),
    //    操作失败
    FAI(false, 4000, "sys_opt_fail"),
    //    错误的请求
    BAD_REQUEST(false, 400, "bad_request"),
    //    未授权 暂未登录或token已经过期
    UNAUTHORIZED(false, 401, "unauthorized"),
    //    无权限访问
    FORBIDDEN(false, 403, "forbidden"),
    //    实体类太大
    REQUEST_ENTITY_TOO_LARGE(false, 413, "request_entity_too_large"),
    //    页面无法访问
    NOT_FOUND(false, 404, "not_found"),
    //    服务器异常
    FAILED(false, 500, "internal_server_error"),
    //    接口未实现
    NOT_IMPLEMENTED(false, 501, "not_implemented"),
    //    网关超时
    GATEWAY_TIMEOUT(false, 504, "gateway_timeout"),
    //    HTTP版本不支持
    HTTP_VERSION_NOT_SUPPORTED(false, 505, "http_version_not_supported"),
    //    实体不存在
    ENTITY_NOT_EXITS(false, 40001, "entity_not_exits"),
    //    参数错误
    ERROR_PARAM(false, 40002, "error_param"),
    //    参数{}为空
    PARAM_IS_NULL(false, 40003, "param_is_null");

//    ################################ 常用状态码 ##########################################################


    private boolean success;
    private int code;
    private String message;

    private ResultCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
