/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.repository;

import com.zhyc.openapi.ratelimit.domain.OpenApiRateLimitPolicy;
import com.zhyc.openapi.ratelimit.mapper.OpenApiRateLimitPolicyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放 API 限流策略仓储实现。
 */
@Repository
public class MyBatisOpenApiRateLimitPolicyRepository implements OpenApiRateLimitPolicyRepository {

    /** 限流策略 Mapper。 */
    private final OpenApiRateLimitPolicyMapper rateLimitPolicyMapper;

    /**
     * 创建限流策略仓储实现。
     *
     * @param rateLimitPolicyMapper 限流策略 Mapper
     */
    public MyBatisOpenApiRateLimitPolicyRepository(OpenApiRateLimitPolicyMapper rateLimitPolicyMapper) {
        this.rateLimitPolicyMapper = Objects.requireNonNull(rateLimitPolicyMapper, "限流策略 Mapper 不能为空");
    }

    @Override
    public List<OpenApiRateLimitPolicy> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return rateLimitPolicyMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(OpenApiRateLimitPolicy policy) {
        rateLimitPolicyMapper.upsert(policy);
    }
}
