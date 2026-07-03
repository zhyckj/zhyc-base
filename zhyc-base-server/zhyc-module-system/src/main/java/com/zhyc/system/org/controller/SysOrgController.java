/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.org.service.SysOrgSaveCommand;
import com.zhyc.system.org.service.SysOrgService;
import com.zhyc.system.org.service.SysOrgTreeNode;
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
 * 系统组织机构管理接口。
 */
@RestController
@RequestMapping("/system/orgs")
public class SysOrgController {

    /** 组织保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_ORG_SAVE_REQUEST_REQUIRED";
    /** 组织状态请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_SYSTEM_ORG_STATUS_REQUEST_REQUIRED";

    /** 系统组织机构业务服务。 */
    private final SysOrgService orgService;

    /**
     * 创建系统组织机构管理接口。
     *
     * @param orgService 系统组织机构业务服务
     */
    public SysOrgController(SysOrgService orgService) {
        this.orgService = Objects.requireNonNull(orgService, "系统组织机构业务服务不能为空");
    }

    /**
     * 查询租户组织机构树。
     *
     * @param tenantId 租户业务编码
     * @return 组织机构树节点列表
     */
    @RequiresPermissions("system:org:query")
    @GetMapping("/tree")
    public ApiResult<List<SysOrgTreeNode>> listTree(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(orgService.listOrgTree(tenantId));
    }

    /**
     * 新增系统组织机构。
     *
     * @param request 组织保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:org:create")
    @PostMapping
    public ApiResult<Void> createOrg(@RequestBody SysOrgSaveRequest request) {
        SysOrgSaveRequest requiredRequest = requireSaveRequest(request);
        orgService.saveOrg(toSaveCommand(null, requiredRequest));
        return ApiResult.ok(null);
    }

    /**
     * 编辑系统组织机构。
     *
     * @param orgId 组织主键
     * @param request 组织保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:org:update")
    @PutMapping("/{orgId}")
    public ApiResult<Void> updateOrg(@PathVariable("orgId") Long orgId, @RequestBody SysOrgSaveRequest request) {
        SysOrgSaveRequest requiredRequest = requireSaveRequest(request);
        orgService.saveOrg(toSaveCommand(orgId, requiredRequest));
        return ApiResult.ok(null);
    }

    /**
     * 调整组织机构状态。
     *
     * @param orgId 组织主键
     * @param request 状态请求
     * @return 空响应
     */
    @RequiresPermissions("system:org:update-status")
    @PutMapping("/{orgId}/status")
    public ApiResult<Void> updateStatus(@PathVariable("orgId") Long orgId, @RequestBody SysOrgStatusRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "组织状态请求不能为空");
        }
        orgService.updateStatus(request.getTenantId(), orgId, request.getStatus());
        return ApiResult.ok(null);
    }

    /**
     * 删除系统组织机构。
     *
     * @param orgId 组织主键
     * @param tenantId 租户业务编码
     * @return 空响应
     */
    @RequiresPermissions("system:org:delete")
    @DeleteMapping("/{orgId}")
    public ApiResult<Void> deleteOrg(@PathVariable("orgId") Long orgId, @RequestParam("tenantId") String tenantId) {
        orgService.deleteOrg(tenantId, orgId);
        return ApiResult.ok(null);
    }

    private SysOrgSaveRequest requireSaveRequest(SysOrgSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "组织保存请求不能为空");
        }
        return request;
    }

    private SysOrgSaveCommand toSaveCommand(Long orgId, SysOrgSaveRequest request) {
        return new SysOrgSaveCommand(orgId, request.getTenantId(), request.getParentId(), request.getOrgCode(),
                request.getOrgName(), request.getLeaderUserId(), request.getSortOrder(), request.getStatus());
    }
}
