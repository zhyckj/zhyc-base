/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.service;

import com.zhyc.ai.app.domain.AiApp;
import com.zhyc.ai.app.domain.AiAppStatus;
import com.zhyc.ai.app.repository.AiAppRepository;
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
 * 默认 AI 应用接入业务服务。
 */
@Service
public class DefaultAiAppService implements AiAppService {

    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_APP_COMMAND_REQUIRED";
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_APP_TENANT_ID_REQUIRED";
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_AI_APP_CODE_REQUIRED";
    private static final String ERROR_APP_NAME_REQUIRED = "ZHYC_AI_APP_NAME_REQUIRED";
    private static final String ERROR_MODEL_ID_REQUIRED = "ZHYC_AI_APP_MODEL_ID_REQUIRED";
    private static final String ERROR_SYSTEM_PROMPT_REQUIRED = "ZHYC_AI_APP_SYSTEM_PROMPT_REQUIRED";
    private static final String ERROR_DAILY_QUOTA_INVALID = "ZHYC_AI_APP_DAILY_QUOTA_INVALID";
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_AI_APP_STATUS_REQUIRED";
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_AI_APP_STATUS_UNSUPPORTED";

    private final AiAppRepository appRepository;

    public DefaultAiAppService(AiAppRepository appRepository) {
        this.appRepository = Objects.requireNonNull(appRepository, "AI 应用接入仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.AI_APPS,
            key = "#tenantId == null ? '' : #tenantId.trim()")
    public List<AiAppResponse> listApps(String tenantId) {
        String requiredTenantId = AiValidationSupport.requireTenantId(tenantId, ERROR_TENANT_ID_REQUIRED);
        return appRepository.findByTenantId(requiredTenantId).stream()
                .map(app -> new AiAppResponse(app.getAppCode(), app.getAppName(), app.getDefaultModelId(),
                        app.getSystemPrompt(), app.getDailyTokenQuota(), app.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = ZhycCacheNames.AI_APPS, allEntries = true)
    public void save(AiAppSaveCommand command) {
        AiAppSaveCommand requiredCommand = AiValidationSupport.requireObject(command, ERROR_COMMAND_REQUIRED,
                "AI 应用接入保存命令不能为空");
        if (requiredCommand.getDailyTokenQuota() <= 0) {
            throw new BusinessException(ERROR_DAILY_QUOTA_INVALID, "AI 应用每日令牌额度必须大于 0");
        }
        AiApp app = new AiApp(null,
                AiValidationSupport.requireTenantId(requiredCommand.getTenantId(), ERROR_TENANT_ID_REQUIRED),
                AiValidationSupport.requireText(requiredCommand.getAppCode(), ERROR_APP_CODE_REQUIRED,
                        "AI 应用编码不能为空"),
                AiValidationSupport.requireText(requiredCommand.getAppName(), ERROR_APP_NAME_REQUIRED,
                        "AI 应用名称不能为空"),
                AiValidationSupport.requireObject(requiredCommand.getDefaultModelId(), ERROR_MODEL_ID_REQUIRED,
                        "默认模型配置主键不能为空"),
                AiValidationSupport.requireText(requiredCommand.getSystemPrompt(), ERROR_SYSTEM_PROMPT_REQUIRED,
                        "AI 应用系统提示词不能为空"),
                requiredCommand.getDailyTokenQuota(), requireStatus(requiredCommand.getStatus()), null, null);
        appRepository.save(app);
    }

    private String requireStatus(String status) {
        String normalized = AiValidationSupport.requireText(status, ERROR_STATUS_REQUIRED, "AI 应用状态不能为空");
        try {
            return AiAppStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }
}
