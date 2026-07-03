/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.repository;

import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.mapper.OpenApiAppMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的开发者应用仓储实现。
 */
@Repository
public class MyBatisOpenApiAppRepository implements OpenApiAppRepository {

    /** 开发者应用 Mapper。 */
    private final OpenApiAppMapper appMapper;

    /**
     * 创建开发者应用仓储实现。
     *
     * @param appMapper 开发者应用 Mapper
     */
    public MyBatisOpenApiAppRepository(OpenApiAppMapper appMapper) {
        this.appMapper = Objects.requireNonNull(appMapper, "开发者应用 Mapper 不能为空");
    }

    @Override
    public List<OpenApiApp> findByTenantId(String tenantId) {
        return appMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<OpenApiApp> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return Optional.ofNullable(appMapper.selectByTenantIdAndAppCode(tenantId, appCode));
    }

    @Override
    public void save(OpenApiApp app) {
        appMapper.upsert(app);
    }
}
