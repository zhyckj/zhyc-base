/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.system.loginlog.service.SysLoginLogResponse;
import com.zhyc.system.loginlog.service.SysLoginLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统登录日志接口。
 */
@RestController
@RequestMapping("/system/login-logs")
public class SysLoginLogController {

    /** 系统登录日志业务服务。 */
    private final SysLoginLogService loginLogService;

    /**
     * 创建系统登录日志接口。
     *
     * @param loginLogService 系统登录日志业务服务
     */
    public SysLoginLogController(SysLoginLogService loginLogService) {
        this.loginLogService = Objects.requireNonNull(loginLogService, "系统登录日志业务服务不能为空");
    }

    /**
     * 查询租户最近登录日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近登录日志列表
     */
    @RequiresPermissions("system:audit:query")
    @GetMapping("/recent")
    public ApiResult<List<SysLoginLogResponse>> listRecent(@RequestParam("tenantId") String tenantId,
                                                           @RequestParam(value = "limit", defaultValue = "50")
                                                           int limit) {
        return ApiResult.ok(loginLogService.listRecent(tenantId, limit));
    }
}
