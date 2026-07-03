/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.module.service.SysModuleResponse;
import com.zhyc.system.module.service.SysModuleService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统模块管理接口。
 */
@RestController
@RequestMapping("/system/modules")
public class SysModuleController {

    /** 系统模块启用状态请求缺失错误码。 */
    private static final String ERROR_ENABLED_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_MODULE_ENABLED_REQUEST_REQUIRED";

    /** 系统模块业务服务。 */
    private final SysModuleService moduleService;

    /**
     * 创建系统模块管理接口。
     *
     * @param moduleService 系统模块业务服务
     */
    public SysModuleController(SysModuleService moduleService) {
        this.moduleService = Objects.requireNonNull(moduleService, "系统模块业务服务不能为空");
    }

    /**
     * 查询系统模块清单。
     *
     * @return 系统模块清单
     */
    @RequiresPermissions("system:module:query")
    @GetMapping
    public ApiResult<List<SysModuleResponse>> listModules() {
        return ApiResult.ok(moduleService.listModules());
    }

    /**
     * 修改模块启用状态。
     *
     * @param moduleCode 模块编码
     * @param request 模块启停请求
     * @return 空响应
     */
    @RequiresPermissions("system:module:update")
    @PutMapping("/{moduleCode}/enabled")
    public ApiResult<Void> changeEnabled(@PathVariable("moduleCode") String moduleCode,
                                         @RequestBody SysModuleEnabledRequest request) {
        if (request == null || request.getEnabled() == null) {
            throw new BusinessException(ERROR_ENABLED_REQUEST_REQUIRED, "模块启用状态不能为空");
        }
        moduleService.changeEnabled(moduleCode, request.getEnabled());
        return ApiResult.ok(null);
    }
}
