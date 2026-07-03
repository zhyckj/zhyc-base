/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开放 API 后端转发请求。
 */
public class OpenApiBackendRequest {

  /** HTTP 方法。 */
  private final String httpMethod;
  /** 请求体。 */
  private final String body;
  /** 请求内容类型。 */
  private final String contentType;
  /** 原始查询字符串。 */
  private final String queryString;
  /** 透传给后端业务服务的开放 API 上下文头。 */
  private final Map<String, String> headers;

  /**
   * 创建开放 API 后端转发请求。
   *
   * @param httpMethod HTTP 方法
   * @param body 请求体
   * @param contentType 请求内容类型
   */
  public OpenApiBackendRequest(String httpMethod, String body, String contentType) {
    this(httpMethod, body, contentType, null, Map.of());
  }

  /**
   * 创建开放 API 后端转发请求。
   *
   * @param httpMethod HTTP 方法
   * @param body 请求体
   * @param contentType 请求内容类型
   * @param queryString 原始查询字符串
   */
  public OpenApiBackendRequest(String httpMethod, String body, String contentType, String queryString) {
    this(httpMethod, body, contentType, queryString, Map.of());
  }

  /**
   * 创建开放 API 后端转发请求。
   *
   * @param httpMethod HTTP 方法
   * @param body 请求体
   * @param contentType 请求内容类型
   * @param headers 透传给后端业务服务的开放 API 上下文头
   */
  public OpenApiBackendRequest(String httpMethod, String body, String contentType, Map<String, String> headers) {
    this(httpMethod, body, contentType, null, headers);
  }

  /**
   * 创建开放 API 后端转发请求。
   *
   * @param httpMethod HTTP 方法
   * @param body 请求体
   * @param contentType 请求内容类型
   * @param queryString 原始查询字符串
   * @param headers 透传给后端业务服务的开放 API 上下文头
   */
  public OpenApiBackendRequest(String httpMethod, String body, String contentType, String queryString,
      Map<String, String> headers) {
    this.httpMethod = httpMethod;
    this.body = body;
    this.contentType = contentType;
    this.queryString = queryString;
    this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers == null ? Map.of() : headers));
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getBody() {
    return body;
  }

  public String getContentType() {
    return contentType;
  }

  public String getQueryString() {
    return queryString;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }
}
