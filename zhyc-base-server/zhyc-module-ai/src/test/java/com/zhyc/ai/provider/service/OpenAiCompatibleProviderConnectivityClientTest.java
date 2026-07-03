/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OpenAI 兼容供应商连通性客户端测试。
 */
class OpenAiCompatibleProviderConnectivityClientTest {

    /**
     * 验证密钥明文即使包含 Bearer 前缀，也不会生成重复 Authorization 前缀。
     *
     * @throws Exception 本地测试服务启动失败时抛出
     */
    @Test
    void shouldStripBearerPrefixFromApiKeyBeforeSendingAuthorizationHeader() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = startServer(200, authorization);
        try {
            OpenAiCompatibleProviderConnectivityClient client = new OpenAiCompatibleProviderConnectivityClient();

            client.test("deepseek", "deepseek", "secret:deepseek", baseUrl(server), " Bearer sk-test-key ");

            assertEquals("Bearer sk-test-key", authorization.get());
        } finally {
            server.stop(0);
        }
    }

    /**
     * 验证供应商返回 401 时给出明确鉴权提示，方便用户修正 API Key 配置。
     *
     * @throws Exception 本地测试服务启动失败时抛出
     */
    @Test
    void shouldExplainApiKeyProblemWhenProviderReturnsUnauthorized() throws Exception {
        HttpServer server = startServer(401, new AtomicReference<>());
        try {
            OpenAiCompatibleProviderConnectivityClient client = new OpenAiCompatibleProviderConnectivityClient();

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> client.test("deepseek", "deepseek", "secret:deepseek", baseUrl(server), "sk-wrong-key"));

            assertEquals("ZHYC_AI_PROVIDER_TEST_HTTP_FAILED", exception.getCode());
            assertTrue(exception.getMessage().contains("HTTP 状态码: 401"));
            assertTrue(exception.getMessage().contains("API Key"));
            assertTrue(exception.getMessage().contains("Bearer"));
        } finally {
            server.stop(0);
        }
    }

    /**
     * 验证 DeepSeek 密钥为占位值时直接在本地拦截，避免外部 401 掩盖真实配置问题。
     */
    @Test
    void shouldRejectPlaceholderDeepSeekApiKeyBeforeSendingRequest() {
        OpenAiCompatibleProviderConnectivityClient client = new OpenAiCompatibleProviderConnectivityClient();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> client.test("deepseek", "deepseek", "secret:deepseek", "https://api.deepseek.com", "deepseek"));

        assertEquals("ZHYC_AI_PROVIDER_TEST_API_KEY_FORMAT", exception.getCode());
        assertTrue(exception.getMessage().contains("secret:deepseek"));
        assertTrue(exception.getMessage().contains("sk-"));
    }

    /**
     * 启动本地 HTTP 服务，记录 Authorization 请求头。
     *
     * @param statusCode 响应状态码
     * @param authorization 请求头记录容器
     * @return 本地 HTTP 服务
     * @throws IOException 启动失败时抛出
     */
    private static HttpServer startServer(int statusCode, AtomicReference<String> authorization) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/models", exchange -> respond(exchange, statusCode, authorization));
        server.start();
        return server;
    }

    /**
     * 返回测试响应并记录鉴权请求头。
     *
     * @param exchange HTTP 交换对象
     * @param statusCode 响应状态码
     * @param authorization 请求头记录容器
     * @throws IOException 响应失败时抛出
     */
    private static void respond(HttpExchange exchange, int statusCode, AtomicReference<String> authorization)
            throws IOException {
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    /**
     * 生成本地服务基础地址。
     *
     * @param server 本地 HTTP 服务
     * @return 基础地址
     */
    private static String baseUrl(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }
}
