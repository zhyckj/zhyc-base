/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.service;

/**
 * 系统异常日志记录命令。
 */
public class SysExceptionLogRecordCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 链路追踪编号。 */
    private final String traceId;
    /** 操作用户主键。 */
    private final Long userId;
    /** 操作账号。 */
    private final String username;
    /** 请求地址。 */
    private final String requestUri;
    /** 请求方法。 */
    private final String requestMethod;
    /** 异常类名。 */
    private final String exceptionName;
    /** 异常消息。 */
    private final String message;
    /** 异常堆栈。 */
    private final String stackTrace;
    /** 客户端 IP。 */
    private final String clientIp;

    /**
     * 创建系统异常日志记录命令。
     *
     * @param tenantId 租户业务编码
     * @param traceId 链路追踪编号
     * @param userId 操作用户主键
     * @param username 操作账号
     * @param requestUri 请求地址
     * @param requestMethod 请求方法
     * @param exceptionName 异常类名
     * @param message 异常消息
     * @param stackTrace 异常堆栈
     * @param clientIp 客户端 IP
     */
    public SysExceptionLogRecordCommand(String tenantId, String traceId, Long userId, String username,
                                        String requestUri, String requestMethod, String exceptionName,
                                        String message, String stackTrace, String clientIp) {
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.userId = userId;
        this.username = username;
        this.requestUri = requestUri;
        this.requestMethod = requestMethod;
        this.exceptionName = exceptionName;
        this.message = message;
        this.stackTrace = stackTrace;
        this.clientIp = clientIp;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回链路追踪编号。
     *
     * @return 链路追踪编号
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 返回操作用户主键。
     *
     * @return 操作用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回操作账号。
     *
     * @return 操作账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 返回请求地址。
     *
     * @return 请求地址
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * 返回请求方法。
     *
     * @return 请求方法
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * 返回异常类名。
     *
     * @return 异常类名
     */
    public String getExceptionName() {
        return exceptionName;
    }

    /**
     * 返回异常消息。
     *
     * @return 异常消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 返回异常堆栈。
     *
     * @return 异常堆栈
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * 返回客户端 IP。
     *
     * @return 客户端 IP
     */
    public String getClientIp() {
        return clientIp;
    }
}
