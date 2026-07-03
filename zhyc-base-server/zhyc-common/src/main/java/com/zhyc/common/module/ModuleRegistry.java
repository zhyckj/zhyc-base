/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 模块注册表。
 *
 * <p>用于在平台启动或模块元数据同步时集中校验模块编码、启用状态和依赖关系。该类只维护模块元数据视图，
 * 不负责模块安装、类加载或运行期热插拔。</p>
 */
public final class ModuleRegistry {

    /** 按模块编码建立的模块描述索引，保持注册顺序便于菜单和脚本按声明顺序处理。 */
    private final Map<String, ModuleDescriptor> modulesByCode;

    private ModuleRegistry(Map<String, ModuleDescriptor> modulesByCode) {
        this.modulesByCode = Collections.unmodifiableMap(new LinkedHashMap<>(modulesByCode));
    }

    /**
     * 创建模块注册表。
     *
     * <p>创建时会拒绝空编码、重复编码、缺失依赖，以及启用模块依赖未启用模块的情况，避免模块管理、
     * 菜单权限注册和数据库脚本执行出现不一致状态。</p>
     *
     * @param descriptors 模块描述列表；传入 {@code null} 时按空列表处理
     * @return 已校验的模块注册表
     */
    public static ModuleRegistry of(List<ModuleDescriptor> descriptors) {
        Map<String, ModuleDescriptor> modules = new LinkedHashMap<>();
        for (ModuleDescriptor descriptor : normalize(descriptors)) {
            String code = descriptor.getCode();
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("模块编码不能为空");
            }
            if (modules.containsKey(code)) {
                throw new IllegalArgumentException("模块编码重复：" + code);
            }
            modules.put(code, descriptor);
        }
        validateDependencies(modules);
        return new ModuleRegistry(modules);
    }

    /**
     * 根据模块编码查找模块描述。
     *
     * @param code 模块编码
     * @return 模块存在时返回模块描述，否则返回空
     */
    public Optional<ModuleDescriptor> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(modulesByCode.get(code));
    }

    /**
     * 返回所有启用模块。
     *
     * <p>返回顺序与模块注册顺序一致，方便后续菜单、权限、字典和数据库脚本按声明顺序执行。</p>
     *
     * @return 启用模块描述列表
     */
    public List<ModuleDescriptor> enabledModules() {
        return modulesByCode.values().stream()
                .filter(ModuleDescriptor::isEnabled)
                .toList();
    }

    private static List<ModuleDescriptor> normalize(List<ModuleDescriptor> descriptors) {
        if (descriptors == null) {
            return List.of();
        }
        return new ArrayList<>(descriptors);
    }

    private static void validateDependencies(Map<String, ModuleDescriptor> modules) {
        for (ModuleDescriptor descriptor : modules.values()) {
            for (String dependency : descriptor.getDependencies()) {
                ModuleDescriptor dependencyModule = modules.get(dependency);
                if (dependencyModule == null) {
                    throw new IllegalArgumentException("模块 " + descriptor.getCode() + " 依赖不存在：" + dependency);
                }
                if (descriptor.isEnabled() && !dependencyModule.isEnabled()) {
                    throw new IllegalArgumentException("模块 " + descriptor.getCode() + " 依赖未启用：" + dependency);
                }
            }
        }
    }
}
