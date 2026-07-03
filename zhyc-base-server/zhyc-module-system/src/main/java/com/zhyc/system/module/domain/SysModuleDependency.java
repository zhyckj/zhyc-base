/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.domain;

import java.time.LocalDateTime;

/**
 * 系统模块依赖领域模型。
 */
public class SysModuleDependency {

    /** 数据库主键。 */
    private Long id;
    /** 模块编码。 */
    private String moduleCode;
    /** 依赖模块编码。 */
    private String dependsOnCode;
    /** 依赖模块要求版本。 */
    private String requiredVersion;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空系统模块依赖对象。
     */
    public SysModuleDependency() {
    }

    /**
     * 创建完整系统模块依赖对象。
     *
     * @param id 数据库主键
     * @param moduleCode 模块编码
     * @param dependsOnCode 依赖模块编码
     * @param requiredVersion 依赖模块要求版本
     * @param createdAt 创建时间
     */
    public SysModuleDependency(Long id, String moduleCode, String dependsOnCode, String requiredVersion,
                               LocalDateTime createdAt) {
        this.id = id;
        this.moduleCode = moduleCode;
        this.dependsOnCode = dependsOnCode;
        this.requiredVersion = requiredVersion;
        this.createdAt = createdAt;
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
     * 返回模块编码。
     *
     * @return 模块编码
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * 设置模块编码。
     *
     * @param moduleCode 模块编码
     */
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    /**
     * 返回依赖模块编码。
     *
     * @return 依赖模块编码
     */
    public String getDependsOnCode() {
        return dependsOnCode;
    }

    /**
     * 设置依赖模块编码。
     *
     * @param dependsOnCode 依赖模块编码
     */
    public void setDependsOnCode(String dependsOnCode) {
        this.dependsOnCode = dependsOnCode;
    }

    /**
     * 返回依赖模块要求版本。
     *
     * @return 依赖模块要求版本
     */
    public String getRequiredVersion() {
        return requiredVersion;
    }

    /**
     * 设置依赖模块要求版本。
     *
     * @param requiredVersion 依赖模块要求版本
     */
    public void setRequiredVersion(String requiredVersion) {
        this.requiredVersion = requiredVersion;
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
}
