/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.debug.service.DefaultOpenApiDebugService;
import com.zhyc.openapi.debug.service.OpenApiDebugCommand;
import com.zhyc.openapi.debug.service.OpenApiDebugGatewayClient;
import com.zhyc.openapi.debug.service.OpenApiDebugGatewayRequest;
import com.zhyc.openapi.debug.service.OpenApiDebugGatewayResponse;
import com.zhyc.openapi.debug.service.OpenApiDebugResponse;
import org.junit.jupiter.api.Test;

/**
 * 开放 API 调试代理业务服务测试。
 */
class OpenApiDebugServiceTest {

    /**
     * 验证 API Key 调试请求会携带租户、签名和追踪请求头进入网关客户端。
     */
    @Test
    void shouldInvokeGatewayWithApiKeyHeaders() {
        RecordingOpenApiDebugGatewayClient gatewayClient = new RecordingOpenApiDebugGatewayClient();
        DefaultOpenApiDebugService service = new DefaultOpenApiDebugService(gatewayClient);

        OpenApiDebugResponse response = service.invoke(apiKeyCommand());

        assertEquals("debug-request-1", response.getRequestId());
        assertEquals("purchase-purchaseOrder", response.getApiCode());
        assertEquals(200, response.getHttpStatus());
        assertTrue(response.isSuccess());
        assertEquals("{\"ok\":true}", response.getResponseBody());
        assertEquals("tenant_a", gatewayClient.recorded.getTenantId());
        assertEquals("GET", gatewayClient.recorded.getMethod());
        assertEquals("/openapi/v1/purchase/purchaseOrder", gatewayClient.recorded.getPath());
        assertEquals("access-key", gatewayClient.recorded.getHeaders().get("X-ZHYC-Access-Key"));
        assertEquals("1700000000000", gatewayClient.recorded.getHeaders().get("X-ZHYC-Timestamp"));
        assertEquals("nonce-1", gatewayClient.recorded.getHeaders().get("X-ZHYC-Nonce"));
        assertEquals("signature-1", gatewayClient.recorded.getHeaders().get("X-ZHYC-Signature"));
        assertEquals("4d4bbe59c6aad22442cde199a6a8a5f034405fcd78fb5a81c24ef249de1c45f1",
                gatewayClient.recorded.getHeaders().get("X-ZHYC-Body-SHA256"));
        assertEquals("debug-request-1", gatewayClient.recorded.getHeaders().get("X-ZHYC-Request-Id"));
        assertEquals("tenant_a", gatewayClient.recorded.getHeaders().get("X-ZHYC-Tenant-Id"));
    }

    /**
     * 验证 OAuth2/OIDC 调试请求会携带 Bearer Token 进入网关客户端。
     */
    @Test
    void shouldInvokeGatewayWithOAuth2Header() {
        RecordingOpenApiDebugGatewayClient gatewayClient = new RecordingOpenApiDebugGatewayClient();
        DefaultOpenApiDebugService service = new DefaultOpenApiDebugService(gatewayClient);

        service.invoke(new OpenApiDebugCommand("tenant_a", "purchase-purchaseOrder", "GET",
                "/openapi/v1/purchase/purchaseOrder", "OAUTH2", null, null, null, null,
                "access-token", "debug-request-2", ""));

        assertEquals("Bearer access-token", gatewayClient.recorded.getHeaders().get("Authorization"));
        assertEquals("tenant_a", gatewayClient.recorded.getHeaders().get("X-ZHYC-Tenant-Id"));
        assertEquals("debug-request-2", gatewayClient.recorded.getHeaders().get("X-ZHYC-Request-Id"));
    }

    /**
     * 验证调试代理拒绝非开放 API 网关路径，避免后台代理变成任意 URL 转发器。
     */
    @Test
    void shouldRejectNonOpenApiPath() {
        DefaultOpenApiDebugService service = new DefaultOpenApiDebugService(new RecordingOpenApiDebugGatewayClient());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.invoke(new OpenApiDebugCommand("tenant_a", "purchase-purchaseOrder", "GET",
                        "http://example.com/openapi/v1/purchase/purchaseOrder", "API_KEY", "access-key",
                        "1700000000000", "nonce-1", "signature-1", null, "debug-request-1", "")));

        assertEquals("ZHYC_OPENAPI_DEBUG_PATH_INVALID", exception.getCode());
        assertEquals("开放 API 调试路径必须以 /openapi/ 开头", exception.getMessage());
    }

    /**
     * 验证调试代理拒绝不受支持的 HTTP 方法，避免非法方法进入 JDK HTTP 客户端或网关权限匹配。
     */
    @Test
    void shouldRejectUnsupportedHttpMethod() {
        DefaultOpenApiDebugService service = new DefaultOpenApiDebugService(new RecordingOpenApiDebugGatewayClient());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.invoke(new OpenApiDebugCommand("tenant_a", "purchase-purchaseOrder", "TRACE",
                        "/openapi/v1/purchase/purchaseOrder", "API_KEY", "access-key",
                        "1700000000000", "nonce-1", "signature-1", null, "debug-request-1", "")));

        assertEquals("ZHYC_OPENAPI_DEBUG_METHOD_UNSUPPORTED", exception.getCode());
        assertEquals("开放 API 调试 HTTP 方法不受支持", exception.getMessage());
    }

    private OpenApiDebugCommand apiKeyCommand() {
        return new OpenApiDebugCommand("tenant_a", "purchase-purchaseOrder", "GET",
                "/openapi/v1/purchase/purchaseOrder", "API_KEY", "access-key", "1700000000000",
                "nonce-1", "signature-1", null, "debug-request-1", "{\"amount\":100}");
    }

    /**
     * 记录调试网关调用的测试客户端。
     */
    private static class RecordingOpenApiDebugGatewayClient implements OpenApiDebugGatewayClient {

        /** 最近一次调试网关请求。 */
        private OpenApiDebugGatewayRequest recorded;

        @Override
        public OpenApiDebugGatewayResponse invoke(OpenApiDebugGatewayRequest request) {
            this.recorded = request;
            return new OpenApiDebugGatewayResponse(200, true, null, 12L, "{\"ok\":true}");
        }
    }
}
