/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.service;

import com.zhyc.ai.model.domain.AiModelConfig;
import com.zhyc.ai.model.domain.AiModelConfigStatus;
import com.zhyc.ai.model.domain.AiModelType;
import com.zhyc.ai.model.repository.AiModelConfigRepository;
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
 * 默认 AI 模型配置业务服务。
 */
@Service
public class DefaultAiModelConfigService implements AiModelConfigService {

    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_MODEL_COMMAND_REQUIRED";
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_MODEL_TENANT_ID_REQUIRED";
    private static final String ERROR_PROVIDER_ID_REQUIRED = "ZHYC_AI_MODEL_PROVIDER_ID_REQUIRED";
    private static final String ERROR_MODEL_CODE_REQUIRED = "ZHYC_AI_MODEL_CODE_REQUIRED";
    private static final String ERROR_MODEL_NAME_REQUIRED = "ZHYC_AI_MODEL_NAME_REQUIRED";
    private static final String ERROR_MODEL_TYPE_REQUIRED = "ZHYC_AI_MODEL_TYPE_REQUIRED";
    private static final String ERROR_MODEL_TYPE_UNSUPPORTED = "ZHYC_AI_MODEL_TYPE_UNSUPPORTED";
    private static final String ERROR_CONTEXT_WINDOW_INVALID = "ZHYC_AI_MODEL_CONTEXT_WINDOW_INVALID";
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_AI_MODEL_STATUS_REQUIRED";
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_AI_MODEL_STATUS_UNSUPPORTED";

    private final AiModelConfigRepository modelConfigRepository;

    public DefaultAiModelConfigService(AiModelConfigRepository modelConfigRepository) {
        this.modelConfigRepository = Objects.requireNonNull(modelConfigRepository, "AI 模型配置仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.AI_MODELS,
            key = "#tenantId == null ? '' : #tenantId.trim()")
    public List<AiModelConfigResponse> listModels(String tenantId) {
        String requiredTenantId = AiValidationSupport.requireTenantId(tenantId, ERROR_TENANT_ID_REQUIRED);
        return modelConfigRepository.findByTenantId(requiredTenantId).stream()
                .map(model -> new AiModelConfigResponse(model.getId(), model.getProviderId(), model.getModelCode(),
                        model.getModelName(), model.getModelType(), model.getContextWindow(), model.isSupportStream(),
                        model.isSupportTool(), model.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.AI_MODELS, ZhycCacheNames.AI_APPS}, allEntries = true)
    public void save(AiModelConfigSaveCommand command) {
        AiModelConfigSaveCommand requiredCommand = AiValidationSupport.requireObject(command,
                ERROR_COMMAND_REQUIRED, "AI 模型配置保存命令不能为空");
        int contextWindow = requiredCommand.getContextWindow();
        if (contextWindow <= 0) {
            throw new BusinessException(ERROR_CONTEXT_WINDOW_INVALID, "模型上下文长度必须大于 0");
        }
        AiModelConfig modelConfig = new AiModelConfig(null,
                AiValidationSupport.requireTenantId(requiredCommand.getTenantId(), ERROR_TENANT_ID_REQUIRED),
                AiValidationSupport.requireObject(requiredCommand.getProviderId(), ERROR_PROVIDER_ID_REQUIRED,
                        "模型供应商主键不能为空"),
                AiValidationSupport.requireText(requiredCommand.getModelCode(), ERROR_MODEL_CODE_REQUIRED,
                        "模型编码不能为空"),
                AiValidationSupport.requireText(requiredCommand.getModelName(), ERROR_MODEL_NAME_REQUIRED,
                        "模型名称不能为空"),
                requireModelType(requiredCommand.getModelType()), contextWindow,
                requiredCommand.isSupportStream(), requiredCommand.isSupportTool(),
                requireStatus(requiredCommand.getStatus()), null, null);
        modelConfigRepository.save(modelConfig);
    }

    private String requireModelType(String modelType) {
        String normalized = AiValidationSupport.requireText(modelType, ERROR_MODEL_TYPE_REQUIRED,
                "模型类型不能为空");
        try {
            return AiModelType.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_MODEL_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    private String requireStatus(String status) {
        String normalized = AiValidationSupport.requireText(status, ERROR_STATUS_REQUIRED, "模型配置状态不能为空");
        try {
            return AiModelConfigStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }
}
