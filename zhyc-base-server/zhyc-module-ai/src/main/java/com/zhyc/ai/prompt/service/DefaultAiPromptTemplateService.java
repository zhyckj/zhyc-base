/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.service;

import com.zhyc.ai.prompt.domain.AiPromptTemplate;
import com.zhyc.ai.prompt.domain.AiPromptTemplateStatus;
import com.zhyc.ai.prompt.repository.AiPromptTemplateRepository;
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
 * 默认 AI 提示词模板业务服务。
 */
@Service
public class DefaultAiPromptTemplateService implements AiPromptTemplateService {

    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_PROMPT_COMMAND_REQUIRED";
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_PROMPT_TENANT_ID_REQUIRED";
    private static final String ERROR_PROMPT_CODE_REQUIRED = "ZHYC_AI_PROMPT_CODE_REQUIRED";
    private static final String ERROR_PROMPT_NAME_REQUIRED = "ZHYC_AI_PROMPT_NAME_REQUIRED";
    private static final String ERROR_VERSION_REQUIRED = "ZHYC_AI_PROMPT_VERSION_REQUIRED";
    private static final String ERROR_TEMPLATE_CONTENT_REQUIRED = "ZHYC_AI_PROMPT_TEMPLATE_CONTENT_REQUIRED";
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_AI_PROMPT_STATUS_REQUIRED";
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_AI_PROMPT_STATUS_UNSUPPORTED";

    private final AiPromptTemplateRepository templateRepository;

    public DefaultAiPromptTemplateService(AiPromptTemplateRepository templateRepository) {
        this.templateRepository = Objects.requireNonNull(templateRepository, "AI 提示词模板仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.AI_PROMPTS,
            key = "#tenantId == null ? '' : #tenantId.trim()")
    public List<AiPromptTemplateResponse> listTemplates(String tenantId) {
        String requiredTenantId = AiValidationSupport.requireTenantId(tenantId, ERROR_TENANT_ID_REQUIRED);
        return templateRepository.findByTenantId(requiredTenantId).stream()
                .map(template -> new AiPromptTemplateResponse(template.getPromptCode(), template.getPromptName(),
                        template.getVersion(), template.getTemplateContent(), template.getVariables(),
                        template.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = ZhycCacheNames.AI_PROMPTS, allEntries = true)
    public void save(AiPromptTemplateSaveCommand command) {
        AiPromptTemplateSaveCommand requiredCommand = AiValidationSupport.requireObject(command,
                ERROR_COMMAND_REQUIRED, "AI 提示词模板保存命令不能为空");
        AiPromptTemplate template = new AiPromptTemplate(null,
                AiValidationSupport.requireTenantId(requiredCommand.getTenantId(), ERROR_TENANT_ID_REQUIRED),
                AiValidationSupport.requireText(requiredCommand.getPromptCode(), ERROR_PROMPT_CODE_REQUIRED,
                        "AI 提示词编码不能为空"),
                AiValidationSupport.requireText(requiredCommand.getPromptName(), ERROR_PROMPT_NAME_REQUIRED,
                        "AI 提示词名称不能为空"),
                AiValidationSupport.requireText(requiredCommand.getVersion(), ERROR_VERSION_REQUIRED,
                        "AI 提示词版本不能为空"),
                AiValidationSupport.requireText(requiredCommand.getTemplateContent(),
                        ERROR_TEMPLATE_CONTENT_REQUIRED, "AI 提示词模板内容不能为空"),
                AiValidationSupport.trimToNull(requiredCommand.getVariables()),
                requireStatus(requiredCommand.getStatus()), null, null);
        templateRepository.save(template);
    }

    private String requireStatus(String status) {
        String normalized = AiValidationSupport.requireText(status, ERROR_STATUS_REQUIRED, "AI 提示词状态不能为空");
        try {
            return AiPromptTemplateStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }
}
