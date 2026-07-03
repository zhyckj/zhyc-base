/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.system.exceptionlog.service.SysExceptionLogResponse;
import com.zhyc.system.exceptionlog.service.SysExceptionLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统异常日志接口。
 */
@RestController
@RequestMapping("/system/exception-logs")
public class SysExceptionLogController {

    /** 系统异常日志业务服务。 */
    private final SysExceptionLogService exceptionLogService;

    /**
     * 创建系统异常日志接口。
     *
     * @param exceptionLogService 系统异常日志业务服务
     */
    public SysExceptionLogController(SysExceptionLogService exceptionLogService) {
        this.exceptionLogService = Objects.requireNonNull(exceptionLogService, "系统异常日志业务服务不能为空");
    }

    /**
     * 查询租户最近异常日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近异常日志列表
     */
    @RequiresPermissions("system:audit:query")
    @GetMapping("/recent")
    public ApiResult<List<SysExceptionLogResponse>> listRecent(@RequestParam("tenantId") String tenantId,
                                                               @RequestParam(value = "limit", defaultValue = "50")
                                                               int limit) {
        return ApiResult.ok(exceptionLogService.listRecent(tenantId, limit));
    }
}
