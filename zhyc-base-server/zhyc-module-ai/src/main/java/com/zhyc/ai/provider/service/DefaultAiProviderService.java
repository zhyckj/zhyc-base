/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

import com.zhyc.ai.provider.domain.AiProvider;
import com.zhyc.ai.provider.domain.AiProviderStatus;
import com.zhyc.ai.provider.domain.AiProviderType;
import com.zhyc.ai.provider.repository.AiProviderRepository;
import com.zhyc.ai.support.AiValidationSupport;
import com.zhyc.common.cache.ZhycCacheNames;
import com.zhyc.common.exception.BusinessException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认 AI 模型供应商业务服务。
 */
@Service
public class DefaultAiProviderService implements AiProviderService {

    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_PROVIDER_COMMAND_REQUIRED";
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_PROVIDER_TENANT_ID_REQUIRED";
    private static final String ERROR_PROVIDER_CODE_REQUIRED = "ZHYC_AI_PROVIDER_CODE_REQUIRED";
    private static final String ERROR_PROVIDER_NAME_REQUIRED = "ZHYC_AI_PROVIDER_NAME_REQUIRED";
    private static final String ERROR_PROVIDER_TYPE_REQUIRED = "ZHYC_AI_PROVIDER_TYPE_REQUIRED";
    private static final String ERROR_PROVIDER_TYPE_UNSUPPORTED = "ZHYC_AI_PROVIDER_TYPE_UNSUPPORTED";
    private static final String ERROR_BASE_URL_REQUIRED = "ZHYC_AI_PROVIDER_BASE_URL_REQUIRED";
    private static final String ERROR_SECRET_REF_INVALID = "ZHYC_AI_PROVIDER_SECRET_REF_INVALID";
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_AI_PROVIDER_STATUS_REQUIRED";
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_AI_PROVIDER_STATUS_UNSUPPORTED";

    private final AiProviderRepository providerRepository;

    public DefaultAiProviderService(AiProviderRepository providerRepository) {
        this.providerRepository = Objects.requireNonNull(providerRepository, "AI 模型供应商仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.AI_PROVIDERS,
            key = "#tenantId == null ? '' : #tenantId.trim()")
    public List<AiProviderResponse> listProviders(String tenantId) {
        String requiredTenantId = AiValidationSupport.requireTenantId(tenantId, ERROR_TENANT_ID_REQUIRED);
        return providerRepository.findByTenantId(requiredTenantId).stream()
                .map(provider -> new AiProviderResponse(provider.getId(), provider.getProviderCode(),
                        provider.getProviderName(), provider.getProviderType(), provider.getBaseUrl(), provider.getSecretRef(),
                        provider.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.AI_PROVIDERS, ZhycCacheNames.AI_MODELS, ZhycCacheNames.AI_APPS},
            allEntries = true)
    public void save(AiProviderSaveCommand command) {
        AiProviderSaveCommand requiredCommand = AiValidationSupport.requireObject(command, ERROR_COMMAND_REQUIRED,
                "AI 模型供应商保存命令不能为空");
        AiProvider provider = new AiProvider(null,
                AiValidationSupport.requireTenantId(requiredCommand.getTenantId(), ERROR_TENANT_ID_REQUIRED),
                AiValidationSupport.requireText(requiredCommand.getProviderCode(), ERROR_PROVIDER_CODE_REQUIRED,
                        "模型供应商编码不能为空"),
                AiValidationSupport.requireText(requiredCommand.getProviderName(), ERROR_PROVIDER_NAME_REQUIRED,
                        "模型供应商名称不能为空"),
                requireProviderType(requiredCommand.getProviderType()),
                AiValidationSupport.requireText(requiredCommand.getBaseUrl(), ERROR_BASE_URL_REQUIRED,
                        "模型服务基础地址不能为空"),
                AiValidationSupport.requireSecretRef(requiredCommand.getSecretRef(), ERROR_SECRET_REF_INVALID),
                requireStatus(requiredCommand.getStatus()), null, null);
        providerRepository.save(provider);
    }

    private String requireProviderType(String providerType) {
        String normalized = AiValidationSupport.requireText(providerType, ERROR_PROVIDER_TYPE_REQUIRED,
                "模型供应商类型不能为空");
        try {
            return AiProviderType.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_PROVIDER_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    private String requireStatus(String status) {
        String normalized = AiValidationSupport.requireText(status, ERROR_STATUS_REQUIRED, "模型供应商状态不能为空");
        try {
            return AiProviderStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }
}
