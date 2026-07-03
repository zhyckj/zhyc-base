/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.module.ModuleDescriptor;
import com.zhyc.common.module.ModuleRegistry;
import com.zhyc.system.module.domain.SysModule;
import com.zhyc.system.module.domain.SysModuleDependency;
import com.zhyc.system.module.domain.SysModuleResource;
import com.zhyc.system.module.repository.SysModuleRepository;
import com.zhyc.system.module.service.DefaultSysModuleService;
import com.zhyc.system.module.service.SysModuleResponse;
import com.zhyc.system.module.service.SysModuleService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统模块业务服务测试。
 */
class SysModuleServiceTest {

    /**
     * 验证模块服务会聚合模块依赖和资源清单，供模块管理页面展示。
     */
    @Test
    void shouldListModulesWithDependenciesAndResources() {
        RecordingModuleRepository repository = new RecordingModuleRepository();
        SysModuleService service = new DefaultSysModuleService(repository);

        List<SysModuleResponse> modules = service.listModules();

        assertEquals(1, modules.size());
        assertEquals("system", modules.get(0).getModuleCode());
        assertEquals(List.of("common"), modules.get(0).getDependencies());
        assertEquals(2, modules.get(0).getResources().size());
        assertEquals("menu", modules.get(0).getResources().get(0).getResourceType());
    }

    /**
     * 验证模块启停会裁剪模块编码，并透传启用状态。
     */
    @Test
    void shouldChangeModuleEnabledStatus() {
        RecordingModuleRepository repository = new RecordingModuleRepository();
        SysModuleService service = new DefaultSysModuleService(repository);

        service.changeEnabled(" system ", false);

        assertEquals("system", repository.lastModuleCode);
        assertFalse(repository.lastEnabled);
    }

    /**
     * 验证禁用模块前会检查启用模块依赖，避免关闭仍被依赖的核心模块。
     */
    @Test
    void shouldRejectDisableWhenEnabledModuleDependsOnIt() {
        RecordingModuleRepository repository = new RecordingModuleRepository();
        repository.modules = List.of(
                new SysModule(1L, "common", "公共能力", "1.0.0", "core", true,
                        LocalDateTime.now(), LocalDateTime.now()),
                new SysModule(2L, "system", "系统管理", "1.0.0", "core", true,
                        LocalDateTime.now(), LocalDateTime.now()));
        SysModuleService service = new DefaultSysModuleService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changeEnabled("common", false));

        assertEquals("MODULE_DEPENDENCY_CONFLICT", exception.getCode());
        assertTrue(exception.getMessage().contains("system"));
        assertNull(repository.lastModuleCode);
    }

    /**
     * 验证模块服务可以把系统模块表转换为公共模块注册表，供微内核和插件元数据校验复用。
     */
    @Test
    void shouldBuildModuleRegistryFromSystemModules() {
        RecordingModuleRepository repository = new RecordingModuleRepository();
        repository.modules = List.of(
                new SysModule(1L, "common", "公共能力", "1.0.0", "core", true,
                        LocalDateTime.now(), LocalDateTime.now()),
                new SysModule(2L, "system", "系统管理", "1.0.0", "core", true,
                        LocalDateTime.now(), LocalDateTime.now()));
        SysModuleService service = new DefaultSysModuleService(repository);

        ModuleRegistry registry = service.moduleRegistry();

        assertTrue(registry.findByCode("system").isPresent());
        assertEquals(List.of("common", "system"),
                registry.enabledModules().stream().map(descriptor -> descriptor.getCode()).toList());
        assertEquals(List.of("system:config"), registry.findByCode("system").orElseThrow().getMenus());
        assertEquals(List.of("system:module:query"),
                registry.findByCode("system").orElseThrow().getPermissions());
    }

    /**
     * 验证模块服务可以把 classpath 模块描述同步到系统模块表，形成首期模块安装初始化入口。
     */
    @Test
    void shouldSynchronizeModuleRegistryIntoSystemModuleTables() {
        RecordingModuleRepository repository = new RecordingModuleRepository();
        SysModuleService service = new DefaultSysModuleService(repository);
        ModuleDescriptor descriptor = new ModuleDescriptor();
        descriptor.setCode("workflow");
        descriptor.setName("工作流引擎");
        descriptor.setVersion("1.0.0");
        descriptor.setModuleType("extension");
        descriptor.setEnabled(true);
        descriptor.setDependencies(List.of("common", "system"));
        descriptor.setMenus(List.of("workflow:task"));
        descriptor.setPermissions(List.of("workflow:task:approve"));

        service.syncModuleRegistry(ModuleRegistry.of(List.of(
                descriptor("common", "公共能力", List.of(), List.of(), List.of()),
                descriptor("system", "系统管理", List.of("common"), List.of("system:module"), List.of()),
                descriptor)));

        assertEquals(List.of("common", "system", "workflow"), repository.savedModuleCodes);
        assertTrue(repository.savedDependencies.contains("system->common"));
        assertTrue(repository.savedDependencies.contains("workflow->common"));
        assertTrue(repository.savedDependencies.contains("workflow->system"));
        assertTrue(repository.savedResources.contains("workflow:menu:workflow:task"));
        assertTrue(repository.savedResources.contains("workflow:permission:workflow:task:approve"));
    }

    private ModuleDescriptor descriptor(String code, String name, List<String> dependencies,
                                        List<String> menus, List<String> permissions) {
        ModuleDescriptor descriptor = new ModuleDescriptor();
        descriptor.setCode(code);
        descriptor.setName(name);
        descriptor.setVersion("1.0.0");
        descriptor.setModuleType("core");
        descriptor.setEnabled(true);
        descriptor.setDependencies(dependencies);
        descriptor.setMenus(menus);
        descriptor.setPermissions(permissions);
        return descriptor;
    }

    /**
     * 测试用系统模块仓储。
     */
    private static class RecordingModuleRepository implements SysModuleRepository {

        /** 最近一次启停的模块编码。 */
        private String lastModuleCode;
        /** 最近一次启停状态。 */
        private boolean lastEnabled = true;
        /** 测试用系统模块清单。 */
        private List<SysModule> modules = List.of(new SysModule(1L, "system", "系统管理", "1.0.0",
                "core", true, LocalDateTime.now(), LocalDateTime.now()));
        /** 已保存的模块编码。 */
        private final List<String> savedModuleCodes = new java.util.ArrayList<>();
        /** 已保存的模块依赖。 */
        private final List<String> savedDependencies = new java.util.ArrayList<>();
        /** 已保存的模块资源。 */
        private final List<String> savedResources = new java.util.ArrayList<>();

        @Override
        public List<SysModule> findAll() {
            return modules;
        }

        @Override
        public List<SysModuleDependency> findDependenciesByModuleCode(String moduleCode) {
            if ("common".equals(moduleCode)) {
                return List.of();
            }
            return List.of(new SysModuleDependency(1L, moduleCode, "common",
                    "1.0.0", LocalDateTime.now()));
        }

        @Override
        public List<SysModuleResource> findResourcesByModuleCode(String moduleCode) {
            if ("common".equals(moduleCode)) {
                return List.of();
            }
            return List.of(
                    new SysModuleResource(1L, moduleCode, "menu", "system:config",
                            "/system/config", LocalDateTime.now()),
                    new SysModuleResource(2L, moduleCode, "permission", "system:module:query",
                            "system:module:query", LocalDateTime.now()));
        }

        @Override
        public void updateEnabled(String moduleCode, boolean enabled) {
            lastModuleCode = moduleCode;
            lastEnabled = enabled;
        }

        @Override
        public void saveModule(SysModule module) {
            savedModuleCodes.add(module.getModuleCode());
        }

        @Override
        public void replaceDependencies(String moduleCode, List<SysModuleDependency> dependencies) {
            dependencies.forEach(dependency ->
                    savedDependencies.add(moduleCode + "->" + dependency.getDependsOnCode()));
        }

        @Override
        public void replaceResources(String moduleCode, List<SysModuleResource> resources) {
            resources.forEach(resource -> savedResources.add(moduleCode + ":" + resource.getResourceType()
                    + ":" + resource.getResourceCode()));
        }
    }
}
