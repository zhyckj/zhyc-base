/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.domain;

import java.time.LocalDateTime;

/**
 * 系统模块资源领域模型。
 */
public class SysModuleResource {

    /** 数据库主键。 */
    private Long id;
    /** 模块编码。 */
    private String moduleCode;
    /** 资源类型，例如 menu、permission、dict、route、template。 */
    private String resourceType;
    /** 资源编码。 */
    private String resourceCode;
    /** 资源路径或权限标识。 */
    private String resourcePath;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空系统模块资源对象。
     */
    public SysModuleResource() {
    }

    /**
     * 创建完整系统模块资源对象。
     *
     * @param id 数据库主键
     * @param moduleCode 模块编码
     * @param resourceType 资源类型
     * @param resourceCode 资源编码
     * @param resourcePath 资源路径或权限标识
     * @param createdAt 创建时间
     */
    public SysModuleResource(Long id, String moduleCode, String resourceType, String resourceCode,
                             String resourcePath, LocalDateTime createdAt) {
        this.id = id;
        this.moduleCode = moduleCode;
        this.resourceType = resourceType;
        this.resourceCode = resourceCode;
        this.resourcePath = resourcePath;
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
     * 返回资源类型。
     *
     * @return 资源类型
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * 设置资源类型。
     *
     * @param resourceType 资源类型
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * 返回资源编码。
     *
     * @return 资源编码
     */
    public String getResourceCode() {
        return resourceCode;
    }

    /**
     * 设置资源编码。
     *
     * @param resourceCode 资源编码
     */
    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    /**
     * 返回资源路径或权限标识。
     *
     * @return 资源路径或权限标识
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * 设置资源路径或权限标识。
     *
     * @param resourcePath 资源路径或权限标识
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
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
