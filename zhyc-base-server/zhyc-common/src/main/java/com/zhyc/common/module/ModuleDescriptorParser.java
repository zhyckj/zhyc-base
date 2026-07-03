/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块描述文件解析器。
 *
 * <p>用于解析低代码生成器输出的 `META-INF/zhyc-module.yml` 轻量模块描述文件。首期只支持平台约定的
 * 简单键值、字符串列表和扩展点名称，不作为通用 YAML 解析器使用。</p>
 */
public final class ModuleDescriptorParser {

    /** 依赖模块列表键。 */
    private static final String DEPENDENCIES = "dependencies";
    /** 菜单资源列表键。 */
    private static final String MENUS = "menus";
    /** 权限资源列表键。 */
    private static final String PERMISSIONS = "permissions";
    /** 字典资源列表键。 */
    private static final String DICTIONARIES = "dictionaries";
    /** 数据库脚本列表键。 */
    private static final String DATABASE_SCRIPTS = "databaseScripts";
    /** 生成模板列表键。 */
    private static final String GENERATOR_TEMPLATES = "generatorTemplates";
    /** 扩展点列表键。 */
    private static final String EXTENSION_POINTS = "extensionPoints";

    private ModuleDescriptorParser() {
    }

    /**
     * 解析模块描述文件内容。
     *
     * <p>解析结果用于模块注册、生成工程边界校验和后续插件化元数据同步。缺少模块编码会直接失败，
     * 避免注册不可识别模块。</p>
     *
     * @param content 模块描述文件文本
     * @return 模块描述对象
     */
    public static ModuleDescriptor parse(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("模块描述内容不能为空");
        }
        ModuleDescriptor descriptor = new ModuleDescriptor();
        List<String> dependencies = new ArrayList<>();
        List<String> menus = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        List<String> dictionaries = new ArrayList<>();
        List<String> dbScripts = new ArrayList<>();
        List<String> generatorTemplates = new ArrayList<>();
        List<String> extensionPoints = new ArrayList<>();
        String activeListKey = null;

        for (String rawLine : content.split("\\R")) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.endsWith(":") && !line.startsWith("-")) {
                activeListKey = line.substring(0, line.length() - 1).trim();
                continue;
            }
            if (line.startsWith("-")) {
                appendListValue(activeListKey, line, dependencies, menus, permissions, dictionaries,
                        dbScripts, generatorTemplates, extensionPoints);
                continue;
            }
            if (activeListKey != null && !rawLine.isBlank() && Character.isWhitespace(rawLine.charAt(0))
                    && line.contains(":")) {
                continue;
            }
            int separatorIndex = line.indexOf(':');
            if (separatorIndex <= 0) {
                continue;
            }
            activeListKey = null;
            String key = line.substring(0, separatorIndex).trim();
            String value = line.substring(separatorIndex + 1).trim();
            applyScalar(descriptor, key, value);
        }

        descriptor.setDependencies(dependencies);
        descriptor.setMenus(menus);
        descriptor.setPermissions(permissions);
        descriptor.setDictionaries(dictionaries);
        descriptor.setDbScripts(dbScripts);
        descriptor.setGeneratorTemplates(generatorTemplates);
        descriptor.setExtensionPoints(extensionPoints);
        validate(descriptor);
        return descriptor;
    }

    private static void applyScalar(ModuleDescriptor descriptor, String key, String value) {
        switch (key) {
            case "moduleCode" -> descriptor.setCode(value);
            case "moduleName" -> descriptor.setName(value);
            case "version" -> descriptor.setVersion(value);
            case "moduleType" -> descriptor.setModuleType(value);
            case "serviceName" -> descriptor.setServiceName(value);
            case "tenantMode" -> descriptor.setTenantMode(value);
            case "permissionPrefix" -> descriptor.setPermissionPrefix(value);
            case "backendPackage" -> descriptor.setBackendPackage(value);
            case "openApiGatewayRequired" -> descriptor.setOpenApiGatewayRequired(Boolean.parseBoolean(value));
            case "authServerRequired" -> descriptor.setAuthServerRequired(Boolean.parseBoolean(value));
            case "enabled" -> descriptor.setEnabled(Boolean.parseBoolean(value));
            default -> {
            }
        }
    }

    private static void appendListValue(String activeListKey, String line, List<String> dependencies,
                                        List<String> menus, List<String> permissions, List<String> dictionaries,
                                        List<String> dbScripts, List<String> generatorTemplates,
                                        List<String> extensionPoints) {
        if (activeListKey == null) {
            return;
        }
        String value = line.substring(1).trim();
        if (value.startsWith("name:")) {
            value = value.substring("name:".length()).trim();
        }
        switch (activeListKey) {
            case DEPENDENCIES -> dependencies.add(value);
            case MENUS -> menus.add(value);
            case PERMISSIONS -> permissions.add(value);
            case DICTIONARIES -> dictionaries.add(value);
            case DATABASE_SCRIPTS -> dbScripts.add(value);
            case GENERATOR_TEMPLATES -> generatorTemplates.add(value);
            case EXTENSION_POINTS -> extensionPoints.add(value);
            default -> {
            }
        }
    }

    private static String stripComment(String line) {
        int commentIndex = line.indexOf('#');
        if (commentIndex < 0) {
            return line;
        }
        return line.substring(0, commentIndex);
    }

    private static void validate(ModuleDescriptor descriptor) {
        if (descriptor.getCode() == null || descriptor.getCode().isBlank()) {
            throw new IllegalArgumentException("模块描述缺少 moduleCode");
        }
    }
}
