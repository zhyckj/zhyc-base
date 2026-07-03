/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.repository;

import com.zhyc.ai.app.domain.AiApp;
import com.zhyc.ai.app.mapper.AiAppMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的 AI 应用接入仓储实现。
 */
@Repository
public class MyBatisAiAppRepository implements AiAppRepository {

    private final AiAppMapper appMapper;

    public MyBatisAiAppRepository(AiAppMapper appMapper) {
        this.appMapper = Objects.requireNonNull(appMapper, "AI 应用接入 Mapper 不能为空");
    }

    @Override
    public List<AiApp> findByTenantId(String tenantId) {
        return appMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<AiApp> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return appMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(AiApp app) {
        appMapper.upsert(app);
    }
}
