/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.repository;

import com.zhyc.openapi.audit.domain.OpenApiCallAudit;

import java.util.List;

/**
 * 开放 API 调用审计仓储。
 */
public interface OpenApiCallAuditRepository {

    /**
     * 查询租户指定应用的开放 API 调用审计列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 调用审计列表
     */
    List<OpenApiCallAudit> findByTenantIdAndAppCode(String tenantId, String appCode);

    /**
     * 查询租户指定应用的开放 API 错误日志列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 错误日志列表
     */
    List<OpenApiCallAudit> findErrorLogsByTenantIdAndAppCode(String tenantId, String appCode);

    /**
     * 保存开放 API 调用审计。
     *
     * @param audit 开放 API 调用审计领域对象
     */
    void save(OpenApiCallAudit audit);
}
