/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.controller;

/**
 * 开放 API 限流策略保存请求。
 */
public class OpenApiRateLimitPolicySaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** 开放 API 业务编码。 */
    private String apiCode;
    /** 时间窗口内允许的最大调用次数。 */
    private int limitCount;
    /** 限流时间窗口，单位秒。 */
    private int windowSeconds;
    /** 限流策略状态。 */
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getApiCode() {
        return apiCode;
    }

    public int getLimitCount() {
        return limitCount;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public String getStatus() {
        return status;
    }
}
