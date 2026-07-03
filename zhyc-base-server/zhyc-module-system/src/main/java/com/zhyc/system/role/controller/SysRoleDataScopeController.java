/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.role.service.RoleDataScopeBindCommand;
import com.zhyc.system.role.service.SysRoleDataScopeResponse;
import com.zhyc.system.role.service.SysRoleDataScopeService;
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
 * 系统角色自定义数据权限接口。
 */
@RestController
@RequestMapping("/system/roles/{roleId}/data-scopes")
public class SysRoleDataScopeController {

    /** 角色数据权限绑定请求缺失错误码。 */
    private static final String ERROR_BIND_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_ROLE_DATA_SCOPE_BIND_REQUEST_REQUIRED";

    /** 系统角色自定义数据权限业务服务。 */
    private final SysRoleDataScopeService roleDataScopeService;

    /**
     * 创建系统角色自定义数据权限接口。
     *
     * @param roleDataScopeService 系统角色自定义数据权限业务服务
     */
    public SysRoleDataScopeController(SysRoleDataScopeService roleDataScopeService) {
        this.roleDataScopeService = Objects.requireNonNull(roleDataScopeService,
                "系统角色自定义数据权限业务服务不能为空");
    }

    /**
     * 查询角色自定义组织范围。
     *
     * @param roleId 角色主键
     * @param tenantId 租户业务编码
     * @return 角色自定义数据权限响应列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping
    public ApiResult<List<SysRoleDataScopeResponse>> listRoleDataScopes(@PathVariable("roleId") Long roleId,
                                                                        @RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(roleDataScopeService.listRoleDataScopes(tenantId, roleId));
    }

    /**
     * 绑定角色自定义组织范围。
     *
     * @param roleId 角色主键
     * @param tenantId 租户业务编码
     * @param request 角色自定义数据权限绑定请求
     * @return 空响应
     */
    @RequiresPermissions("system:role:edit")
    @PutMapping
    public ApiResult<Void> bindRoleDataScopes(@PathVariable("roleId") Long roleId,
                                              @RequestParam("tenantId") String tenantId,
                                              @RequestBody RoleDataScopeBindRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_BIND_REQUEST_REQUIRED, "角色数据权限绑定请求不能为空");
        }
        List<Long> orgIds = request.getOrgIds() == null ? List.of() : request.getOrgIds();
        roleDataScopeService.bindRoleDataScopes(new RoleDataScopeBindCommand(tenantId, roleId, orgIds));
        return ApiResult.ok(null);
    }
}
