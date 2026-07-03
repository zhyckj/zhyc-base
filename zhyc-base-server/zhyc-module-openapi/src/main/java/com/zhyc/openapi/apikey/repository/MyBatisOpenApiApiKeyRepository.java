/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.repository;

import com.zhyc.openapi.apikey.domain.OpenApiApiKey;
import com.zhyc.openapi.apikey.mapper.OpenApiApiKeyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的 API Key 仓储实现。
 */
@Repository
public class MyBatisOpenApiApiKeyRepository implements OpenApiApiKeyRepository {

    /** API Key Mapper。 */
    private final OpenApiApiKeyMapper apiKeyMapper;

    /**
     * 创建 API Key 仓储实现。
     *
     * @param apiKeyMapper API Key Mapper
     */
    public MyBatisOpenApiApiKeyRepository(OpenApiApiKeyMapper apiKeyMapper) {
        this.apiKeyMapper = Objects.requireNonNull(apiKeyMapper, "API Key Mapper 不能为空");
    }

    @Override
    public List<OpenApiApiKey> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return apiKeyMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(OpenApiApiKey apiKey) {
        apiKeyMapper.upsert(apiKey);
    }
}
