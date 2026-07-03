/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.domain;

import java.time.LocalDateTime;

/**
 * 系统租户套餐模型。
 */
public class SysTenantPackage {

    /** 数据库主键。 */
    private Long id;
    /** 套餐编码，平台全局唯一。 */
    private String packageCode;
    /** 套餐名称。 */
    private String packageName;
    /** 套餐状态，例如 enabled、disabled。 */
    private String status;
    /** 最大用户数，0 表示不限制。 */
    private Integer maxUserCount;
    /** 最大存储容量，单位 MB，0 表示不限制。 */
    private Integer maxStorageMb;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空租户套餐对象。
     */
    public SysTenantPackage() {
    }

    /**
     * 创建完整租户套餐对象。
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
    public SysTenantPackage(Long id, String packageCode, String packageName, String status,
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
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
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
     * 设置套餐编码。
     *
     * @param packageCode 套餐编码
     */
    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
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
     * 设置套餐名称。
     *
     * @param packageName 套餐名称
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
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
     * 设置套餐状态。
     *
     * @param status 套餐状态
     */
    public void setStatus(String status) {
        this.status = status;
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
     * 设置最大用户数。
     *
     * @param maxUserCount 最大用户数
     */
    public void setMaxUserCount(Integer maxUserCount) {
        this.maxUserCount = maxUserCount;
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
     * 设置最大存储容量，单位 MB。
     *
     * @param maxStorageMb 最大存储容量
     */
    public void setMaxStorageMb(Integer maxStorageMb) {
        this.maxStorageMb = maxStorageMb;
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
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
