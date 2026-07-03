/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.repository;

import com.zhyc.ai.audit.domain.AiInvocationAudit;

import java.util.List;

/**
 * AI 调用审计仓储。
 */
public interface AiInvocationAuditRepository {

    List<AiInvocationAudit> findByTenantIdAndAppCode(String tenantId, String appCode);

    void save(AiInvocationAudit audit);
}
