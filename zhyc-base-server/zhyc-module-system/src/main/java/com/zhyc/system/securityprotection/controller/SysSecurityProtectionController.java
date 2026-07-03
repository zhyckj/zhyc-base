/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import com.zhyc.system.securityprotection.service.SecurityEventRecordCommand;
import com.zhyc.system.securityprotection.service.SecurityEventResponse;
import com.zhyc.system.securityprotection.service.SecurityIpBlockCommand;
import com.zhyc.system.securityprotection.service.SecurityOverviewResponse;
import com.zhyc.system.securityprotection.service.SecurityRankResponse;
import com.zhyc.system.securityprotection.service.SysSecurityProtectionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统安全防护中心管理接口。
 */
@RestController
@RequestMapping("/system/security-protection")
public class SysSecurityProtectionController {

    /** 请求体不能为空错误码。 */
    private static final String ERROR_REQUEST_REQUIRED = "ZHYC_SYSTEM_SECURITY_PROTECTION_REQUEST_REQUIRED";

    /** 系统安全防护中心业务服务。 */
    private final SysSecurityProtectionService securityProtectionService;

    /**
     * 创建系统安全防护中心管理接口。
     *
     * @param securityProtectionService 系统安全防护中心业务服务
     */
    public SysSecurityProtectionController(SysSecurityProtectionService securityProtectionService) {
        this.securityProtectionService = Objects.requireNonNull(securityProtectionService,
                "系统安全防护中心业务服务不能为空");
    }

    /**
     * 查询安全防护总览。
     *
     * @param tenantId 租户业务编码
     * @return 安全防护总览
     */
    @RequiresPermissions("system:security-protection:query")
    @GetMapping("/overview")
    public ApiResult<SecurityOverviewResponse> overview(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(securityProtectionService.overview(tenantId, LocalDateTime.now()));
    }

    /**
     * 查询安全防护策略。
     *
     * @param tenantId 租户业务编码
     * @return 安全防护策略列表
     */
    @RequiresPermissions("system:security-protection:query")
    @GetMapping("/policies")
    public ApiResult<List<SysSecurityPolicy>> policies(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(securityProtectionService.listPolicies(tenantId));
    }

    /**
     * 保存安全防护策略。
     *
     * @param request 安全防护策略保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:security-protection:save")
    @PutMapping("/policies")
    public ApiResult<Void> savePolicy(@RequestBody SecurityPolicySaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_REQUEST_REQUIRED, "安全防护策略保存请求不能为空");
        }
        securityProtectionService.savePolicy(toPolicy(request));
        return ApiResult.ok(null);
    }

    /**
     * 查询最近安全事件。
     *
     * @param tenantId 租户业务编码
     * @param limit 返回数量
     * @return 最近安全事件列表
     */
    @RequiresPermissions("system:security-protection:query")
    @GetMapping("/events")
    public ApiResult<List<SecurityEventResponse>> events(@RequestParam("tenantId") String tenantId,
                                                         @RequestParam(value = "limit", defaultValue = "20")
                                                         int limit) {
        return ApiResult.ok(securityProtectionService.recentEvents(tenantId, limit));
    }

    /**
     * 记录安全事件。
     *
     * @param request 安全事件记录请求
     * @return 空响应
     */
    @RequiresPermissions("system:security-protection:record")
    @PostMapping("/events")
    public ApiResult<Void> recordEvent(@RequestBody SecurityEventRecordRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_REQUEST_REQUIRED, "安全事件记录请求不能为空");
        }
        securityProtectionService.recordEvent(new SecurityEventRecordCommand(request.getTenantId(),
                request.getEventType(), request.getEventLevel(), request.getSourceIp(), request.getUserId(),
                request.getUsername(), request.getRequestPath(), request.getHttpMethod(), request.getAction(),
                request.getResult(), request.getMessage(), request.getOccurredAt()));
        return ApiResult.ok(null);
    }

    /**
     * 查询来源 IP 请求排行。
     *
     * @param tenantId 租户业务编码
     * @param limit 返回数量
     * @return 来源 IP 请求排行
     */
    @RequiresPermissions("system:security-protection:query")
    @GetMapping("/ip-ranking")
    public ApiResult<List<SecurityRankResponse>> topSourceIps(@RequestParam("tenantId") String tenantId,
                                                              @RequestParam(value = "limit", defaultValue = "10")
                                                              int limit) {
        return ApiResult.ok(securityProtectionService.topSourceIps(tenantId, LocalDateTime.now(), limit));
    }

    /**
     * 查询接口访问排行。
     *
     * @param tenantId 租户业务编码
     * @param limit 返回数量
     * @return 接口访问排行
     */
    @RequiresPermissions("system:security-protection:query")
    @GetMapping("/api-ranking")
    public ApiResult<List<SecurityRankResponse>> topRequestPaths(@RequestParam("tenantId") String tenantId,
                                                                 @RequestParam(value = "limit", defaultValue = "10")
                                                                 int limit) {
        return ApiResult.ok(securityProtectionService.topRequestPaths(tenantId, LocalDateTime.now(), limit));
    }

    /**
     * 封禁 IP。
     *
     * @param request IP 封禁请求
     * @return 空响应
     */
    @RequiresPermissions("system:security-protection:block")
    @PostMapping("/ip-blocks")
    public ApiResult<Void> blockIp(@RequestBody SecurityIpBlockRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_REQUEST_REQUIRED, "IP 封禁请求不能为空");
        }
        securityProtectionService.blockIp(new SecurityIpBlockCommand(request.getTenantId(), request.getIpValue(),
                request.getBlockType(), request.getReason(), request.getStartAt(), request.getEndAt()));
        return ApiResult.ok(null);
    }

    /**
     * 解封 IP。
     *
     * @param request IP 解封请求
     * @return 空响应
     */
    @RequiresPermissions("system:security-protection:unblock")
    @PostMapping("/ip-blocks/unblock")
    public ApiResult<Void> unblockIp(@RequestBody SecurityIpUnblockRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_REQUEST_REQUIRED, "IP 解封请求不能为空");
        }
        securityProtectionService.unblockIp(request.getTenantId(), request.getIpValue());
        return ApiResult.ok(null);
    }

    private SysSecurityPolicy toPolicy(SecurityPolicySaveRequest request) {
        SysSecurityPolicy policy = new SysSecurityPolicy();
        policy.setTenantId(request.getTenantId());
        policy.setPolicyCode(request.getPolicyCode());
        policy.setPolicyName(request.getPolicyName());
        policy.setProtectionScope(request.getProtectionScope());
        policy.setTargetPattern(request.getTargetPattern());
        policy.setThresholdLimit(request.getThresholdLimit());
        policy.setWindowSeconds(request.getWindowSeconds());
        policy.setAction(request.getAction());
        policy.setBlockSeconds(request.getBlockSeconds());
        policy.setStatus(request.getStatus());
        return policy;
    }
}
