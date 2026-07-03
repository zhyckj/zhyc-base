/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.system.user.service.SysUserPasswordChangeCommand;
import com.zhyc.system.user.service.SysUserResponse;
import com.zhyc.system.user.service.SysUserSaveCommand;
import com.zhyc.system.user.service.SysUserService;
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
 * 系统用户管理接口。
 */
@RestController
@RequestMapping("/system/users")
public class SysUserController {

    /** 系统用户业务服务。 */
    private final SysUserService userService;

    /**
     * 创建系统用户管理接口。
     *
     * @param userService 系统用户业务服务
     */
    public SysUserController(SysUserService userService) {
        this.userService = Objects.requireNonNull(userService, "系统用户业务服务不能为空");
    }

    /**
     * 查询租户内系统用户列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统用户列表
     */
    @RequiresPermissions("system:user:query")
    @GetMapping
    public ApiResult<List<SysUserResponse>> listUsers(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(userService.listUsers(tenantId));
    }

    /**
     * 新增系统用户。
     *
     * @param request 系统用户保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:user:create")
    @PostMapping
    public ApiResult<Void> createUser(@RequestBody SysUserSaveRequest request) {
        SysUserSaveRequest requiredRequest = com.zhyc.system.support.SystemServiceValidation.requireObject(request,
                "用户保存请求不能为空");
        userService.saveUser(new SysUserSaveCommand(null, requiredRequest.getTenantId(), requiredRequest.getUsername(),
                requiredRequest.getNickname(), requiredRequest.getPassword(), requiredRequest.getStatus()));
        return ApiResult.ok(null);
    }

    /**
     * 编辑系统用户。
     *
     * @param userId 用户主键
     * @param request 系统用户保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:user:update")
    @PutMapping("/{userId}")
    public ApiResult<Void> updateUser(@PathVariable("userId") Long userId,
                                      @RequestBody SysUserSaveRequest request) {
        SysUserSaveRequest requiredRequest = com.zhyc.system.support.SystemServiceValidation.requireObject(request,
                "用户保存请求不能为空");
        userService.saveUser(new SysUserSaveCommand(userId, requiredRequest.getTenantId(), requiredRequest.getUsername(),
                requiredRequest.getNickname(), requiredRequest.getPassword(), requiredRequest.getStatus()));
        return ApiResult.ok(null);
    }

    /**
     * 修改系统用户状态。
     *
     * @param userId 用户主键
     * @param request 状态变更请求
     * @return 空响应
     */
    @RequiresPermissions("system:user:update-status")
    @PutMapping("/{userId}/status")
    public ApiResult<Void> updateStatus(@PathVariable("userId") Long userId,
                                        @RequestBody SysUserStatusRequest request) {
        SysUserStatusRequest requiredRequest = com.zhyc.system.support.SystemServiceValidation.requireObject(request,
                "用户状态请求不能为空");
        userService.updateStatus(requiredRequest.getTenantId(), userId, requiredRequest.getStatus());
        return ApiResult.ok(null);
    }

    /**
     * 重置系统用户密码。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @param request 重置密码请求
     * @return 空响应
     */
    @RequiresPermissions("system:user:reset-password")
    @PutMapping("/{userId}/password")
    public ApiResult<Void> resetPassword(@PathVariable("userId") Long userId,
                                         @RequestParam("tenantId") String tenantId,
                                         @RequestBody SysUserResetPasswordRequest request) {
        SysUserResetPasswordRequest requiredRequest = com.zhyc.system.support.SystemServiceValidation.requireObject(
                request, "重置密码请求不能为空");
        userService.resetPassword(tenantId, userId, requiredRequest.getPassword());
        return ApiResult.ok(null);
    }

    /**
     * 删除系统用户。
     *
     * @param userId 用户主键
     * @param tenantId 租户业务编码
     * @return 空响应
     */
    @RequiresPermissions("system:user:delete")
    @DeleteMapping("/{userId}")
    public ApiResult<Void> deleteUser(@PathVariable("userId") Long userId,
                                      @RequestParam("tenantId") String tenantId) {
        userService.deleteUser(tenantId, userId);
        return ApiResult.ok(null);
    }

    /**
     * 修改系统用户密码。
     *
     * @param command 修改密码命令
     * @return 修改结果
     */
    @RequiresPermissions("system:user:change-password")
    @PostMapping("/password")
    public ApiResult<Boolean> changePassword(@RequestBody SysUserPasswordChangeCommand command) {
        userService.changePassword(com.zhyc.system.support.SystemServiceValidation.requireObject(command,
                "修改密码请求不能为空"));
        return ApiResult.ok(Boolean.TRUE);
    }
}
