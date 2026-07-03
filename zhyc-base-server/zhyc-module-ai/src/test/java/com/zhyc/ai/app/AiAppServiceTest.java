/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app;

import com.zhyc.ai.app.domain.AiApp;
import com.zhyc.ai.app.repository.AiAppRepository;
import com.zhyc.ai.app.service.AiAppResponse;
import com.zhyc.ai.app.service.AiAppSaveCommand;
import com.zhyc.ai.app.service.DefaultAiAppService;
import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 应用接入服务测试。
 */
class AiAppServiceTest {

    @Test
    void shouldSaveAiAppWithDefaultQuota() {
        RecordingAiAppRepository repository = new RecordingAiAppRepository();
        DefaultAiAppService service = new DefaultAiAppService(repository);

        service.save(new AiAppSaveCommand(" tenant-a ", "contract-assistant", "合同助手", 1001L,
                "你是合同审查助手", 5000, "enabled"));

        AiApp saved = repository.saved.get(0);
        assertEquals("tenant-a", saved.getTenantId());
        assertEquals("contract-assistant", saved.getAppCode());
        assertEquals("合同助手", saved.getAppName());
        assertEquals(1001L, saved.getDefaultModelId());
        assertEquals("你是合同审查助手", saved.getSystemPrompt());
        assertEquals(5000, saved.getDailyTokenQuota());
        assertEquals("enabled", saved.getStatus());
    }

    @Test
    void shouldListAppsByTenant() {
        RecordingAiAppRepository repository = new RecordingAiAppRepository();
        repository.rows.add(new AiApp(1L, "tenant-a", "assistant", "业务助手", 2L,
                "system", 1000, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        DefaultAiAppService service = new DefaultAiAppService(repository);

        List<AiAppResponse> responses = service.listApps("tenant-a");

        assertEquals(1, responses.size());
        assertEquals("assistant", responses.get(0).appCode());
    }

    @Test
    void shouldRejectInvalidDailyQuota() {
        DefaultAiAppService service = new DefaultAiAppService(new RecordingAiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new AiAppSaveCommand("tenant-a", "assistant", "业务助手", 2L,
                        "system", 0, "enabled")));

        assertEquals("ZHYC_AI_APP_DAILY_QUOTA_INVALID", exception.getCode());
        assertEquals("AI 应用每日令牌额度必须大于 0", exception.getMessage());
    }

    /**
     * AI 应用测试仓储。
     */
    private static final class RecordingAiAppRepository implements AiAppRepository {

        private final List<AiApp> rows = new ArrayList<>();

        private final List<AiApp> saved = new ArrayList<>();

        @Override
        public List<AiApp> findByTenantId(String tenantId) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId())).toList();
        }

        @Override
        public Optional<AiApp> findByTenantIdAndAppCode(String tenantId, String appCode) {
            return rows.stream()
                    .filter(row -> tenantId.equals(row.getTenantId()) && appCode.equals(row.getAppCode()))
                    .findFirst();
        }

        @Override
        public void save(AiApp app) {
            saved.add(app);
            rows.add(app);
        }
    }
}
