/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.service;

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
import com.zhyc.ai.runtime.spi.AiChatClient;
import com.zhyc.ai.runtime.spi.AiChatRequest;
import com.zhyc.ai.runtime.spi.AiChatResponse;
import com.zhyc.ai.support.AiValidationSupport;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretReference;
import com.zhyc.common.secret.SecretResolver;
import com.zhyc.common.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 默认 AI 运行时统一调用服务。
 *
 * <p>负责按租户解析 AI 应用、默认模型、供应商和提示词模板，统一完成提示词渲染、模型调用和审计记录。</p>
 */
@Service
public class DefaultAiRuntimeService implements AiRuntimeService {

    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_RUNTIME_COMMAND_REQUIRED";
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_RUNTIME_TENANT_ID_REQUIRED";
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_AI_RUNTIME_APP_CODE_REQUIRED";
    private static final String ERROR_PROMPT_CODE_REQUIRED = "ZHYC_AI_RUNTIME_PROMPT_CODE_REQUIRED";
    private static final String ERROR_APP_NOT_FOUND = "ZHYC_AI_RUNTIME_APP_NOT_FOUND";
    private static final String ERROR_APP_DISABLED = "ZHYC_AI_RUNTIME_APP_DISABLED";
    private static final String ERROR_MODEL_NOT_FOUND = "ZHYC_AI_RUNTIME_MODEL_NOT_FOUND";
    private static final String ERROR_MODEL_DISABLED = "ZHYC_AI_RUNTIME_MODEL_DISABLED";
    private static final String ERROR_PROVIDER_NOT_FOUND = "ZHYC_AI_RUNTIME_PROVIDER_NOT_FOUND";
    private static final String ERROR_PROVIDER_DISABLED = "ZHYC_AI_RUNTIME_PROVIDER_DISABLED";
    private static final String ERROR_PROMPT_NOT_FOUND = "ZHYC_AI_RUNTIME_PROMPT_NOT_FOUND";
    private static final String ERROR_PROMPT_NOT_PUBLISHED = "ZHYC_AI_RUNTIME_PROMPT_NOT_PUBLISHED";
    private static final String ERROR_CHAT_CLIENT_NOT_FOUND = "ZHYC_AI_RUNTIME_CLIENT_NOT_FOUND";

    private final AiAppRepository appRepository;
    private final AiModelConfigRepository modelRepository;
    private final AiProviderRepository providerRepository;
    private final AiPromptTemplateRepository promptRepository;
    private final AiInvocationAuditRepository auditRepository;
    private final List<AiChatClient> chatClients;
    private final SecretResolver secretResolver;

    public DefaultAiRuntimeService(AiAppRepository appRepository, AiModelConfigRepository modelRepository,
                                   AiProviderRepository providerRepository,
                                   AiPromptTemplateRepository promptRepository,
                                   AiInvocationAuditRepository auditRepository, List<AiChatClient> chatClients,
                                   SecretResolver secretResolver) {
        this.appRepository = Objects.requireNonNull(appRepository, "AI 应用仓储不能为空");
        this.modelRepository = Objects.requireNonNull(modelRepository, "AI 模型配置仓储不能为空");
        this.providerRepository = Objects.requireNonNull(providerRepository, "AI 模型供应商仓储不能为空");
        this.promptRepository = Objects.requireNonNull(promptRepository, "AI 提示词模板仓储不能为空");
        this.auditRepository = Objects.requireNonNull(auditRepository, "AI 调用审计仓储不能为空");
        this.chatClients = List.copyOf(Objects.requireNonNull(chatClients, "AI 对话适配器列表不能为空"));
        this.secretResolver = Objects.requireNonNull(secretResolver, "密钥解析器不能为空");
    }

    @Override
    @Transactional
    public AiRuntimeChatResponse chat(AiRuntimeChatCommand command) {
        AiRuntimeChatCommand requiredCommand = AiValidationSupport.requireObject(command, ERROR_COMMAND_REQUIRED,
                "AI 运行时调用命令不能为空");
        String tenantId = AiValidationSupport.requireTenantId(requiredCommand.tenantId(), ERROR_TENANT_ID_REQUIRED);
        String appCode = AiValidationSupport.requireText(requiredCommand.appCode(), ERROR_APP_CODE_REQUIRED,
                "AI 应用编码不能为空");
        String promptCode = AiValidationSupport.requireText(requiredCommand.promptCode(), ERROR_PROMPT_CODE_REQUIRED,
                "AI 提示词编码不能为空");
        String promptVersion = defaultPromptVersion(requiredCommand.promptVersion());

        AiApp app = appRepository.findByTenantIdAndAppCode(tenantId, appCode)
                .orElseThrow(() -> new BusinessException(ERROR_APP_NOT_FOUND, "AI 应用不存在或不属于当前租户"));
        requireEnabled(app.getStatus(), ERROR_APP_DISABLED, "AI 应用未启用，不能调用");
        AiModelConfig model = modelRepository.findByTenantIdAndId(tenantId, app.getDefaultModelId())
                .orElseThrow(() -> new BusinessException(ERROR_MODEL_NOT_FOUND, "AI 应用绑定的默认模型不存在"));
        requireEnabled(model.getStatus(), ERROR_MODEL_DISABLED, "AI 模型未启用，不能调用");
        AiProvider provider = providerRepository.findByTenantIdAndId(tenantId, model.getProviderId())
                .orElseThrow(() -> new BusinessException(ERROR_PROVIDER_NOT_FOUND, "AI 模型绑定的供应商不存在"));
        requireEnabled(provider.getStatus(), ERROR_PROVIDER_DISABLED, "AI 模型供应商未启用，不能调用");
        AiPromptTemplate prompt = promptRepository
                .findByTenantIdAndPromptCodeAndVersion(tenantId, promptCode, promptVersion)
                .orElseThrow(() -> new BusinessException(ERROR_PROMPT_NOT_FOUND, "AI 提示词模板不存在或版本不匹配"));
        requirePublished(prompt);

        AiChatClient chatClient = resolveChatClient(provider.getProviderType());
        String renderedPrompt = renderPrompt(prompt.getTemplateContent(), requiredCommand.variables());
        AiChatRequest chatRequest = new AiChatRequest(tenantId, app.getAppCode(), provider.getProviderCode(),
                provider.getBaseUrl(), model.getModelCode(), app.getSystemPrompt(), renderedPrompt,
                requiredCommand.stream());
        long startedAt = System.nanoTime();
        try {
            String apiKey = withTenantContext(tenantId,
                    () -> secretResolver.resolve(SecretReference.parse(provider.getSecretRef())));
            AiChatResponse chatResponse = chatClient.chat(chatRequest, apiKey);
            long latencyMs = elapsedMillis(startedAt);
            recordAudit(tenantId, app, provider, model, chatResponse, latencyMs, "success", null);
            return new AiRuntimeChatResponse(app.getAppCode(), provider.getProviderCode(), model.getModelCode(),
                    chatResponse.content(), chatResponse.promptTokens(), chatResponse.completionTokens(),
                    chatResponse.totalTokens(), latencyMs, chatResponse.traceId());
        } catch (RuntimeException ex) {
            long latencyMs = elapsedMillis(startedAt);
            recordAudit(tenantId, app, provider, model, new AiChatResponse("", 0, 0, 0, null), latencyMs,
                    "failed", ex.getMessage());
            throw ex;
        }
    }

    private AiChatClient resolveChatClient(String providerType) {
        return chatClients.stream()
                .filter(client -> client.supports(providerType))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ERROR_CHAT_CLIENT_NOT_FOUND,
                        "未找到支持该供应商类型的 AI 对话适配器: " + providerType));
    }

    private void requireEnabled(String status, String code, String message) {
        if (!"enabled".equals(status)) {
            throw new BusinessException(code, message);
        }
    }

    private void requirePublished(AiPromptTemplate prompt) {
        if (!"published".equals(prompt.getStatus())) {
            throw new BusinessException(ERROR_PROMPT_NOT_PUBLISHED, "AI 提示词模板未发布，不能调用");
        }
    }

    private String defaultPromptVersion(String promptVersion) {
        String normalized = AiValidationSupport.trimToNull(promptVersion);
        return normalized == null ? "v1" : normalized;
    }

    private String renderPrompt(String template, Map<String, String> variables) {
        String rendered = template == null ? "" : template;
        Map<String, String> safeVariables = variables == null ? Map.of() : variables;
        for (Map.Entry<String, String> entry : safeVariables.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().trim();
            if (!key.isEmpty()) {
                rendered = rendered.replace("{{" + key + "}}", entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return rendered;
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }

    /**
     * 在当前线程临时绑定租户上下文执行密钥解析。
     *
     * <p>AI 运行时通过命令持有租户编码，而系统密钥解析器通过线程上下文读取租户。
     * 这里在解析供应商密钥前绑定租户，并在调用结束后恢复原上下文，避免跨请求污染。</p>
     *
     * @param tenantId 租户业务编码
     * @param supplier 密钥解析操作
     * @param <T> 返回值类型
     * @return 操作返回值
     */
    private static <T> T withTenantContext(String tenantId, Supplier<T> supplier) {
        String previousTenantId = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            return supplier.get();
        } finally {
            if (previousTenantId == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(previousTenantId);
            }
        }
    }

    private void recordAudit(String tenantId, AiApp app, AiProvider provider, AiModelConfig model,
                             AiChatResponse response, long latencyMs, String status, String errorMessage) {
        auditRepository.save(new AiInvocationAudit(null, tenantId, app.getAppCode(), provider.getId(), model.getId(),
                "chat", Math.max(response.promptTokens(), 0), Math.max(response.completionTokens(), 0),
                Math.max(response.totalTokens(), 0), latencyMs, status,
                AiValidationSupport.trimToNull(errorMessage), AiValidationSupport.trimToNull(response.traceId()),
                null));
    }
}
