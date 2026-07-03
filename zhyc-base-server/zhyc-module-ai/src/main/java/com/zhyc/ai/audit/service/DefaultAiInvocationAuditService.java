/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.service;

import com.zhyc.ai.audit.domain.AiInvocationAudit;
import com.zhyc.ai.audit.domain.AiInvocationAuditStatus;
import com.zhyc.ai.audit.repository.AiInvocationAuditRepository;
import com.zhyc.ai.support.AiValidationSupport;
import com.zhyc.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认 AI 调用审计业务服务。
 */
@Service
public class DefaultAiInvocationAuditService implements AiInvocationAuditService {

    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_AI_AUDIT_COMMAND_REQUIRED";
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_AI_AUDIT_TENANT_ID_REQUIRED";
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_AI_AUDIT_APP_CODE_REQUIRED";
    private static final String ERROR_PROVIDER_ID_REQUIRED = "ZHYC_AI_AUDIT_PROVIDER_ID_REQUIRED";
    private static final String ERROR_MODEL_ID_REQUIRED = "ZHYC_AI_AUDIT_MODEL_ID_REQUIRED";
    private static final String ERROR_INVOCATION_TYPE_REQUIRED = "ZHYC_AI_AUDIT_INVOCATION_TYPE_REQUIRED";
    private static final String ERROR_TOTAL_TOKENS_INVALID = "ZHYC_AI_AUDIT_TOTAL_TOKENS_INVALID";
    private static final String ERROR_LATENCY_INVALID = "ZHYC_AI_AUDIT_LATENCY_INVALID";
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_AI_AUDIT_STATUS_REQUIRED";
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_AI_AUDIT_STATUS_UNSUPPORTED";

    private final AiInvocationAuditRepository auditRepository;

    public DefaultAiInvocationAuditService(AiInvocationAuditRepository auditRepository) {
        this.auditRepository = Objects.requireNonNull(auditRepository, "AI 调用审计仓储不能为空");
    }

    @Override
    public List<AiInvocationAuditResponse> listAudits(String tenantId, String appCode) {
        String requiredTenantId = AiValidationSupport.requireTenantId(tenantId, ERROR_TENANT_ID_REQUIRED);
        String requiredAppCode = AiValidationSupport.requireText(appCode, ERROR_APP_CODE_REQUIRED,
                "AI 应用编码不能为空");
        return auditRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(audit -> new AiInvocationAuditResponse(audit.getAppCode(), audit.getProviderId(),
                        audit.getModelId(), audit.getInvocationType(), audit.getPromptTokens(),
                        audit.getCompletionTokens(), audit.getTotalTokens(), audit.getLatencyMs(), audit.getStatus(),
                        audit.getErrorMessage(), audit.getTraceId()))
                .toList();
    }

    @Override
    @Transactional
    public void record(AiInvocationAuditRecordCommand command) {
        AiInvocationAuditRecordCommand requiredCommand = AiValidationSupport.requireObject(command,
                ERROR_COMMAND_REQUIRED, "AI 调用审计记录命令不能为空");
        if (requiredCommand.getTotalTokens() < 0) {
            throw new BusinessException(ERROR_TOTAL_TOKENS_INVALID, "AI 调用总令牌数不能小于 0");
        }
        if (requiredCommand.getLatencyMs() < 0) {
            throw new BusinessException(ERROR_LATENCY_INVALID, "AI 调用耗时不能小于 0");
        }
        AiInvocationAudit audit = new AiInvocationAudit(null,
                AiValidationSupport.requireTenantId(requiredCommand.getTenantId(), ERROR_TENANT_ID_REQUIRED),
                AiValidationSupport.requireText(requiredCommand.getAppCode(), ERROR_APP_CODE_REQUIRED,
                        "AI 应用编码不能为空"),
                AiValidationSupport.requireObject(requiredCommand.getProviderId(), ERROR_PROVIDER_ID_REQUIRED,
                        "AI 模型供应商主键不能为空"),
                AiValidationSupport.requireObject(requiredCommand.getModelId(), ERROR_MODEL_ID_REQUIRED,
                        "AI 模型配置主键不能为空"),
                AiValidationSupport.requireText(requiredCommand.getInvocationType(), ERROR_INVOCATION_TYPE_REQUIRED,
                        "AI 调用类型不能为空"),
                Math.max(requiredCommand.getPromptTokens(), 0),
                Math.max(requiredCommand.getCompletionTokens(), 0),
                requiredCommand.getTotalTokens(), requiredCommand.getLatencyMs(),
                requireStatus(requiredCommand.getStatus()),
                AiValidationSupport.trimToNull(requiredCommand.getErrorMessage()),
                AiValidationSupport.trimToNull(requiredCommand.getTraceId()), null);
        auditRepository.save(audit);
    }

    private String requireStatus(String status) {
        String normalized = AiValidationSupport.requireText(status, ERROR_STATUS_REQUIRED, "AI 调用状态不能为空");
        try {
            return AiInvocationAuditStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }
}
