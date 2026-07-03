/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.repository;

import com.zhyc.ai.app.domain.AiApp;

import java.util.List;
import java.util.Optional;

/**
 * AI 应用接入仓储。
 */
public interface AiAppRepository {

    List<AiApp> findByTenantId(String tenantId);

    Optional<AiApp> findByTenantIdAndAppCode(String tenantId, String appCode);

    void save(AiApp app);
}
