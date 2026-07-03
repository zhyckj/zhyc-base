/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.service;

/**
 * 租户套餐创建命令。
 */
public class TenantPackageCreateCommand {

    /** 套餐编码，平台全局唯一。 */
    private final String packageCode;
    /** 套餐名称，用于后台管理端展示。 */
    private final String packageName;
    /** 套餐状态，默认使用 enabled。 */
    private final String status;
    /** 最大用户数，0 表示不限制。 */
    private final Integer maxUserCount;
    /** 最大存储容量，单位 MB，0 表示不限制。 */
    private final Integer maxStorageMb;

    /**
     * 创建租户套餐创建命令。
     *
     * @param packageCode 套餐编码
     * @param packageName 套餐名称
     * @param status 套餐状态
     * @param maxUserCount 最大用户数
     * @param maxStorageMb 最大存储容量，单位 MB
     */
    public TenantPackageCreateCommand(String packageCode, String packageName, String status,
                                      Integer maxUserCount, Integer maxStorageMb) {
        this.packageCode = packageCode;
        this.packageName = packageName;
        this.status = status;
        this.maxUserCount = maxUserCount;
        this.maxStorageMb = maxStorageMb;
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
     * 返回最大存储容量。
     *
     * @return 最大存储容量，单位 MB
     */
    public Integer getMaxStorageMb() {
        return maxStorageMb;
    }
}
