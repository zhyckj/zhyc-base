/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

import com.zhyc.ai.provider.domain.AiProviderType;
import com.zhyc.ai.support.AiValidationSupport;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretReference;
import com.zhyc.common.secret.SecretResolver;
import com.zhyc.common.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 默认 AI 供应商可用性测试服务。
 */
@Service
public class DefaultAiProviderTestService implements AiProviderTestService {

    /** 测试命令缺失错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_PROVIDER_TEST_COMMAND_REQUIRED";
    /** 租户业务编码缺失错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_PROVIDER_TEST_TENANT_ID_REQUIRED";
    /** 供应商编码缺失错误码。 */
    private static final String ERROR_PROVIDER_CODE_REQUIRED = "ZHYC_AI_PROVIDER_TEST_CODE_REQUIRED";
    /** 供应商类型缺失错误码。 */
    private static final String ERROR_PROVIDER_TYPE_REQUIRED = "ZHYC_AI_PROVIDER_TEST_TYPE_REQUIRED";
    /** 供应商类型不支持错误码。 */
    private static final String ERROR_PROVIDER_TYPE_UNSUPPORTED = "ZHYC_AI_PROVIDER_TEST_TYPE_UNSUPPORTED";
    /** 基础地址缺失错误码。 */
    private static final String ERROR_BASE_URL_REQUIRED = "ZHYC_AI_PROVIDER_TEST_BASE_URL_REQUIRED";
    /** 密钥引用缺失错误码。 */
    private static final String ERROR_SECRET_REF_REQUIRED = "ZHYC_AI_PROVIDER_TEST_SECRET_REF_REQUIRED";
    /** 连通性客户端缺失错误码。 */
    private static final String ERROR_TEST_CLIENT_NOT_FOUND = "ZHYC_AI_PROVIDER_TEST_CLIENT_NOT_FOUND";

    /** 供应商连通性测试客户端列表。 */
    private final List<AiProviderConnectivityClient> connectivityClients;
    /** 密钥解析器。 */
    private final SecretResolver secretResolver;

    /**
     * 创建默认 AI 供应商可用性测试服务。
     *
     * @param connectivityClients 供应商连通性测试客户端列表
     * @param secretResolver 密钥解析器
     */
    public DefaultAiProviderTestService(List<AiProviderConnectivityClient> connectivityClients,
                                        SecretResolver secretResolver) {
        this.connectivityClients = List.copyOf(Objects.requireNonNull(connectivityClients, "供应商测试客户端列表不能为空"));
        this.secretResolver = Objects.requireNonNull(secretResolver, "密钥解析器不能为空");
    }

    @Override
    public AiProviderTestResponse test(AiProviderTestCommand command) {
        AiProviderTestCommand requiredCommand = AiValidationSupport.requireObject(command, ERROR_COMMAND_REQUIRED,
                "AI 供应商测试命令不能为空");
        String tenantId = AiValidationSupport.requireTenantId(requiredCommand.tenantId(), ERROR_TENANT_ID_REQUIRED);
        String providerCode = AiValidationSupport.requireText(requiredCommand.providerCode(),
                ERROR_PROVIDER_CODE_REQUIRED, "供应商编码不能为空");
        String providerType = requireProviderType(requiredCommand.providerType());
        String baseUrl = AiValidationSupport.requireText(requiredCommand.baseUrl(), ERROR_BASE_URL_REQUIRED,
                "供应商基础地址不能为空");
        String secretRef = AiValidationSupport.requireText(requiredCommand.secretRef(), ERROR_SECRET_REF_REQUIRED,
                "供应商密钥引用不能为空");
        AiProviderConnectivityClient client = resolveClient(providerType);
        long startedAt = System.nanoTime();
        try {
            String apiKey = withTenantContext(tenantId, () -> secretResolver.resolve(SecretReference.parse(secretRef)));
            client.test(providerType, providerCode, secretRef, baseUrl, apiKey);
            return new AiProviderTestResponse(providerCode, true, elapsedMillis(startedAt), "供应商可用");
        } catch (RuntimeException ex) {
            return new AiProviderTestResponse(providerCode, false, elapsedMillis(startedAt),
                    normalizeErrorMessage(ex));
        }
    }

    private String requireProviderType(String providerType) {
        String normalized = AiValidationSupport.requireText(providerType, ERROR_PROVIDER_TYPE_REQUIRED,
                "供应商类型不能为空");
        try {
            return AiProviderType.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_PROVIDER_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    private AiProviderConnectivityClient resolveClient(String providerType) {
        return connectivityClients.stream()
                .filter(client -> client.supports(providerType))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ERROR_TEST_CLIENT_NOT_FOUND,
                        "未找到支持该供应商类型的测试客户端: " + providerType));
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }

    private String normalizeErrorMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? "供应商测试失败" : message;
    }

    /**
     * 在当前线程临时绑定租户上下文执行密钥解析。
     *
     * <p>系统密钥解析器基于 {@link TenantContext} 确认租户边界，供应商测试入口只有请求中的租户编码，
     * 因此解析前需要绑定，结束后恢复调用前上下文。</p>
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
}
