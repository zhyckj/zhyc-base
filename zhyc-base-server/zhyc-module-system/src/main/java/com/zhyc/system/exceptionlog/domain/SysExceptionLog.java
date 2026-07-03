/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.domain;

import java.time.LocalDateTime;

/**
 * 系统异常日志领域模型。
 *
 * <p>异常日志按 {@code tenantId} 做租户隔离，用于沉淀接口异常、链路追踪和问题定位信息。</p>
 */
public class SysExceptionLog {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 链路追踪编号。 */
    private String traceId;
    /** 操作用户主键，匿名访问可为空。 */
    private Long userId;
    /** 操作账号。 */
    private String username;
    /** 请求地址。 */
    private String requestUri;
    /** 请求方法。 */
    private String requestMethod;
    /** 异常类名。 */
    private String exceptionName;
    /** 异常消息。 */
    private String message;
    /** 异常堆栈。 */
    private String stackTrace;
    /** 客户端 IP。 */
    private String clientIp;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空异常日志对象。
     */
    public SysExceptionLog() {
    }

    /**
     * 创建完整异常日志对象。
     *
     * @param id 数据库主键
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
     * @param createdAt 创建时间
     */
    public SysExceptionLog(Long id, String tenantId, String traceId, Long userId, String username,
                           String requestUri, String requestMethod, String exceptionName, String message,
                           String stackTrace, String clientIp, LocalDateTime createdAt) {
        this.id = id;
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
        this.createdAt = createdAt;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
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
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
     * 设置链路追踪编号。
     *
     * @param traceId 链路追踪编号
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
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
     * 设置操作用户主键。
     *
     * @param userId 操作用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * 设置操作账号。
     *
     * @param username 操作账号
     */
    public void setUsername(String username) {
        this.username = username;
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
     * 设置请求地址。
     *
     * @param requestUri 请求地址
     */
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
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
     * 设置请求方法。
     *
     * @param requestMethod 请求方法
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
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
     * 设置异常类名。
     *
     * @param exceptionName 异常类名
     */
    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
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
     * 设置异常消息。
     *
     * @param message 异常消息
     */
    public void setMessage(String message) {
        this.message = message;
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
     * 设置异常堆栈。
     *
     * @param stackTrace 异常堆栈
     */
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * 返回客户端 IP。
     *
     * @return 客户端 IP
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * 设置客户端 IP。
     *
     * @param clientIp 客户端 IP
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
