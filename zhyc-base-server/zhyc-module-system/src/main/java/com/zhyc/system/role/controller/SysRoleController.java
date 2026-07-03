/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.role.service.RoleMenuBindCommand;
import com.zhyc.system.role.service.SysRoleResponse;
import com.zhyc.system.role.service.SysRoleSaveCommand;
import com.zhyc.system.role.service.SysRoleService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统角色管理接口。
 */
@RestController
@RequestMapping("/system/roles")
public class SysRoleController {

    /** 角色菜单绑定请求缺失错误码。 */
    private static final String ERROR_MENU_BIND_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_ROLE_MENU_BIND_REQUEST_REQUIRED";
    /** 角色保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_ROLE_SAVE_REQUEST_REQUIRED";
    /** 角色状态请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_SYSTEM_ROLE_STATUS_REQUEST_REQUIRED";

    /** 系统角色业务服务。 */
    private final SysRoleService roleService;

    /**
     * 创建系统角色管理接口。
     *
     * @param roleService 系统角色业务服务
     */
    public SysRoleController(SysRoleService roleService) {
        this.roleService = Objects.requireNonNull(roleService, "系统角色业务服务不能为空");
    }

    /**
     * 查询租户角色列表。
     *
     * @param tenantId 租户业务编码
     * @return 角色列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping
    public ApiResult<List<SysRoleResponse>> listRoles(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(roleService.listRoles(tenantId));
    }

    /**
     * 新增系统角色。
     *
     * @param request 角色保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:role:create")
    @PostMapping
    public ApiResult<Void> createRole(@RequestBody SysRoleSaveRequest request) {
        SysRoleSaveRequest requiredRequest = requireSaveRequest(request);
        roleService.saveRole(toSaveCommand(null, requiredRequest));
        return ApiResult.ok(null);
    }

    /**
     * 编辑系统角色。
     *
     * @param roleId 角色主键
     * @param request 角色保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:role:update")
    @PutMapping("/{roleId}")
    public ApiResult<Void> updateRole(@PathVariable("roleId") Long roleId, @RequestBody SysRoleSaveRequest request) {
        SysRoleSaveRequest requiredRequest = requireSaveRequest(request);
        roleService.saveRole(toSaveCommand(roleId, requiredRequest));
        return ApiResult.ok(null);
    }

    /**
     * 调整系统角色状态。
     *
     * @param roleId 角色主键
     * @param request 状态请求
     * @return 空响应
     */
    @RequiresPermissions("system:role:update-status")
    @PutMapping("/{roleId}/status")
    public ApiResult<Void> updateRoleStatus(@PathVariable("roleId") Long roleId,
                                            @RequestBody SysRoleStatusRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "角色状态请求不能为空");
        }
        roleService.updateStatus(request.getTenantId(), roleId, request.getStatus());
        return ApiResult.ok(null);
    }

    /**
     * 删除系统角色。
     *
     * @param roleId 角色主键
     * @param tenantId 租户业务编码
     * @return 空响应
     */
    @RequiresPermissions("system:role:delete")
    @DeleteMapping("/{roleId}")
    public ApiResult<Void> deleteRole(@PathVariable("roleId") Long roleId,
                                      @RequestParam("tenantId") String tenantId) {
        roleService.deleteRole(tenantId, roleId);
        return ApiResult.ok(null);
    }

    /**
     * 查询角色已绑定的菜单权限。
     *
     * @param roleId 角色主键
     * @param tenantId 租户业务编码
     * @return 已绑定菜单主键列表
     */
    @RequiresPermissions("system:role:authorize")
    @GetMapping("/{roleId}/menus")
    public ApiResult<List<Long>> listRoleMenuIds(@PathVariable("roleId") Long roleId,
                                                 @RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(roleService.listRoleMenuIds(tenantId, roleId));
    }

    /**
     * 绑定角色菜单权限。
     *
     * @param roleId 角色主键
     * @param request 角色菜单绑定请求
     * @return 空响应
     */
    @RequiresPermissions("system:role:authorize")
    @PutMapping("/{roleId}/menus")
    public ApiResult<Void> bindMenus(@PathVariable("roleId") Long roleId, @RequestBody RoleMenuBindRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_MENU_BIND_REQUEST_REQUIRED, "角色菜单绑定请求不能为空");
        }
        roleService.bindRoleMenus(new RoleMenuBindCommand(request.getTenantId(), roleId, request.getMenuIds()));
        return ApiResult.ok(null);
    }

    /**
     * 校验并返回角色保存请求。
     *
     * @param request 角色保存请求
     * @return 非空角色保存请求
     */
    private SysRoleSaveRequest requireSaveRequest(SysRoleSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "角色保存请求不能为空");
        }
        return request;
    }

    /**
     * 转换角色保存请求为业务命令。
     *
     * @param roleId 角色主键，新增时为空
     * @param request 角色保存请求
     * @return 角色保存命令
     */
    private SysRoleSaveCommand toSaveCommand(Long roleId, SysRoleSaveRequest request) {
        return new SysRoleSaveCommand(roleId, request.getTenantId(), request.getRoleCode(), request.getName(),
                request.getDataScope(), request.getStatus());
    }
}
