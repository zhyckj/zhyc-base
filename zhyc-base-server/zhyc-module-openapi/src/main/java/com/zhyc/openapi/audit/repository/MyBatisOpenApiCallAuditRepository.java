/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.repository;

import com.zhyc.openapi.audit.domain.OpenApiCallAudit;
import com.zhyc.openapi.audit.mapper.OpenApiCallAuditMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放 API 调用审计仓储实现。
 */
@Repository
public class MyBatisOpenApiCallAuditRepository implements OpenApiCallAuditRepository {

    /** 开放 API 调用审计 Mapper。 */
    private final OpenApiCallAuditMapper auditMapper;

    /**
     * 创建开放 API 调用审计仓储实现。
     *
     * @param auditMapper 开放 API 调用审计 Mapper
     */
    public MyBatisOpenApiCallAuditRepository(OpenApiCallAuditMapper auditMapper) {
        this.auditMapper = Objects.requireNonNull(auditMapper, "开放 API 调用审计 Mapper 不能为空");
    }

    @Override
    public List<OpenApiCallAudit> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return auditMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public List<OpenApiCallAudit> findErrorLogsByTenantIdAndAppCode(String tenantId, String appCode) {
        return auditMapper.selectErrorLogsByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(OpenApiCallAudit audit) {
        auditMapper.insert(audit);
    }
}
