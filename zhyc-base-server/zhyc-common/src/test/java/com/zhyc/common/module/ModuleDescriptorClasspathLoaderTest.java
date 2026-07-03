/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模块描述 classpath 加载器测试。
 */
class ModuleDescriptorClasspathLoaderTest {

    /** 临时模块资源根目录。 */
    @TempDir
    private Path tempDir;

    /**
     * 验证加载器可以扫描 classpath 中的模块描述文件并构建模块注册表。
     *
     * @throws IOException 写入测试模块描述文件失败时抛出
     */
    @Test
    void shouldLoadModuleDescriptorsFromClasspathResources() throws IOException {
        Path commonRoot = writeDescriptor("common", "公共能力", "", true);
        Path systemRoot = writeDescriptor("system", "系统管理", "  - common\n", true);
        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{commonRoot.toUri().toURL(), systemRoot.toUri().toURL()}, null)) {
            ModuleDescriptorClasspathLoader loader = new ModuleDescriptorClasspathLoader(classLoader);

            ModuleRegistry registry = loader.loadRegistry();

            assertTrue(registry.findByCode("system").isPresent());
            assertEquals(List.of("common", "system"),
                    registry.enabledModules().stream().map(ModuleDescriptor::getCode).toList());
            assertEquals(List.of("common"), registry.findByCode("system").orElseThrow().getDependencies());
        }
    }

    private Path writeDescriptor(String code, String name, String dependencies, boolean enabled) throws IOException {
        Path moduleRoot = tempDir.resolve(code);
        Path descriptorPath = moduleRoot.resolve("META-INF/zhyc-module.yml");
        Files.createDirectories(descriptorPath.getParent());
        String content = """
                moduleCode: %s
                moduleName: %s
                version: 1.0.0
                moduleType: core
                enabled: %s
                dependencies:
                %s
                """.formatted(code, name, enabled, dependencies);
        Files.writeString(descriptorPath, content, StandardCharsets.UTF_8);
        return moduleRoot;
    }
}
