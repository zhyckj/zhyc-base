/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.service;

import java.time.LocalDateTime;

/**
 * 系统租户套餐响应对象。
 */
public class SysTenantPackageResponse {

    /** 数据库主键。 */
    private final Long id;
    /** 套餐编码。 */
    private final String packageCode;
    /** 套餐名称。 */
    private final String packageName;
    /** 套餐状态。 */
    private final String status;
    /** 最大用户数。 */
    private final Integer maxUserCount;
    /** 最大存储容量，单位 MB。 */
    private final Integer maxStorageMb;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    /**
     * 创建系统租户套餐响应对象。
     *
     * @param id 数据库主键
     * @param packageCode 套餐编码
     * @param packageName 套餐名称
     * @param status 套餐状态
     * @param maxUserCount 最大用户数
     * @param maxStorageMb 最大存储容量，单位 MB
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysTenantPackageResponse(Long id, String packageCode, String packageName, String status,
                                    Integer maxUserCount, Integer maxStorageMb,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.packageCode = packageCode;
        this.packageName = packageName;
        this.status = status;
        this.maxUserCount = maxUserCount;
        this.maxStorageMb = maxStorageMb;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回套餐编码。
     *
     * @return 套餐编码
     */
    public String getPackageCode() {
        return packageCode;
    }

    /**
     * 返回套餐名称。
     *
     * @return 套餐名称
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * 返回套餐状态。
     *
     * @return 套餐状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 返回最大用户数。
     *
     * @return 最大用户数
     */
    public Integer getMaxUserCount() {
        return maxUserCount;
    }

    /**
     * 返回最大存储容量，单位 MB。
     *
     * @return 最大存储容量
     */
    public Integer getMaxStorageMb() {
        return maxStorageMb;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
