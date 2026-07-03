/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.repository;

import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.domain.SysModuleDependency;
import com.zhyc.system.module.domain.SysModuleResource;

import java.util.List;

/**
 * 系统模块仓储。
 */
public interface SysModuleRepository {

    /**
     * 查询全部系统模块。
     *
     * @return 系统模块列表
     */
    List<SysModule> findAll();

    /**
     * 查询指定模块依赖列表。
     *
     * @param moduleCode 模块编码
     * @return 模块依赖列表
     */
    List<SysModuleDependency> findDependenciesByModuleCode(String moduleCode);

    /**
     * 查询指定模块资源列表。
     *
     * @param moduleCode 模块编码
     * @return 模块资源列表
     */
    List<SysModuleResource> findResourcesByModuleCode(String moduleCode);

    /**
     * 保存或更新系统模块基础信息。
     *
     * @param module 系统模块基础信息
     */
    void saveModule(SysModule module);

    /**
     * 替换指定模块的依赖关系。
     *
     * <p>替换操作必须限定在模块编码范围内，禁止无条件删除其他模块依赖。</p>
     *
     * @param moduleCode 模块编码
     * @param dependencies 模块依赖列表
     */
    void replaceDependencies(String moduleCode, List<SysModuleDependency> dependencies);

    /**
     * 替换指定模块的资源关系。
     *
     * <p>替换操作必须限定在模块编码范围内，禁止无条件删除其他模块资源。</p>
     *
     * @param moduleCode 模块编码
     * @param resources 模块资源列表
     */
    void replaceResources(String moduleCode, List<SysModuleResource> resources);

    /**
     * 更新模块启用状态。
     *
     * @param moduleCode 模块编码
     * @param enabled 是否启用
     */
    void updateEnabled(String moduleCode, boolean enabled);
}
