/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider;

import com.zhyc.ai.provider.service.AiProviderConnectivityClient;
import com.zhyc.ai.provider.service.AiProviderTestCommand;
import com.zhyc.ai.provider.service.AiProviderTestResponse;
import com.zhyc.ai.provider.service.AiProviderTestService;
import com.zhyc.ai.provider.service.DefaultAiProviderTestService;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretReference;
import com.zhyc.common.secret.SecretResolver;
import com.zhyc.common.tenant.TenantContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AI 供应商可用性测试服务测试。
 */
class AiProviderTestServiceTest {

    /**
     * 验证供应商测试会解析密钥并调用对应类型的连通性客户端。
     */
    @Test
    void shouldResolveSecretAndTestProviderConnectivity() {
        RecordingConnectivityClient client = new RecordingConnectivityClient();
        AiProviderTestService service = new DefaultAiProviderTestService(List.of(client), new FixedSecretResolver());

        AiProviderTestResponse response = service.test(new AiProviderTestCommand(" tenant_a ", " openai-main ",
                " openai_compatible ", " https://api.example.com/v1 ", " secret:openai-main "));

        assertEquals(true, response.success());
        assertEquals("openai-main", response.providerCode());
        assertEquals("供应商可用", response.message());
        assertEquals("openai_compatible", client.lastProviderType);
        assertEquals("openai-main", client.lastProviderCode);
        assertEquals("secret:openai-main", client.lastSecretRef);
        assertEquals("https://api.example.com/v1", client.lastBaseUrl);
        assertEquals("api-key-value", client.lastApiKey);
    }

    /**
     * 验证测试供应商解析密钥前会绑定租户上下文，确保系统密钥中心可按租户读取 API 密钥。
     */
    @Test
    void shouldBindTenantContextBeforeResolvingSecret() {
        RecordingConnectivityClient client = new RecordingConnectivityClient();
        TenantAwareSecretResolver resolver = new TenantAwareSecretResolver();
        AiProviderTestService service = new DefaultAiProviderTestService(List.of(client), resolver);

        AiProviderTestResponse response = service.test(new AiProviderTestCommand(" tenant_a ", " deepseek ",
                " openai_compatible ", " https://api.deepseek.com ", " secret:deepseek "));

        assertEquals(true, response.success());
        assertEquals("tenant_a", resolver.tenantIdDuringResolve);
        assertEquals("deepseek", resolver.secretCodeDuringResolve);
        assertEquals(null, TenantContext.getTenantId());
    }

    /**
     * 验证供应商连通性异常会转为失败结果，便于前端展示。
     */
    @Test
    void shouldReturnFailedResultWhenConnectivityFails() {
        RecordingConnectivityClient client = new RecordingConnectivityClient();
        client.nextFailure = new BusinessException("ZHYC_AI_PROVIDER_TEST_HTTP_FAILED", "供应商接口调用失败");
        AiProviderTestService service = new DefaultAiProviderTestService(List.of(client), new FixedSecretResolver());

        AiProviderTestResponse response = service.test(new AiProviderTestCommand("tenant_a", "openai-main",
                "openai_compatible", "https://api.example.com/v1", "secret:openai-main"));

        assertEquals(false, response.success());
        assertEquals("供应商接口调用失败", response.message());
    }

    private static final class RecordingConnectivityClient implements AiProviderConnectivityClient {
        private String lastProviderType;
        private String lastProviderCode;
        private String lastSecretRef;
        private String lastBaseUrl;
        private String lastApiKey;
        private RuntimeException nextFailure;

        @Override
        public boolean supports(String providerType) {
            return "openai_compatible".equals(providerType);
        }

        @Override
        public void test(String providerType, String providerCode, String secretRef, String baseUrl, String apiKey) {
            this.lastProviderType = providerType;
            this.lastProviderCode = providerCode;
            this.lastSecretRef = secretRef;
            this.lastBaseUrl = baseUrl;
            this.lastApiKey = apiKey;
            if (nextFailure != null) {
                throw nextFailure;
            }
        }
    }

    private static final class FixedSecretResolver implements SecretResolver {
        @Override
        public String resolve(String code) {
            return "api-key-value";
        }

        @Override
        public String resolve(SecretReference reference) {
            return "api-key-value";
        }
    }

    private static final class TenantAwareSecretResolver implements SecretResolver {
        private String tenantIdDuringResolve;
        private String secretCodeDuringResolve;

        @Override
        public String resolve(String code) {
            this.tenantIdDuringResolve = TenantContext.getTenantId();
            this.secretCodeDuringResolve = code;
            if (tenantIdDuringResolve == null) {
                throw new BusinessException("ZHYC_SYSTEM_SECRET_TENANT_CONTEXT_REQUIRED",
                        "解析密钥前必须绑定租户上下文");
            }
            return "api-key-value";
        }

        @Override
        public String resolve(SecretReference reference) {
            return resolve(reference.getCode());
        }
    }
}
