/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditResponse;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统权限变更审计接口。
 */
@RestController
@RequestMapping("/system/permission-audits")
public class SysPermissionAuditController {

    /** 系统权限变更审计业务服务。 */
    private final SysPermissionAuditService permissionAuditService;

    /**
     * 创建系统权限变更审计接口。
     *
     * @param permissionAuditService 系统权限变更审计业务服务
     */
    public SysPermissionAuditController(SysPermissionAuditService permissionAuditService) {
        this.permissionAuditService = Objects.requireNonNull(permissionAuditService, "系统权限变更审计业务服务不能为空");
    }

    /**
     * 查询租户最近权限变更审计。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近权限变更审计列表
     */
    @RequiresPermissions("system:audit:query")
    @GetMapping("/recent")
    public ApiResult<List<SysPermissionAuditResponse>> listRecent(@RequestParam("tenantId") String tenantId,
                                                                  @RequestParam(value = "limit", defaultValue = "50")
                                                                  int limit) {
        return ApiResult.ok(permissionAuditService.listRecent(tenantId, limit));
    }
}
