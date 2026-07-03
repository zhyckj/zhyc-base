/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.module.ModuleDescriptor;
import com.zhyc.common.module.ModuleRegistry;
import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.domain.SysModuleDependency;
import com.zhyc.system.module.domain.SysModuleResource;
import com.zhyc.system.module.repository.SysModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认系统模块业务服务实现。
 */
@Service
public class DefaultSysModuleService implements SysModuleService {

    /** 系统模块仓储。 */
    private final SysModuleRepository moduleRepository;

    /**
     * 创建默认系统模块业务服务。
     *
     * @param moduleRepository 系统模块仓储
     */
    public DefaultSysModuleService(SysModuleRepository moduleRepository) {
        this.moduleRepository = Objects.requireNonNull(moduleRepository, "系统模块仓储不能为空");
    }

    @Override
    public List<SysModuleResponse> listModules() {
        return moduleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ModuleRegistry moduleRegistry() {
        List<ModuleDescriptor> descriptors = moduleRepository.findAll().stream()
                .map(this::toDescriptor)
                .toList();
        return ModuleRegistry.of(descriptors);
    }

    @Override
    @Transactional
    public void syncModuleRegistry(ModuleRegistry registry) {
        Objects.requireNonNull(registry, "模块注册表不能为空").enabledModules()
                .forEach(this::syncModuleDescriptor);
    }

    @Override
    @Transactional
    public void changeEnabled(String moduleCode, boolean enabled) {
        String normalizedModuleCode = requireText(moduleCode, "模块编码不能为空");
        if (!enabled) {
            assertNoEnabledDependents(normalizedModuleCode);
        }
        moduleRepository.updateEnabled(normalizedModuleCode, enabled);
    }

    /**
     * 校验目标模块不存在启用状态的反向依赖。
     *
     * @param moduleCode 准备禁用的模块编码
     */
    private void assertNoEnabledDependents(String moduleCode) {
        List<String> dependentModules = moduleRepository.findAll().stream()
                .filter(SysModule::isEnabled)
                .filter(module -> !moduleCode.equals(module.getModuleCode()))
                .filter(module -> dependsOn(module.getModuleCode(), moduleCode))
                .map(SysModule::getModuleCode)
                .toList();
        if (!dependentModules.isEmpty()) {
            throw new BusinessException("MODULE_DEPENDENCY_CONFLICT",
                    "模块 " + moduleCode + " 仍被启用模块依赖：" + dependentModules);
        }
    }

    /**
     * 判断指定模块是否依赖目标模块。
     *
     * @param moduleCode 当前启用模块编码
     * @param dependencyCode 被依赖模块编码
     * @return 当前模块依赖目标模块时返回 {@code true}
     */
    private boolean dependsOn(String moduleCode, String dependencyCode) {
        return moduleRepository.findDependenciesByModuleCode(moduleCode).stream()
                .map(SysModuleDependency::getDependsOnCode)
                .anyMatch(dependencyCode::equals);
    }

    private void syncModuleDescriptor(ModuleDescriptor descriptor) {
        String moduleCode = requireText(descriptor.getCode(), "模块编码不能为空");
        moduleRepository.saveModule(new SysModule(null, moduleCode, requireText(descriptor.getName(),
                "模块名称不能为空"), defaultText(descriptor.getVersion(), "1.0.0"),
                defaultText(descriptor.getModuleType(), "module"), descriptor.isEnabled(), null, null));
        moduleRepository.replaceDependencies(moduleCode, descriptor.getDependencies().stream()
                .map(dependency -> requireText(dependency, "依赖模块编码不能为空"))
                .map(dependency -> new SysModuleDependency(null, moduleCode, dependency,
                        null, null))
                .toList());
        moduleRepository.replaceResources(moduleCode, moduleResources(descriptor));
    }

    private SysModuleResponse toResponse(SysModule module) {
        String moduleCode = module.getModuleCode();
        List<String> dependencies = moduleRepository.findDependenciesByModuleCode(moduleCode).stream()
                .map(SysModuleDependency::getDependsOnCode)
                .toList();
        List<SysModuleResourceResponse> resources = moduleRepository.findResourcesByModuleCode(moduleCode).stream()
                .map(this::toResourceResponse)
                .toList();
        return new SysModuleResponse(module.getModuleCode(), module.getModuleName(), module.getVersion(),
                module.getModuleType(), module.isEnabled(), dependencies, resources);
    }

    private SysModuleResourceResponse toResourceResponse(SysModuleResource resource) {
        return new SysModuleResourceResponse(resource.getResourceType(), resource.getResourceCode(),
                resource.getResourcePath());
    }

    private ModuleDescriptor toDescriptor(SysModule module) {
        String moduleCode = module.getModuleCode();
        ModuleDescriptor descriptor = new ModuleDescriptor();
        descriptor.setCode(moduleCode);
        descriptor.setName(module.getModuleName());
        descriptor.setVersion(module.getVersion());
        descriptor.setModuleType(module.getModuleType());
        descriptor.setEnabled(module.isEnabled());
        descriptor.setDependencies(moduleRepository.findDependenciesByModuleCode(moduleCode).stream()
                .map(SysModuleDependency::getDependsOnCode)
                .toList());
        List<SysModuleResource> resources = moduleRepository.findResourcesByModuleCode(moduleCode);
        descriptor.setMenus(filterResourceCodes(resources, "menu"));
        descriptor.setPermissions(filterResourceCodes(resources, "permission"));
        descriptor.setDictionaries(filterResourceCodes(resources, "dict"));
        descriptor.setGeneratorTemplates(filterResourceCodes(resources, "template"));
        return descriptor;
    }

    private List<String> filterResourceCodes(List<SysModuleResource> resources, String resourceType) {
        return resources.stream()
                .filter(resource -> resourceType.equals(resource.getResourceType()))
                .map(SysModuleResource::getResourceCode)
                .toList();
    }

    private List<SysModuleResource> moduleResources(ModuleDescriptor descriptor) {
        String moduleCode = descriptor.getCode();
        java.util.ArrayList<SysModuleResource> resources = new java.util.ArrayList<>();
        addResources(resources, moduleCode, "menu", descriptor.getMenus());
        addResources(resources, moduleCode, "permission", descriptor.getPermissions());
        addResources(resources, moduleCode, "dict", descriptor.getDictionaries());
        addResources(resources, moduleCode, "template", descriptor.getGeneratorTemplates());
        addResources(resources, moduleCode, "dbScript", descriptor.getDbScripts());
        addResources(resources, moduleCode, "extensionPoint", descriptor.getExtensionPoints());
        return resources;
    }

    private void addResources(List<SysModuleResource> resources, String moduleCode, String resourceType,
                              List<String> resourceCodes) {
        resourceCodes.stream()
                .map(resourceCode -> requireText(resourceCode, "模块资源编码不能为空"))
                .map(resourceCode -> new SysModuleResource(null, moduleCode, resourceType, resourceCode,
                        resourceCode, null))
                .forEach(resources::add);
    }

    private String defaultText(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }
}
