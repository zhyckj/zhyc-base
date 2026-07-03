/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 RestClient 的开放 API 后端服务调用器。
 */
public class RestClientOpenApiBackendInvoker implements OpenApiBackendInvoker {

  /** REST 客户端。 */
  private final RestClient restClient;

  /**
   * 创建 RestClient 后端服务调用器。
   *
   * @param restClient REST 客户端
   */
  public RestClientOpenApiBackendInvoker(RestClient restClient) {
    this.restClient = Objects.requireNonNull(restClient, "REST 客户端不能为空");
  }

  /**
   * 调用开放 API 对应的后端服务。
   *
   * @param route 开放 API 运行态路由
   * @param request 后端转发请求
   * @return 后端服务响应
   */
  @Override
  public OpenApiBackendResponse invoke(OpenApiRoute route, OpenApiBackendRequest request) {
    RestClient.RequestBodySpec spec = restClient.method(HttpMethod.valueOf(request.getHttpMethod()))
        .uri(buildBackendUri(route, request));
    request.getHeaders().forEach(spec::header);
    if (request.getContentType() != null && !request.getContentType().isBlank()) {
      spec.contentType(MediaType.parseMediaType(request.getContentType()));
    }
    return spec.body(request.getBody() == null ? "" : request.getBody())
        .exchange((clientRequest, clientResponse) -> new OpenApiBackendResponse(
            clientResponse.getStatusCode().value(),
            clientResponse.getHeaders().getContentType() == null
                ? MediaType.APPLICATION_JSON_VALUE
                : clientResponse.getHeaders().getContentType().toString(),
            new String(clientResponse.getBody().readAllBytes()),
            extractAllowedResponseHeaders(clientResponse.getHeaders())));
  }

  /**
   * 构造后端目标地址。
   *
   * <p>开放 API 查询参数必须原样转发给业务后端，避免列表、分页和筛选类接口丢失参数。</p>
   *
   * @param route 开放 API 运行态路由
   * @param request 后端转发请求
   * @return 带查询字符串的后端地址
   */
  private String buildBackendUri(OpenApiRoute route, OpenApiBackendRequest request) {
    String queryString = request.getQueryString();
    if (queryString == null || queryString.isBlank()) {
      return route.getBackendRoute();
    }
    return route.getBackendRoute() + (route.getBackendRoute().contains("?") ? "&" : "?") + queryString;
  }

  /**
   * 提取允许外显给开放 API 调用方的后端响应头。
   *
   * <p>仅回传平台追踪、下载和缓存类响应头，避免 Cookie、连接管理和内部网关头泄露到外部调用方。</p>
   *
   * @param headers 后端响应头
   * @return 允许外显的响应头
   */
  private Map<String, String> extractAllowedResponseHeaders(HttpHeaders headers) {
    Map<String, String> allowedHeaders = new LinkedHashMap<>();
    headers.forEach((headerName, values) -> {
      if (OpenApiResponseHeaderPolicy.isAllowedResponseHeader(headerName) && values != null && !values.isEmpty()) {
        allowedHeaders.put(headerName, values.get(0));
      }
    });
    return allowedHeaders;
  }
}
