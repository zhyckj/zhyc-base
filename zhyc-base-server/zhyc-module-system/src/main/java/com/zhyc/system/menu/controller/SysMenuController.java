/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.menu.service.SysMenuSaveCommand;
import com.zhyc.system.menu.service.SysMenuService;
import com.zhyc.system.menu.service.SysMenuTreeNode;
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
 * 系统菜单管理接口。
 */
@RestController
@RequestMapping("/system/menus")
public class SysMenuController {

    /** 菜单保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_MENU_SAVE_REQUEST_REQUIRED";
    /** 菜单状态请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_SYSTEM_MENU_STATUS_REQUEST_REQUIRED";

    /** 系统菜单业务服务。 */
    private final SysMenuService menuService;

    /**
     * 创建系统菜单管理接口。
     *
     * @param menuService 系统菜单业务服务
     */
    public SysMenuController(SysMenuService menuService) {
        this.menuService = Objects.requireNonNull(menuService, "系统菜单业务服务不能为空");
    }

    /**
     * 查询租户菜单树。
     *
     * @param tenantId 租户业务编码
     * @param includeDisabled 是否包含停用菜单
     * @return 菜单树节点列表
     */
    @RequiresPermissions("system:permission:query")
    @GetMapping("/tree")
    public ApiResult<List<SysMenuTreeNode>> listTree(@RequestParam("tenantId") String tenantId,
                                                     @RequestParam(value = "includeDisabled",
                                                             defaultValue = "false") boolean includeDisabled) {
        return ApiResult.ok(menuService.listMenuTree(tenantId, includeDisabled));
    }

    @RequiresPermissions("system:permission:create")
    @PostMapping
    public ApiResult<Void> createMenu(@RequestBody SysMenuSaveRequest request) {
        SysMenuSaveRequest requiredRequest = requireSaveRequest(request);
        menuService.saveMenu(toSaveCommand(null, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:permission:update")
    @PutMapping("/{menuId}")
    public ApiResult<Void> updateMenu(@PathVariable("menuId") Long menuId, @RequestBody SysMenuSaveRequest request) {
        SysMenuSaveRequest requiredRequest = requireSaveRequest(request);
        menuService.saveMenu(toSaveCommand(menuId, requiredRequest));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:permission:update-status")
    @PutMapping("/{menuId}/status")
    public ApiResult<Void> updateStatus(@PathVariable("menuId") Long menuId,
                                        @RequestBody SysMenuStatusRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "菜单状态请求不能为空");
        }
        menuService.updateStatus(request.getTenantId(), menuId, request.getStatus());
        return ApiResult.ok(null);
    }

    @RequiresPermissions("system:permission:delete")
    @DeleteMapping("/{menuId}")
    public ApiResult<Void> deleteMenu(@PathVariable("menuId") Long menuId,
                                      @RequestParam("tenantId") String tenantId) {
        menuService.deleteMenu(tenantId, menuId);
        return ApiResult.ok(null);
    }

    private SysMenuSaveRequest requireSaveRequest(SysMenuSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "菜单保存请求不能为空");
        }
        return request;
    }

    private SysMenuSaveCommand toSaveCommand(Long menuId, SysMenuSaveRequest request) {
        return new SysMenuSaveCommand(menuId, request.getTenantId(), request.getParentId(), request.getMenuCode(),
                request.getName(), request.getType(), request.getPath(), request.getComponent(),
                request.getPermission(), request.getSortOrder(), request.getStatus());
    }
}
