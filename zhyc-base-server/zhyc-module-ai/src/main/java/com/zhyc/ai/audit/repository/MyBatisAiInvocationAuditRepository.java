/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.repository;

import com.zhyc.ai.audit.domain.AiInvocationAudit;
import com.zhyc.ai.audit.mapper.AiInvocationAuditMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的 AI 调用审计仓储实现。
 */
@Repository
public class MyBatisAiInvocationAuditRepository implements AiInvocationAuditRepository {

    private final AiInvocationAuditMapper auditMapper;

    public MyBatisAiInvocationAuditRepository(AiInvocationAuditMapper auditMapper) {
        this.auditMapper = Objects.requireNonNull(auditMapper, "AI 调用审计 Mapper 不能为空");
    }

    @Override
    public List<AiInvocationAudit> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return auditMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(AiInvocationAudit audit) {
        auditMapper.insert(audit);
    }
}
