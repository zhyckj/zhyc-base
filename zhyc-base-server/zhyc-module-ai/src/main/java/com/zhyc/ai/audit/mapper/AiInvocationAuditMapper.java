/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.mapper;

import com.zhyc.ai.audit.domain.AiInvocationAudit;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * AI 调用审计 MyBatis Mapper。
 */
@Mapper
public interface AiInvocationAuditMapper {

    @SelectProvider(type = AiInvocationAuditSqlProvider.class, method = "selectByTenantIdAndAppCode")
    List<AiInvocationAudit> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                                       @Param("appCode") String appCode);

    @InsertProvider(type = AiInvocationAuditSqlProvider.class, method = "insert")
    void insert(AiInvocationAudit audit);
}
