/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.service;

import com.zhyc.common.module.ModuleRegistry;

import java.util.List;

/**
 * 系统模块业务服务。
 */
public interface SysModuleService {

    /**
     * 查询系统模块清单。
     *
     * @return 系统模块清单
     */
    List<SysModuleResponse> listModules();

    /**
     * 构建公共模块注册表。
     *
     * <p>用于把系统模块表中的模块、依赖和资源转换为微内核公共契约，供模块启停校验、插件元数据同步和
     * 后续生成模块注册使用。</p>
     *
     * @return 已校验的公共模块注册表
     */
    ModuleRegistry moduleRegistry();

    /**
     * 同步公共模块注册表到系统模块表。
     *
     * <p>用于平台启动初始化或模块安装时，把 classpath 模块描述落库为模块、依赖、菜单、权限、字典和模板资源。
     * 同步前会复用公共模块注册表的依赖校验结果，避免启用模块依赖缺失或依赖未启用。</p>
     *
     * @param registry 已校验的公共模块注册表
     */
    void syncModuleRegistry(ModuleRegistry registry);

    /**
     * 修改模块启用状态。
     *
     * @param moduleCode 模块编码
     * @param enabled 是否启用
     */
    void changeEnabled(String moduleCode, boolean enabled);
}
