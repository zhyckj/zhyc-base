/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.domain;

import java.time.LocalDateTime;

/**
 * 系统模块领域模型。
 *
 * <p>系统模块是平台微内核和插件架构的注册单元，不按租户隔离。</p>
 */
public class SysModule {

    /** 数据库主键。 */
    private Long id;
    /** 模块编码，全平台唯一。 */
    private String moduleCode;
    /** 模块名称。 */
    private String moduleName;
    /** 模块版本。 */
    private String version;
    /** 模块类型，例如 core、business、plugin。 */
    private String moduleType;
    /** 是否启用。 */
    private boolean enabled;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空系统模块对象。
     */
    public SysModule() {
    }

    /**
     * 创建完整系统模块对象。
     *
     * @param id 数据库主键
     * @param moduleCode 模块编码
     * @param moduleName 模块名称
     * @param version 模块版本
     * @param moduleType 模块类型
     * @param enabled 是否启用
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysModule(Long id, String moduleCode, String moduleName, String version, String moduleType,
                     boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.version = version;
        this.moduleType = moduleType;
        this.enabled = enabled;
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
     * 返回模块名称。
     *
     * @return 模块名称
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * 设置模块名称。
     *
     * @param moduleName 模块名称
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * 返回模块版本。
     *
     * @return 模块版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置模块版本。
     *
     * @param version 模块版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 返回模块类型。
     *
     * @return 模块类型
     */
    public String getModuleType() {
        return moduleType;
    }

    /**
     * 设置模块类型。
     *
     * @param moduleType 模块类型
     */
    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * 返回模块是否启用。
     *
     * @return 启用返回 {@code true}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置模块是否启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
