/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模块注册表测试。
 */
class ModuleRegistryTest {

    /**
     * 验证注册表只返回启用模块，并支持按模块编码查询。
     */
    @Test
    void shouldRegisterAndQueryEnabledModulesByCode() {
        ModuleDescriptor system = descriptor("system", true);
        ModuleDescriptor lowcode = descriptor("lowcode", true);
        ModuleDescriptor cms = descriptor("cms", false);

        ModuleRegistry registry = ModuleRegistry.of(List.of(system, lowcode, cms));

        assertEquals(List.of("system", "lowcode"),
                registry.enabledModules().stream().map(ModuleDescriptor::getCode).toList());
        Optional<ModuleDescriptor> found = registry.findByCode("lowcode");
        assertTrue(found.isPresent());
        assertEquals("lowcode", found.get().getCode());
        assertFalse(registry.findByCode("missing").isPresent());
    }

    /**
     * 验证重复模块编码会被拒绝，避免菜单、权限和数据库脚本重复注册。
     */
    @Test
    void shouldRejectDuplicatedModuleCode() {
        List<ModuleDescriptor> modules = List.of(descriptor("system", true), descriptor("system", false));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ModuleRegistry.of(modules));

        assertEquals("模块编码重复：system", exception.getMessage());
    }

    /**
     * 验证缺失依赖会被拒绝，避免模块启用后运行期缺少基础能力。
     */
    @Test
    void shouldRejectMissingDependency() {
        ModuleDescriptor lowcode = descriptor("lowcode", true);
        lowcode.setDependencies(List.of("system"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ModuleRegistry.of(List.of(lowcode)));

        assertEquals("模块 lowcode 依赖不存在：system", exception.getMessage());
    }

    /**
     * 验证停用依赖不能支撑启用模块，避免首期模块启停状态不一致。
     */
    @Test
    void shouldRejectDisabledDependencyForEnabledModule() {
        ModuleDescriptor system = descriptor("system", false);
        ModuleDescriptor lowcode = descriptor("lowcode", true);
        lowcode.setDependencies(List.of("system"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ModuleRegistry.of(List.of(system, lowcode)));

        assertEquals("模块 lowcode 依赖未启用：system", exception.getMessage());
    }

    private static ModuleDescriptor descriptor(String code, boolean enabled) {
        ModuleDescriptor descriptor = new ModuleDescriptor();
        descriptor.setCode(code);
        descriptor.setName(code + "模块");
        descriptor.setVersion("1.0.0");
        descriptor.setEnabled(enabled);
        return descriptor;
    }
}
