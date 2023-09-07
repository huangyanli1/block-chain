package com.block.chain.utils;

/**
 * 通用http操作状态码
 */
public class ResponseCode {
    /**
     * 操作成功
     */
    public static final int OK = 200;

    /**
     * 资源创建成功
     */
    public static final int CREATED = 201;

    /**
     * 请求接收
     */
    public static final int ACCEPTED = 202;

    /**
     * 操作已经执行成功，但是没有返回数据
     */
    public static final int NO_DATA = 204;

    /**
     * 资源已被移除
     */
    public static final int RESOURCE_REMOVED = 301;

    /**
     * 参数列表错误（缺少，格式不匹配）
     */
    public static final int ERROR_PARAM = 400;

    /**
     * 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 访问受限，授权过期
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源，服务未找到
     */
    public static final int NOT_FOUND = 404;

    /**
     * 系统内部错误
     */
    public static final int ERROR = 500;

    /**
     * 接口未实现
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * 单点登录错误
     */
    public static final int SSO_ERROR = 10001;

    /**
     * 实体不存在
     */
    public static final int ENTITY_NOT_EXITS = 40001;


}
