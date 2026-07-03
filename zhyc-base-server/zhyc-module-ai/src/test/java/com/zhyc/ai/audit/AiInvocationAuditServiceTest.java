/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit;

import com.zhyc.ai.audit.domain.AiInvocationAudit;
import com.zhyc.ai.audit.repository.AiInvocationAuditRepository;
import com.zhyc.ai.audit.service.AiInvocationAuditRecordCommand;
import com.zhyc.ai.audit.service.AiInvocationAuditResponse;
import com.zhyc.ai.audit.service.DefaultAiInvocationAuditService;
import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 调用审计服务测试。
 */
class AiInvocationAuditServiceTest {

    @Test
    void shouldRecordInvocationAudit() {
        RecordingAiInvocationAuditRepository repository = new RecordingAiInvocationAuditRepository();
        DefaultAiInvocationAuditService service = new DefaultAiInvocationAuditService(repository);

        service.record(new AiInvocationAuditRecordCommand("tenant-a", "assistant", 11L, 22L, "chat",
                300, 120, 420, 680L, "success", null, "trace-001"));

        AiInvocationAudit saved = repository.saved.get(0);
        assertEquals("tenant-a", saved.getTenantId());
        assertEquals("assistant", saved.getAppCode());
        assertEquals(11L, saved.getProviderId());
        assertEquals(22L, saved.getModelId());
        assertEquals("chat", saved.getInvocationType());
        assertEquals(300, saved.getPromptTokens());
        assertEquals(120, saved.getCompletionTokens());
        assertEquals(420, saved.getTotalTokens());
        assertEquals(680L, saved.getLatencyMs());
        assertEquals("success", saved.getStatus());
        assertEquals("trace-001", saved.getTraceId());
    }

    @Test
    void shouldListAuditByTenantAndApp() {
        RecordingAiInvocationAuditRepository repository = new RecordingAiInvocationAuditRepository();
        repository.rows.add(new AiInvocationAudit(1L, "tenant-a", "assistant", 11L, 22L, "chat",
                1, 2, 3, 100L, "success", null, "trace-001", LocalDateTime.now()));
        DefaultAiInvocationAuditService service = new DefaultAiInvocationAuditService(repository);

        List<AiInvocationAuditResponse> responses = service.listAudits("tenant-a", "assistant");

        assertEquals(1, responses.size());
        assertEquals(3, responses.get(0).totalTokens());
    }

    @Test
    void shouldRejectNegativeTotalTokens() {
        DefaultAiInvocationAuditService service = new DefaultAiInvocationAuditService(new RecordingAiInvocationAuditRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new AiInvocationAuditRecordCommand("tenant-a", "assistant", 11L, 22L,
                        "chat", 1, 2, -1, 100L, "success", null, "trace-001")));

        assertEquals("ZHYC_AI_AUDIT_TOTAL_TOKENS_INVALID", exception.getCode());
        assertEquals("AI 调用总令牌数不能小于 0", exception.getMessage());
    }

    /**
     * AI 调用审计测试仓储。
     */
    private static final class RecordingAiInvocationAuditRepository implements AiInvocationAuditRepository {

        private final List<AiInvocationAudit> rows = new ArrayList<>();

        private final List<AiInvocationAudit> saved = new ArrayList<>();

        @Override
        public List<AiInvocationAudit> findByTenantIdAndAppCode(String tenantId, String appCode) {
            return rows.stream()
                    .filter(row -> tenantId.equals(row.getTenantId()) && appCode.equals(row.getAppCode()))
                    .toList();
        }

        @Override
        public void save(AiInvocationAudit audit) {
            saved.add(audit);
            rows.add(audit);
        }
    }
}
