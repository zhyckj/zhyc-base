/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.domain;

import java.time.LocalDateTime;

/**
 * 开放 API 限流策略领域对象。
 */
public class OpenApiRateLimitPolicy {

    /** 主键。 */
    private Long id;
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
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建开放 API 限流策略领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param apiCode 开放 API 业务编码
     * @param limitCount 时间窗口内允许的最大调用次数
     * @param windowSeconds 限流时间窗口，单位秒
     * @param status 限流策略状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiRateLimitPolicy(Long id, String tenantId, String appCode, String apiCode,
                                  int limitCount, int windowSeconds, String status,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.apiCode = apiCode;
        this.limitCount = limitCount;
        this.windowSeconds = windowSeconds;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
