/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.service;

import java.util.List;

/**
 * AI 调用审计业务服务。
 */
public interface AiInvocationAuditService {

    List<AiInvocationAuditResponse> listAudits(String tenantId, String appCode);

    void record(AiInvocationAuditRecordCommand command);
}
