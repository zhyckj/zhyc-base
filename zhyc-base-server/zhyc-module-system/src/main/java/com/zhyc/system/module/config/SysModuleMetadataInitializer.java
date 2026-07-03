/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.config;

import com.zhyc.common.module.ModuleDescriptorClasspathLoader;
import com.zhyc.common.module.ModuleRegistry;
import com.zhyc.system.module.service.SysModuleService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 系统模块元数据启动初始化器。
 *
 * <p>平台启动后扫描 classpath 中的 {@code META-INF/zhyc-module.yml} 描述文件，并同步到系统模块表。
 * 该初始化器只负责模块元数据落库，不执行业务表 DDL、不加载插件代码，也不绕过模块依赖校验。</p>
 */
@Component
public class SysModuleMetadataInitializer implements ApplicationRunner {

    /** 系统模块业务服务。 */
    private final SysModuleService moduleService;

    /**
     * 创建系统模块元数据启动初始化器。
     *
     * @param moduleService 系统模块业务服务
     */
    public SysModuleMetadataInitializer(SysModuleService moduleService) {
        this.moduleService = Objects.requireNonNull(moduleService, "系统模块业务服务不能为空");
    }

    /**
     * 执行系统模块元数据初始化。
     *
     * <p>初始化过程复用公共模块注册表的依赖校验，校验通过后再同步模块、依赖和资源关系。</p>
     *
     * @param args Spring Boot 启动参数；当前初始化逻辑不依赖具体参数
     */
    @Override
    public void run(ApplicationArguments args) {
        ModuleRegistry registry = new ModuleDescriptorClasspathLoader().loadRegistry();
        moduleService.syncModuleRegistry(registry);
    }
}
