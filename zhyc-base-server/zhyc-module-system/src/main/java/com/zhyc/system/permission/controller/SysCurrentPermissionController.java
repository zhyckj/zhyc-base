/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.system.permission.service.SysPermissionService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 当前登录用户权限接口。
 *
 * <p>该接口只返回当前 Shiro Subject 绑定用户的权限编码，供前端按钮权限判断使用，避免前端依赖菜单维护树。</p>
 */
@RestController
@RequestMapping("/system/permissions")
public class SysCurrentPermissionController {

    /** 系统权限业务服务。 */
    private final SysPermissionService permissionService;

    /**
     * 创建当前登录用户权限接口。
     *
     * @param permissionService 系统权限业务服务
     */
    public SysCurrentPermissionController(SysPermissionService permissionService) {
        this.permissionService = Objects.requireNonNull(permissionService, "系统权限业务服务不能为空");
    }

    /**
     * 查询当前登录用户权限编码。
     *
     * @return 当前登录用户权限编码列表
     */
    @RequiresAuthentication
    @GetMapping("/current")
    public ApiResult<List<String>> listCurrentPermissions() {
        CurrentPrincipal principal = resolveCurrentPrincipal(SecurityUtils.getSubject().getPrincipal());
        return ApiResult.ok(permissionService.listUserPermissions(principal.tenantId(), principal.userId()));
    }

    /**
     * 解析 Shiro 主体中的租户和用户主键。
     *
     * @param principal Shiro 当前主体
     * @return 当前用户主体关键信息
     */
    private CurrentPrincipal resolveCurrentPrincipal(Object principal) {
        if (principal == null) {
            throw new AuthorizationException("当前用户未登录");
        }
        return new CurrentPrincipal(invokeStringGetter(principal, "getTenantId"),
                invokeLongGetter(principal, "getUserId"));
    }

    private String invokeStringGetter(Object target, String methodName) {
        Object value = invokeGetter(target, methodName);
        if (value instanceof String text && !text.trim().isEmpty()) {
            return text.trim();
        }
        throw new AuthorizationException("当前用户主体缺少租户信息");
    }

    private Long invokeLongGetter(Object target, String methodName) {
        Object value = invokeGetter(target, methodName);
        if (value instanceof Number number && number.longValue() > 0) {
            return number.longValue();
        }
        throw new AuthorizationException("当前用户主体缺少用户主键");
    }

    private Object invokeGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException exception) {
            throw new AuthorizationException("当前用户主体格式不正确", exception);
        }
    }

    /**
     * 当前用户主体关键信息。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    private record CurrentPrincipal(String tenantId, Long userId) {
    }
}
