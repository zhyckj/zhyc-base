/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * RestClient 开放 API 后端调用器测试。
 */
class RestClientOpenApiBackendInvokerTest {

  /**
   * 验证后端调用器会将开放 API 查询参数追加到后端路由地址。
   */
  @Test
  void shouldAppendQueryStringToBackendRouteWhenInvokeBackend() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    RestClientOpenApiBackendInvoker invoker = new RestClientOpenApiBackendInvoker(builder.build());
    server.expect(requestTo("http://purchase-service/internal/purchase/requests?status=pending&page=1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("accepted", MediaType.TEXT_PLAIN));

    OpenApiBackendResponse response = invoker.invoke(
        new OpenApiRoute("purchase.request.list", "http://purchase-service/internal/purchase/requests"),
        new OpenApiBackendRequest("GET", "", null, "status=pending&page=1"));

    assertEquals(200, response.getStatus());
    assertEquals("accepted", response.getBody());
    server.verify();
  }

  /**
   * 验证后端调用器只提取允许外显的响应头。
   */
  @Test
  void shouldExtractAllowedBackendResponseHeaders() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    RestClientOpenApiBackendInvoker invoker = new RestClientOpenApiBackendInvoker(builder.build());
    server.expect(requestTo("http://purchase-service/internal/purchase/requests"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("accepted", MediaType.APPLICATION_JSON)
            .header("X-ZHYC-Request-Id", "req-001")
            .header("Content-Disposition", "attachment; filename=result.json")
            .header("X-ZHYC-Internal-Token", "internal-secret")
            .header("Set-Cookie", "SESSION=unsafe"));

    OpenApiBackendResponse response = invoker.invoke(
        new OpenApiRoute("purchase.request.create", "http://purchase-service/internal/purchase/requests"),
        new OpenApiBackendRequest("POST", "{}", MediaType.APPLICATION_JSON_VALUE));

    assertEquals("req-001", response.getHeaders().get("X-ZHYC-Request-Id"));
    assertEquals("attachment; filename=result.json", response.getHeaders().get("Content-Disposition"));
    assertEquals(null, response.getHeaders().get("X-ZHYC-Internal-Token"));
    assertEquals(null, response.getHeaders().get("Set-Cookie"));
    server.verify();
  }
}
