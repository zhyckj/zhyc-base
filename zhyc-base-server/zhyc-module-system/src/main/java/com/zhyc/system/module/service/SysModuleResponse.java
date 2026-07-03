/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.service;

import java.util.List;

/**
 * 系统模块响应对象。
 */
public class SysModuleResponse {

    /** 模块编码。 */
    private final String moduleCode;
    /** 模块名称。 */
    private final String moduleName;
    /** 模块版本。 */
    private final String version;
    /** 模块类型。 */
    private final String moduleType;
    /** 是否启用。 */
    private final boolean enabled;
    /** 依赖模块编码列表。 */
    private final List<String> dependencies;
    /** 模块资源列表。 */
    private final List<SysModuleResourceResponse> resources;

    /**
     * 创建系统模块响应对象。
     *
     * @param moduleCode 模块编码
     * @param moduleName 模块名称
     * @param version 模块版本
     * @param moduleType 模块类型
     * @param enabled 是否启用
     * @param dependencies 依赖模块编码列表
     * @param resources 模块资源列表
     */
    public SysModuleResponse(String moduleCode, String moduleName, String version, String moduleType,
                             boolean enabled, List<String> dependencies,
                             List<SysModuleResourceResponse> resources) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.version = version;
        this.moduleType = moduleType;
        this.enabled = enabled;
        this.dependencies = List.copyOf(dependencies);
        this.resources = List.copyOf(resources);
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
     * 返回模块名称。
     *
     * @return 模块名称
     */
    public String getModuleName() {
        return moduleName;
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
     * 返回模块类型。
     *
     * @return 模块类型
     */
    public String getModuleType() {
        return moduleType;
    }

    /**
     * 返回是否启用。
     *
     * @return 启用返回 {@code true}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 返回依赖模块编码列表。
     *
     * @return 依赖模块编码列表
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    /**
     * 返回模块资源列表。
     *
     * @return 模块资源列表
     */
    public List<SysModuleResourceResponse> getResources() {
        return resources;
    }
}
