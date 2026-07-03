/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt;

import com.zhyc.ai.prompt.domain.AiPromptTemplate;
import com.zhyc.ai.prompt.repository.AiPromptTemplateRepository;
import com.zhyc.ai.prompt.service.AiPromptTemplateResponse;
import com.zhyc.ai.prompt.service.AiPromptTemplateSaveCommand;
import com.zhyc.ai.prompt.service.DefaultAiPromptTemplateService;
import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 提示词模板服务测试。
 */
class AiPromptTemplateServiceTest {

    @Test
    void shouldSavePromptTemplateVersion() {
        RecordingAiPromptTemplateRepository repository = new RecordingAiPromptTemplateRepository();
        DefaultAiPromptTemplateService service = new DefaultAiPromptTemplateService(repository);

        service.save(new AiPromptTemplateSaveCommand("tenant-a", "summary", "摘要生成", "v1",
                "请总结：{{content}}", "content", "draft"));

        AiPromptTemplate saved = repository.saved.get(0);
        assertEquals("tenant-a", saved.getTenantId());
        assertEquals("summary", saved.getPromptCode());
        assertEquals("摘要生成", saved.getPromptName());
        assertEquals("v1", saved.getVersion());
        assertEquals("请总结：{{content}}", saved.getTemplateContent());
        assertEquals("content", saved.getVariables());
        assertEquals("draft", saved.getStatus());
    }

    @Test
    void shouldListPromptTemplatesByTenant() {
        RecordingAiPromptTemplateRepository repository = new RecordingAiPromptTemplateRepository();
        repository.rows.add(new AiPromptTemplate(1L, "tenant-a", "summary", "摘要生成", "v1",
                "template", "content", "published", LocalDateTime.now(), LocalDateTime.now()));
        DefaultAiPromptTemplateService service = new DefaultAiPromptTemplateService(repository);

        List<AiPromptTemplateResponse> responses = service.listTemplates("tenant-a");

        assertEquals(1, responses.size());
        assertEquals("summary", responses.get(0).promptCode());
    }

    @Test
    void shouldRejectBlankTemplateContent() {
        DefaultAiPromptTemplateService service = new DefaultAiPromptTemplateService(new RecordingAiPromptTemplateRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new AiPromptTemplateSaveCommand("tenant-a", "summary", "摘要生成",
                        "v1", " ", "content", "draft")));

        assertEquals("ZHYC_AI_PROMPT_TEMPLATE_CONTENT_REQUIRED", exception.getCode());
        assertEquals("AI 提示词模板内容不能为空", exception.getMessage());
    }

    /**
     * AI 提示词模板测试仓储。
     */
    private static final class RecordingAiPromptTemplateRepository implements AiPromptTemplateRepository {

        private final List<AiPromptTemplate> rows = new ArrayList<>();

        private final List<AiPromptTemplate> saved = new ArrayList<>();

        @Override
        public List<AiPromptTemplate> findByTenantId(String tenantId) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId())).toList();
        }

        @Override
        public Optional<AiPromptTemplate> findByTenantIdAndPromptCodeAndVersion(String tenantId, String promptCode,
                                                                                String version) {
            return rows.stream()
                    .filter(row -> tenantId.equals(row.getTenantId())
                            && promptCode.equals(row.getPromptCode())
                            && version.equals(row.getVersion()))
                    .findFirst();
        }

        @Override
        public void save(AiPromptTemplate template) {
            saved.add(template);
            rows.add(template);
        }
    }
}
