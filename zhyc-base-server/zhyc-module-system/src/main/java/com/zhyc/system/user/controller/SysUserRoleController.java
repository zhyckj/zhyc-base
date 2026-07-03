/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.user.service.SysUserRoleBindCommand;
import com.zhyc.system.user.service.SysUserRoleResponse;
import com.zhyc.system.user.service.SysUserRoleService;
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
 * 系统用户角色绑定接口。
 */
@RestController
@RequestMapping("/system/users/{userId}/roles")
public class SysUserRoleController {

    /** 用户角色绑定请求缺失错误码。 */
    private static final String ERROR_BIND_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_USER_ROLE_BIND_REQUEST_REQUIRED";

    /** 系统用户角色业务服务。 */
    private final SysUserRoleService userRoleService;

    /**
     * 创建系统用户角色绑定接口。
     *
     * @param userRoleService 系统用户角色业务服务
     */
    public SysUserRoleController(SysUserRoleService userRoleService) {
        this.userRoleService = Objects.requireNonNull(userRoleService, "系统用户角色业务服务不能为空");
    }

    /**
     * 查询用户角色列表。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @return 用户角色列表
     */
    @RequiresPermissions("system:user:query")
    @GetMapping
    public ApiResult<List<SysUserRoleResponse>> listUserRoles(@PathVariable("userId") Long userId,
                                                              @RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(userRoleService.listUserRoles(tenantId, userId));
    }

    /**
     * 绑定用户角色列表。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @param request 用户角色绑定请求
     * @return 空响应
     */
    @RequiresPermissions("system:user:edit")
    @PutMapping
    public ApiResult<Void> bindUserRoles(@PathVariable("userId") Long userId,
                                         @RequestParam("tenantId") String tenantId,
                                         @RequestBody UserRoleBindRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_BIND_REQUEST_REQUIRED, "用户角色绑定请求不能为空");
        }
        List<Long> roleIds = request.getRoleIds() == null ? List.of() : request.getRoleIds();
        userRoleService.bindUserRoles(new SysUserRoleBindCommand(tenantId, userId, roleIds));
        return ApiResult.ok(null);
    }
}
