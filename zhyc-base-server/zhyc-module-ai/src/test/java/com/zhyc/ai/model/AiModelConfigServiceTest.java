/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model;

import com.zhyc.ai.model.domain.AiModelConfig;
import com.zhyc.ai.model.repository.AiModelConfigRepository;
import com.zhyc.ai.model.service.AiModelConfigSaveCommand;
import com.zhyc.ai.model.service.AiModelConfigService;
import com.zhyc.ai.model.service.DefaultAiModelConfigService;
import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 模型配置业务服务测试。
 */
class AiModelConfigServiceTest {

    /**
     * 验证模型配置保存时保留流式和工具调用能力标识。
     */
    @Test
    void shouldSaveModelConfigWithCapabilities() {
        RecordingAiModelConfigRepository repository = new RecordingAiModelConfigRepository();
        AiModelConfigService service = new DefaultAiModelConfigService(repository);

        service.save(new AiModelConfigSaveCommand(" tenant_a ", 10L, " gpt-main ", " gpt-4.1 ",
                " chat ", 128000, true, true, " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals(10L, repository.lastSaved.getProviderId());
        assertEquals("gpt-main", repository.lastSaved.getModelCode());
        assertEquals("gpt-4.1", repository.lastSaved.getModelName());
        assertEquals("chat", repository.lastSaved.getModelType());
        assertEquals(128000, repository.lastSaved.getContextWindow());
        assertEquals(true, repository.lastSaved.isSupportStream());
        assertEquals(true, repository.lastSaved.isSupportTool());
    }

    /**
     * 验证模型上下文长度必须大于 0。
     */
    @Test
    void shouldRejectNonPositiveContextWindow() {
        RecordingAiModelConfigRepository repository = new RecordingAiModelConfigRepository();
        AiModelConfigService service = new DefaultAiModelConfigService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new AiModelConfigSaveCommand("tenant_a", 10L, "gpt-main",
                        "gpt-4.1", "chat", 0, true, true, "enabled")));

        assertEquals("ZHYC_AI_MODEL_CONTEXT_WINDOW_INVALID", exception.getCode());
        assertEquals("模型上下文长度必须大于 0", exception.getMessage());
    }

    /**
     * 验证模型配置列表会返回模型主键和供应商主键，供前端应用绑定和编辑态回填。
     */
    @Test
    void shouldListModelsWithIdAndProviderId() {
        RecordingAiModelConfigRepository repository = new RecordingAiModelConfigRepository();
        AiModelConfigService service = new DefaultAiModelConfigService(repository);

        var models = service.listModels(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, models.size());
        assertEquals(1L, models.get(0).getId());
        assertEquals(10L, models.get(0).getProviderId());
    }

    private static class RecordingAiModelConfigRepository implements AiModelConfigRepository {

        /** 最近一次保存记录。 */
        private AiModelConfig lastSaved;
        /** 最近一次查询租户。 */
        private String lastTenantId;

        @Override
        public List<AiModelConfig> findByTenantId(String tenantId) {
            this.lastTenantId = tenantId;
            return List.of(new AiModelConfig(1L, tenantId, 10L, "gpt-main", "GPT-4.1",
                    "chat", 8192, true, false, "enabled", null, null));
        }

        @Override
        public java.util.Optional<AiModelConfig> findByTenantIdAndId(String tenantId, Long id) {
            return java.util.Optional.empty();
        }

        @Override
        public void save(AiModelConfig modelConfig) {
            this.lastSaved = modelConfig;
        }
    }
}
