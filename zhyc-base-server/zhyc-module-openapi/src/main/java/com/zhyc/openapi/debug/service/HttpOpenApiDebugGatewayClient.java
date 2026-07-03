/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 基于 JDK HttpClient 的开放 API 调试网关客户端。
 */
@Component
public class HttpOpenApiDebugGatewayClient implements OpenApiDebugGatewayClient {

    /** 默认调试请求超时时间。 */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    /** 开放 API 网关基础地址。 */
    private final String gatewayBaseUrl;
    /** JDK HTTP 客户端。 */
    private final HttpClient httpClient;

    /**
     * 创建开放 API 调试网关客户端。
     *
     * @param gatewayBaseUrl 开放 API 网关基础地址
     */
    @Autowired
    public HttpOpenApiDebugGatewayClient(
            @Value("${zhyc.openapi.gateway-url:http://127.0.0.1:9100}") String gatewayBaseUrl) {
        this(gatewayBaseUrl, HttpClient.newHttpClient());
    }

    /**
     * 创建开放 API 调试网关客户端。
     *
     * @param gatewayBaseUrl 开放 API 网关基础地址
     * @param httpClient JDK HTTP 客户端
     */
    HttpOpenApiDebugGatewayClient(String gatewayBaseUrl, HttpClient httpClient) {
        this.gatewayBaseUrl = gatewayBaseUrl.endsWith("/")
                ? gatewayBaseUrl.substring(0, gatewayBaseUrl.length() - 1)
                : gatewayBaseUrl;
        this.httpClient = httpClient;
    }

    /**
     * 调用开放 API 网关。
     *
     * @param request 调试网关请求
     * @return 调试网关响应
     */
    @Override
    public OpenApiDebugGatewayResponse invoke(OpenApiDebugGatewayRequest request) {
        long started = System.currentTimeMillis();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(gatewayBaseUrl + request.getPath()))
                    .timeout(DEFAULT_TIMEOUT)
                    .header("Content-Type", "application/json");
            request.getHeaders().forEach(builder::header);
            HttpRequest.BodyPublisher bodyPublisher = request.getBody() == null || request.getBody().isBlank()
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(request.getBody());
            HttpResponse<String> response = httpClient.send(builder.method(request.getMethod(), bodyPublisher).build(),
                    HttpResponse.BodyHandlers.ofString());
            return new OpenApiDebugGatewayResponse(response.statusCode(), response.statusCode() >= 200
                    && response.statusCode() < 300, response.statusCode() >= 200 && response.statusCode() < 300
                    ? null : "ZHYC_OPENAPI_DEBUG_GATEWAY_ERROR", System.currentTimeMillis() - started,
                    response.body());
        } catch (IOException exception) {
            return gatewayFailure(started, exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return gatewayFailure(started, "开放 API 调试请求被中断");
        }
    }

    /**
     * 构建网关不可达响应。
     *
     * @param started 调试调用开始时间戳
     * @param message 网关调用失败说明
     * @return 网关不可达响应
     */
    private OpenApiDebugGatewayResponse gatewayFailure(long started, String message) {
        return new OpenApiDebugGatewayResponse(502, false, "ZHYC_OPENAPI_DEBUG_GATEWAY_UNAVAILABLE",
                System.currentTimeMillis() - started, message == null ? "" : message);
    }
}
