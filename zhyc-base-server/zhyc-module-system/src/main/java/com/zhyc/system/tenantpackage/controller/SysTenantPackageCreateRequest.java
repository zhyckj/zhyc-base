/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.controller;

/**
 * 系统租户套餐创建请求。
 */
public class SysTenantPackageCreateRequest {

    /** 套餐编码，平台全局唯一。 */
    private String packageCode;
    /** 套餐名称，用于后台管理端展示。 */
    private String packageName;
    /** 套餐状态，默认使用 enabled。 */
    private String status;
    /** 最大用户数，0 表示不限制。 */
    private Integer maxUserCount;
    /** 最大存储容量，单位 MB，0 表示不限制。 */
    private Integer maxStorageMb;

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
     * 返回最大存储容量。
     *
     * @return 最大存储容量，单位 MB
     */
    public Integer getMaxStorageMb() {
        return maxStorageMb;
    }

    /**
     * 设置最大存储容量。
     *
     * @param maxStorageMb 最大存储容量，单位 MB
     */
    public void setMaxStorageMb(Integer maxStorageMb) {
        this.maxStorageMb = maxStorageMb;
    }
}
