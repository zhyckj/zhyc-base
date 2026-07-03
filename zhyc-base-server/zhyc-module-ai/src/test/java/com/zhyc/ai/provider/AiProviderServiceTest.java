/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider;

import com.zhyc.ai.provider.domain.AiProvider;
import com.zhyc.ai.provider.service.AiProviderResponse;
import com.zhyc.ai.provider.service.AiProviderSaveCommand;
import com.zhyc.ai.provider.service.AiProviderService;
import com.zhyc.ai.provider.service.DefaultAiProviderService;
import com.zhyc.ai.provider.repository.AiProviderRepository;
import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 模型供应商业务服务测试。
 */
class AiProviderServiceTest {

    /**
     * 验证保存模型供应商时会规范化基础字段并要求密钥引用。
     */
    @Test
    void shouldSaveProviderWithNormalizedFields() {
        RecordingAiProviderRepository repository = new RecordingAiProviderRepository();
        AiProviderService service = new DefaultAiProviderService(repository);

        service.save(new AiProviderSaveCommand(" tenant_a ", " openai-main ", " OpenAI 主模型 ",
                " openai_compatible ", " https://api.example.com/v1 ", " secret:openai-main ", " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("openai-main", repository.lastSaved.getProviderCode());
        assertEquals("OpenAI 主模型", repository.lastSaved.getProviderName());
        assertEquals("openai_compatible", repository.lastSaved.getProviderType());
        assertEquals("https://api.example.com/v1", repository.lastSaved.getBaseUrl());
        assertEquals("secret:openai-main", repository.lastSaved.getSecretRef());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证密钥引用必须使用密钥中心引用格式，避免模型密钥明文落库。
     */
    @Test
    void shouldRejectPlainSecretReference() {
        RecordingAiProviderRepository repository = new RecordingAiProviderRepository();
        AiProviderService service = new DefaultAiProviderService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new AiProviderSaveCommand("tenant_a", "openai-main", "OpenAI 主模型",
                        "openai_compatible", "https://api.example.com/v1", "sk-plain", "enabled")));

        assertEquals("ZHYC_AI_PROVIDER_SECRET_REF_INVALID", exception.getCode());
        assertEquals("模型供应商密钥必须使用 secret:<secretCode> 引用", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证可按租户查询模型供应商。
     */
    @Test
    void shouldListProvidersByTenant() {
        RecordingAiProviderRepository repository = new RecordingAiProviderRepository();
        AiProviderService service = new DefaultAiProviderService(repository);

        List<AiProviderResponse> providers = service.listProviders(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, providers.size());
        assertEquals(1L, providers.get(0).getId());
        assertEquals("openai-main", providers.get(0).getProviderCode());
        assertEquals("secret:openai-main", providers.get(0).getSecretRef());
    }

    /**
     * 测试用模型供应商仓储。
     */
    private static class RecordingAiProviderRepository implements AiProviderRepository {

        /** 最近一次查询租户。 */
        private String lastTenantId;
        /** 最近一次保存记录。 */
        private AiProvider lastSaved;

        @Override
        public List<AiProvider> findByTenantId(String tenantId) {
            this.lastTenantId = tenantId;
            return List.of(new AiProvider(1L, tenantId, "openai-main", "OpenAI 主模型",
                    "openai_compatible", "https://api.example.com/v1", "secret:openai-main", "enabled",
                    null, null));
        }

        @Override
        public java.util.Optional<AiProvider> findByTenantIdAndId(String tenantId, Long id) {
            return java.util.Optional.empty();
        }

        @Override
        public void save(AiProvider provider) {
            this.lastSaved = provider;
        }
    }
}
