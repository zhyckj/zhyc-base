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
import com.zhyc.system.module.mapper.SysModuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统模块仓储实现。
 */
@Repository
public class MyBatisSysModuleRepository implements SysModuleRepository {

    /** 系统模块 Mapper。 */
    private final SysModuleMapper moduleMapper;

    /**
     * 创建系统模块仓储实现。
     *
     * @param moduleMapper 系统模块 Mapper
     */
    public MyBatisSysModuleRepository(SysModuleMapper moduleMapper) {
        this.moduleMapper = Objects.requireNonNull(moduleMapper, "系统模块 Mapper 不能为空");
    }

    @Override
    public List<SysModule> findAll() {
        return moduleMapper.selectAllModules();
    }

    @Override
    public List<SysModuleDependency> findDependenciesByModuleCode(String moduleCode) {
        return moduleMapper.selectDependenciesByModuleCode(moduleCode);
    }

    @Override
    public List<SysModuleResource> findResourcesByModuleCode(String moduleCode) {
        return moduleMapper.selectResourcesByModuleCode(moduleCode);
    }

    @Override
    public void saveModule(SysModule module) {
        int updatedRows = moduleMapper.updateModule(module);
        if (updatedRows == 0) {
            moduleMapper.insertModule(module);
        }
    }

    @Override
    public void replaceDependencies(String moduleCode, List<SysModuleDependency> dependencies) {
        moduleMapper.deleteDependenciesByModuleCode(moduleCode);
        dependencies.forEach(moduleMapper::insertDependency);
    }

    @Override
    public void replaceResources(String moduleCode, List<SysModuleResource> resources) {
        moduleMapper.deleteResourcesByModuleCode(moduleCode);
        resources.forEach(moduleMapper::insertResource);
    }

    @Override
    public void updateEnabled(String moduleCode, boolean enabled) {
        moduleMapper.updateEnabled(moduleCode, enabled);
    }
}
