/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.service;

import java.util.List;

/**
 * 开放 API 限流策略业务服务。
 */
public interface OpenApiRateLimitPolicyService {

    /**
     * 查询租户指定应用的限流策略列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 限流策略列表
     */
    List<OpenApiRateLimitPolicyResponse> listPolicies(String tenantId, String appCode);

    /**
     * 保存或更新限流策略。
     *
     * @param command 限流策略保存命令
     */
    void save(OpenApiRateLimitPolicySaveCommand command);
}
