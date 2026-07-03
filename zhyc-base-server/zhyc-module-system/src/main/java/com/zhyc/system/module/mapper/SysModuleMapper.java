/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.mapper;

import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.domain.SysModuleDependency;
import com.zhyc.system.module.domain.SysModuleResource;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统模块 MyBatis Mapper。
 */
@Mapper
public interface SysModuleMapper {

    /**
     * 查询全部系统模块。
     *
     * @return 系统模块列表
     */
    @SelectProvider(type = SysModuleSqlProvider.class, method = "selectAllModules")
    List<SysModule> selectAllModules();

    /**
     * 查询指定模块依赖列表。
     *
     * @param moduleCode 模块编码
     * @return 模块依赖列表
     */
    @SelectProvider(type = SysModuleSqlProvider.class, method = "selectDependenciesByModuleCode")
    List<SysModuleDependency> selectDependenciesByModuleCode(@Param("moduleCode") String moduleCode);

    /**
     * 查询指定模块资源列表。
     *
     * @param moduleCode 模块编码
     * @return 模块资源列表
     */
    @SelectProvider(type = SysModuleSqlProvider.class, method = "selectResourcesByModuleCode")
    List<SysModuleResource> selectResourcesByModuleCode(@Param("moduleCode") String moduleCode);

    /**
     * 插入系统模块基础信息。
     *
     * @param module 系统模块基础信息
     */
    @InsertProvider(type = SysModuleSqlProvider.class, method = "insertModule")
    void insertModule(SysModule module);

    /**
     * 更新系统模块基础信息。
     *
     * @param module 系统模块基础信息
     * @return 被更新的行数
     */
    @UpdateProvider(type = SysModuleSqlProvider.class, method = "updateModule")
    int updateModule(SysModule module);

    /**
     * 删除指定模块的依赖关系。
     *
     * @param moduleCode 模块编码
     */
    @DeleteProvider(type = SysModuleSqlProvider.class, method = "deleteDependenciesByModuleCode")
    void deleteDependenciesByModuleCode(@Param("moduleCode") String moduleCode);

    /**
     * 插入系统模块依赖关系。
     *
     * @param dependency 系统模块依赖关系
     */
    @InsertProvider(type = SysModuleSqlProvider.class, method = "insertDependency")
    void insertDependency(SysModuleDependency dependency);

    /**
     * 删除指定模块的资源关系。
     *
     * @param moduleCode 模块编码
     */
    @DeleteProvider(type = SysModuleSqlProvider.class, method = "deleteResourcesByModuleCode")
    void deleteResourcesByModuleCode(@Param("moduleCode") String moduleCode);

    /**
     * 插入系统模块资源关系。
     *
     * @param resource 系统模块资源关系
     */
    @InsertProvider(type = SysModuleSqlProvider.class, method = "insertResource")
    void insertResource(SysModuleResource resource);

    /**
     * 更新模块启用状态。
     *
     * @param moduleCode 模块编码
     * @param enabled 是否启用
     */
    @UpdateProvider(type = SysModuleSqlProvider.class, method = "updateEnabled")
    void updateEnabled(@Param("moduleCode") String moduleCode, @Param("enabled") boolean enabled);
}
