/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.mapper;

import com.zhyc.openapi.ratelimit.domain.OpenApiRateLimitPolicy;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开放 API 限流策略 MyBatis Mapper。
 */
@Mapper
public interface OpenApiRateLimitPolicyMapper {

    /**
     * 查询租户指定应用的限流策略列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 限流策略列表
     */
    @SelectProvider(type = OpenApiRateLimitPolicySqlProvider.class, method = "selectByTenantIdAndAppCode")
    List<OpenApiRateLimitPolicy> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                                             @Param("appCode") String appCode);

    /**
     * 保存或更新限流策略。
     *
     * @param policy 限流策略领域对象
     */
    @InsertProvider(type = OpenApiRateLimitPolicySqlProvider.class, method = "upsert")
    void upsert(OpenApiRateLimitPolicy policy);
}
