/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit;

import java.time.Instant;

/**
 * Open API 请求审计事件模型。
 *
 * <p>当前类仅作为普通 Java 数据模型使用，不绑定数据库表或消息队列。</p>
 */
public class OpenApiAuditEvent {

  /** 租户标识。 */
  private String tenantId;

  /** Open API 应用标识。 */
  private String appKey;

  /** 调用方标识，例如外部系统、账号或客户端名称。 */
  private String caller;

  /** 调用方来源 IP。 */
  private String sourceIp;

  /** 被调用的 API 路径。 */
  private String apiPath;

  /** HTTP 请求方法。 */
  private String method;

  /** 调用是否成功。 */
  private boolean success;

  /** HTTP 响应状态码。 */
  private int statusCode;

  /** 请求处理耗时，单位毫秒。 */
  private long latencyMs;

  /** 审计补充信息或失败原因。 */
  private String message;

  /** 标准化错误码。 */
  private String errorCode;

  /** 面向审计排查的错误详情。 */
  private String errorMessage;

  /** 请求唯一标识。 */
  private String requestId;

  /** 链路追踪标识。 */
  private String traceId;

  /** 审计事件发生时间。 */
  private Instant timestamp;

  /**
   * 创建空审计事件。
   */
  public OpenApiAuditEvent() {
  }

  /**
   * 创建包含全部字段的审计事件。
   *
   * @param tenantId 租户标识
   * @param appKey Open API 应用标识
   * @param apiPath 被调用的 API 路径
   * @param method HTTP 请求方法
   * @param success 调用是否成功
   * @param statusCode HTTP 响应状态码
   * @param latencyMs 请求处理耗时，单位毫秒
   * @param message 审计补充信息或失败原因
   * @param timestamp 审计事件发生时间
   */
  public OpenApiAuditEvent(String tenantId, String appKey, String apiPath, String method,
      boolean success, int statusCode, long latencyMs, String message, Instant timestamp) {
    this.tenantId = tenantId;
    this.appKey = appKey;
    this.apiPath = apiPath;
    this.method = method;
    this.success = success;
    this.statusCode = statusCode;
    this.latencyMs = latencyMs;
    this.message = message;
    this.timestamp = timestamp;
  }

  /**
   * 创建包含扩展审计字段的审计事件。
   *
   * @param tenantId 租户标识
   * @param appKey Open API 应用标识
   * @param caller 调用方标识
   * @param sourceIp 调用方来源 IP
   * @param apiPath 被调用的 API 路径
   * @param method HTTP 请求方法
   * @param success 调用是否成功
   * @param statusCode HTTP 响应状态码
   * @param latencyMs 请求处理耗时，单位毫秒
   * @param message 审计补充信息或失败原因
   * @param errorCode 标准化错误码
   * @param errorMessage 面向审计排查的错误详情
   * @param requestId 请求唯一标识
   * @param traceId 链路追踪标识
   * @param timestamp 审计事件发生时间
   */
  public OpenApiAuditEvent(String tenantId, String appKey, String caller, String sourceIp,
      String apiPath, String method, boolean success, int statusCode, long latencyMs,
      String message, String errorCode, String errorMessage, String requestId, String traceId,
      Instant timestamp) {
    this.tenantId = tenantId;
    this.appKey = appKey;
    this.caller = caller;
    this.sourceIp = sourceIp;
    this.apiPath = apiPath;
    this.method = method;
    this.success = success;
    this.statusCode = statusCode;
    this.latencyMs = latencyMs;
    this.message = message;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.requestId = requestId;
    this.traceId = traceId;
    this.timestamp = timestamp;
  }

  /**
   * 获取租户标识。
   *
   * @return 租户标识
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 设置租户标识。
   *
   * @param tenantId 租户标识
   */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /**
   * 获取 Open API 应用标识。
   *
   * @return Open API 应用标识
   */
  public String getAppKey() {
    return appKey;
  }

  /**
   * 设置 Open API 应用标识。
   *
   * @param appKey Open API 应用标识
   */
  public void setAppKey(String appKey) {
    this.appKey = appKey;
  }

  /**
   * 获取调用方标识。
   *
   * @return 调用方标识
   */
  public String getCaller() {
    return caller;
  }

  /**
   * 设置调用方标识。
   *
   * @param caller 调用方标识
   */
  public void setCaller(String caller) {
    this.caller = caller;
  }

  /**
   * 获取调用方来源 IP。
   *
   * @return 调用方来源 IP
   */
  public String getSourceIp() {
    return sourceIp;
  }

  /**
   * 设置调用方来源 IP。
   *
   * @param sourceIp 调用方来源 IP
   */
  public void setSourceIp(String sourceIp) {
    this.sourceIp = sourceIp;
  }

  /**
   * 获取被调用的 API 路径。
   *
   * @return 被调用的 API 路径
   */
  public String getApiPath() {
    return apiPath;
  }

  /**
   * 设置被调用的 API 路径。
   *
   * @param apiPath 被调用的 API 路径
   */
  public void setApiPath(String apiPath) {
    this.apiPath = apiPath;
  }

  /**
   * 获取 HTTP 请求方法。
   *
   * @return HTTP 请求方法
   */
  public String getMethod() {
    return method;
  }

  /**
   * 设置 HTTP 请求方法。
   *
   * @param method HTTP 请求方法
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * 获取调用是否成功。
   *
   * @return 调用是否成功
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * 设置调用是否成功。
   *
   * @param success 调用是否成功
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * 获取 HTTP 响应状态码。
   *
   * @return HTTP 响应状态码
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * 设置 HTTP 响应状态码。
   *
   * @param statusCode HTTP 响应状态码
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * 获取请求处理耗时。
   *
   * @return 请求处理耗时，单位毫秒
   */
  public long getLatencyMs() {
    return latencyMs;
  }

  /**
   * 设置请求处理耗时。
   *
   * @param latencyMs 请求处理耗时，单位毫秒
   */
  public void setLatencyMs(long latencyMs) {
    this.latencyMs = latencyMs;
  }

  /**
   * 获取审计补充信息。
   *
   * @return 审计补充信息或失败原因
   */
  public String getMessage() {
    return message;
  }

  /**
   * 设置审计补充信息。
   *
   * @param message 审计补充信息或失败原因
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * 获取标准化错误码。
   *
   * @return 标准化错误码
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * 设置标准化错误码。
   *
   * @param errorCode 标准化错误码
   */
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * 获取面向审计排查的错误详情。
   *
   * @return 面向审计排查的错误详情
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * 设置面向审计排查的错误详情。
   *
   * @param errorMessage 面向审计排查的错误详情
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * 获取请求唯一标识。
   *
   * @return 请求唯一标识
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   * 设置请求唯一标识。
   *
   * @param requestId 请求唯一标识
   */
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  /**
   * 获取链路追踪标识。
   *
   * @return 链路追踪标识
   */
  public String getTraceId() {
    return traceId;
  }

  /**
   * 设置链路追踪标识。
   *
   * @param traceId 链路追踪标识
   */
  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  /**
   * 获取审计事件发生时间。
   *
   * @return 审计事件发生时间
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  /**
   * 设置审计事件发生时间。
   *
   * @param timestamp 审计事件发生时间
   */
  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
