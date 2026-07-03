/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime;

import com.zhyc.ai.app.domain.AiApp;
import com.zhyc.ai.app.repository.AiAppRepository;
import com.zhyc.ai.audit.domain.AiInvocationAudit;
import com.zhyc.ai.audit.repository.AiInvocationAuditRepository;
import com.zhyc.ai.model.domain.AiModelConfig;
import com.zhyc.ai.model.repository.AiModelConfigRepository;
import com.zhyc.ai.prompt.domain.AiPromptTemplate;
import com.zhyc.ai.prompt.repository.AiPromptTemplateRepository;
import com.zhyc.ai.provider.domain.AiProvider;
import com.zhyc.ai.provider.repository.AiProviderRepository;
import com.zhyc.ai.runtime.service.AiRuntimeChatCommand;
import com.zhyc.ai.runtime.service.AiRuntimeChatResponse;
import com.zhyc.ai.runtime.service.DefaultAiRuntimeService;
import com.zhyc.ai.runtime.spi.AiChatClient;
import com.zhyc.ai.runtime.spi.AiChatRequest;
import com.zhyc.ai.runtime.spi.AiChatResponse;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretReference;
import com.zhyc.common.secret.SecretResolver;
import com.zhyc.common.tenant.TenantContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 运行时调用服务测试。
 */
class AiRuntimeServiceTest {

    @Test
    void shouldRenderPromptCallModelAndRecordAudit() {
        RuntimeFixture fixture = RuntimeFixture.enabled();
        fixture.chatClient.nextResponse = new AiChatResponse("采购申请摘要", 12, 8, 20, "trace-001");

        AiRuntimeChatResponse response = fixture.service.chat(new AiRuntimeChatCommand("tenant-a",
                "purchase-assistant", "summary", "v1", Map.of("content", "采购电脑 3 台"), false));

        assertEquals("采购申请摘要", response.content());
        assertEquals("purchase-assistant", response.appCode());
        assertEquals("deepseek-chat", response.modelCode());
        assertEquals("openai-main", response.providerCode());
        assertEquals(20, response.totalTokens());
        assertEquals("你是采购助手", fixture.chatClient.lastRequest.systemPrompt());
        assertEquals("请总结：采购电脑 3 台", fixture.chatClient.lastRequest.userPrompt());
        assertEquals("api-key-value", fixture.chatClient.lastApiKey);
        assertEquals(1, fixture.auditRepository.rows.size());
        AiInvocationAudit audit = fixture.auditRepository.rows.get(0);
        assertEquals("tenant-a", audit.getTenantId());
        assertEquals("purchase-assistant", audit.getAppCode());
        assertEquals(10L, audit.getProviderId());
        assertEquals(20L, audit.getModelId());
        assertEquals("success", audit.getStatus());
        assertEquals("trace-001", audit.getTraceId());
        assertEquals("tenant-a", fixture.secretResolver.tenantIdDuringResolve);
        assertEquals("ai-main", fixture.secretResolver.secretCodeDuringResolve);
        assertEquals(null, TenantContext.getTenantId());
    }

    @Test
    void shouldRejectDisabledAiAppBeforeCallingProvider() {
        RuntimeFixture fixture = RuntimeFixture.enabled();
        fixture.appRepository.rows.clear();
        fixture.appRepository.rows.add(new AiApp(30L, "tenant-a", "purchase-assistant", "采购助手", 20L,
                "你是采购助手", 1000, "disabled", LocalDateTime.now(), LocalDateTime.now()));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> fixture.service.chat(new AiRuntimeChatCommand("tenant-a", "purchase-assistant",
                        "summary", "v1", Map.of("content", "采购电脑 3 台"), false)));

        assertEquals("ZHYC_AI_RUNTIME_APP_DISABLED", exception.getCode());
        assertEquals(0, fixture.chatClient.callCount);
        assertEquals(0, fixture.auditRepository.rows.size());
    }

    private static final class RuntimeFixture {

        private final RecordingAiAppRepository appRepository = new RecordingAiAppRepository();
        private final RecordingAiModelConfigRepository modelRepository = new RecordingAiModelConfigRepository();
        private final RecordingAiProviderRepository providerRepository = new RecordingAiProviderRepository();
        private final RecordingAiPromptTemplateRepository promptRepository = new RecordingAiPromptTemplateRepository();
        private final RecordingAiInvocationAuditRepository auditRepository = new RecordingAiInvocationAuditRepository();
        private final RecordingAiChatClient chatClient = new RecordingAiChatClient();
        private final TenantAwareSecretResolver secretResolver = new TenantAwareSecretResolver();
        private final DefaultAiRuntimeService service = new DefaultAiRuntimeService(appRepository, modelRepository,
                providerRepository, promptRepository, auditRepository, List.of(chatClient), secretResolver);

        private static RuntimeFixture enabled() {
            RuntimeFixture fixture = new RuntimeFixture();
            fixture.providerRepository.rows.add(new AiProvider(10L, "tenant-a", "openai-main", "OpenAI 兼容服务",
                    "openai_compatible", "https://ai.example.com/v1", "secret:ai-main", "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
            fixture.modelRepository.rows.add(new AiModelConfig(20L, "tenant-a", 10L, "deepseek-chat",
                    "DeepSeek Chat", "chat", 64000, false, false, "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
            fixture.appRepository.rows.add(new AiApp(30L, "tenant-a", "purchase-assistant", "采购助手", 20L,
                    "你是采购助手", 1000, "enabled", LocalDateTime.now(), LocalDateTime.now()));
            fixture.promptRepository.rows.add(new AiPromptTemplate(40L, "tenant-a", "summary", "摘要生成",
                    "v1", "请总结：{{content}}", "content", "published",
                    LocalDateTime.now(), LocalDateTime.now()));
            return fixture;
        }
    }

    private static final class RecordingAiAppRepository implements AiAppRepository {
        private final List<AiApp> rows = new ArrayList<>();

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
            rows.add(app);
        }
    }

    private static final class RecordingAiModelConfigRepository implements AiModelConfigRepository {
        private final List<AiModelConfig> rows = new ArrayList<>();

        @Override
        public List<AiModelConfig> findByTenantId(String tenantId) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId())).toList();
        }

        @Override
        public Optional<AiModelConfig> findByTenantIdAndId(String tenantId, Long id) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId()) && id.equals(row.getId()))
                    .findFirst();
        }

        @Override
        public void save(AiModelConfig modelConfig) {
            rows.add(modelConfig);
        }
    }

    private static final class RecordingAiProviderRepository implements AiProviderRepository {
        private final List<AiProvider> rows = new ArrayList<>();

        @Override
        public List<AiProvider> findByTenantId(String tenantId) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId())).toList();
        }

        @Override
        public Optional<AiProvider> findByTenantIdAndId(String tenantId, Long id) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId()) && id.equals(row.getId()))
                    .findFirst();
        }

        @Override
        public void save(AiProvider provider) {
            rows.add(provider);
        }
    }

    private static final class RecordingAiPromptTemplateRepository implements AiPromptTemplateRepository {
        private final List<AiPromptTemplate> rows = new ArrayList<>();

        @Override
        public List<AiPromptTemplate> findByTenantId(String tenantId) {
            return rows.stream().filter(row -> tenantId.equals(row.getTenantId())).toList();
        }

        @Override
        public Optional<AiPromptTemplate> findByTenantIdAndPromptCodeAndVersion(String tenantId, String promptCode,
                                                                                String version) {
            return rows.stream()
                    .filter(row -> tenantId.equals(row.getTenantId()) && promptCode.equals(row.getPromptCode())
                            && version.equals(row.getVersion()))
                    .findFirst();
        }

        @Override
        public void save(AiPromptTemplate template) {
            rows.add(template);
        }
    }

    private static final class RecordingAiInvocationAuditRepository implements AiInvocationAuditRepository {
        private final List<AiInvocationAudit> rows = new ArrayList<>();

        @Override
        public List<AiInvocationAudit> findByTenantIdAndAppCode(String tenantId, String appCode) {
            return rows.stream()
                    .filter(row -> tenantId.equals(row.getTenantId()) && appCode.equals(row.getAppCode()))
                    .toList();
        }

        @Override
        public void save(AiInvocationAudit audit) {
            rows.add(audit);
        }
    }

    private static final class RecordingAiChatClient implements AiChatClient {
        private AiChatRequest lastRequest;
        private String lastApiKey;
        private int callCount;
        private AiChatResponse nextResponse = new AiChatResponse("ok", 1, 1, 2, "trace");

        @Override
        public boolean supports(String providerType) {
            return "openai_compatible".equals(providerType);
        }

        @Override
        public AiChatResponse chat(AiChatRequest request, String apiKey) {
            this.lastRequest = request;
            this.lastApiKey = apiKey;
            this.callCount++;
            return nextResponse;
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
