/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.adminscope.service.AdminScopeBindCommand;
import com.zhyc.system.adminscope.service.AdminScopeBindItem;
import com.zhyc.system.adminscope.service.SysAdminScopeResponse;
import com.zhyc.system.adminscope.service.SysAdminScopeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统管理员管理范围接口。
 */
@RestController
@RequestMapping("/system/admins/{userId}/scopes")
public class SysAdminScopeController {

    /** 管理员管理范围绑定请求缺失错误码。 */
    private static final String ERROR_BIND_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_ADMIN_SCOPE_BIND_REQUEST_REQUIRED";

    /** 系统管理员管理范围业务服务。 */
    private final SysAdminScopeService adminScopeService;

    /**
     * 创建系统管理员管理范围接口。
     *
     * @param adminScopeService 系统管理员管理范围业务服务
     */
    public SysAdminScopeController(SysAdminScopeService adminScopeService) {
        this.adminScopeService = Objects.requireNonNull(adminScopeService,
                "系统管理员管理范围业务服务不能为空");
    }

    /**
     * 查询管理员管理范围。
     *
     * @param userId 管理员用户主键
     * @param tenantId 租户业务编码
     * @return 管理员管理范围响应列表
     */
    @RequiresPermissions("system:admin:query")
    @GetMapping
    public ApiResult<List<SysAdminScopeResponse>> listAdminScopes(@PathVariable("userId") Long userId,
                                                                  @RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(adminScopeService.listAdminScopes(tenantId, userId));
    }

    /**
     * 绑定管理员管理范围。
     *
     * @param userId 管理员用户主键
     * @param tenantId 租户业务编码
     * @param request 管理员管理范围绑定请求
     * @return 空响应
     */
    @RequiresPermissions("system:admin:edit")
    @PutMapping
    public ApiResult<Void> bindAdminScopes(@PathVariable("userId") Long userId,
                                           @RequestParam("tenantId") String tenantId,
                                           @RequestBody AdminScopeBindRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_BIND_REQUEST_REQUIRED, "管理员管理范围绑定请求不能为空");
        }
        List<AdminScopeBindItem> scopes = request.getScopes() == null
                ? List.of()
                : request.getScopes().stream()
                        .map(item -> new AdminScopeBindItem(item.getScopeType(), item.getScopeRefCode()))
                        .toList();
        adminScopeService.bindAdminScopes(new AdminScopeBindCommand(tenantId, userId, scopes));
        return ApiResult.ok(null);
    }
}
