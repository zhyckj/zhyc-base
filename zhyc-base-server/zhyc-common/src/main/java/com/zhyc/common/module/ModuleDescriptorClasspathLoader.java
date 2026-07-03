/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * 模块描述 classpath 加载器。
 *
 * <p>用于扫描各模块或生成工程中的 {@code META-INF/zhyc-module.yml} 描述文件，并转换为公共模块注册表。
 * 首期用于编译期模块和生成模块的元数据发现，不承担运行期插件热加载职责。</p>
 */
public class ModuleDescriptorClasspathLoader {

    /** 平台约定的模块描述资源路径。 */
    public static final String DESCRIPTOR_RESOURCE = "META-INF/zhyc-module.yml";

    /** 用于扫描模块描述资源的类加载器。 */
    private final ClassLoader classLoader;

    /**
     * 创建基于当前线程上下文类加载器的模块描述加载器。
     */
    public ModuleDescriptorClasspathLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 创建模块描述加载器。
     *
     * @param classLoader 类加载器；不能为空
     */
    public ModuleDescriptorClasspathLoader(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "模块描述类加载器不能为空");
    }

    /**
     * 加载所有模块描述。
     *
     * @return 模块描述列表，顺序与 classpath 资源枚举顺序一致
     */
    public List<ModuleDescriptor> loadDescriptors() {
        try {
            Enumeration<URL> resources = classLoader.getResources(DESCRIPTOR_RESOURCE);
            List<ModuleDescriptor> descriptors = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                descriptors.add(ModuleDescriptorParser.parse(readResource(resource)));
            }
            return descriptors;
        } catch (IOException exception) {
            throw new UncheckedIOException("扫描模块描述资源失败", exception);
        }
    }

    /**
     * 加载并校验模块注册表。
     *
     * @return 已校验的模块注册表
     */
    public ModuleRegistry loadRegistry() {
        return ModuleRegistry.of(loadDescriptors());
    }

    private String readResource(URL resource) throws IOException {
        try (var inputStream = resource.openStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
