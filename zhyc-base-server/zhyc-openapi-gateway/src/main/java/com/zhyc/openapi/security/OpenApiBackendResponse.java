/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Map;
import java.util.Objects;

/**
 * 开放 API 后端转发响应。
 */
public class OpenApiBackendResponse {

  /** HTTP 响应状态码。 */
  private final int status;
  /** 响应内容类型。 */
  private final String contentType;
  /** 响应体。 */
  private final String body;
  /** 允许回传给开放 API 调用方的响应头。 */
  private final Map<String, String> headers;

  /**
   * 创建开放 API 后端转发响应。
   *
   * @param status HTTP 响应状态码
   * @param contentType 响应内容类型
   * @param body 响应体
   */
  public OpenApiBackendResponse(int status, String contentType, String body) {
    this(status, contentType, body, Map.of());
  }

  /**
   * 创建开放 API 后端转发响应。
   *
   * @param status HTTP 响应状态码
   * @param contentType 响应内容类型
   * @param body 响应体
   * @param headers 允许回传给开放 API 调用方的响应头
   */
  public OpenApiBackendResponse(int status, String contentType, String body, Map<String, String> headers) {
    this.status = status;
    this.contentType = contentType;
    this.body = body;
    this.headers = Map.copyOf(Objects.requireNonNull(headers, "响应头不能为空"));
  }

  public int getStatus() {
    return status;
  }

  public String getContentType() {
    return contentType;
  }

  public String getBody() {
    return body;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }
}
