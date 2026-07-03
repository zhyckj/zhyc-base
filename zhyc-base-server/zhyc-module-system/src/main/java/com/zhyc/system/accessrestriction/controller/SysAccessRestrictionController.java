/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionEvaluationResult;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionResponse;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionSaveCommand;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 系统访问限制管理接口。
 */
@RestController
@RequestMapping("/system/access-restrictions")
public class SysAccessRestrictionController {

    /** 访问限制判定请求缺失错误码。 */
    private static final String ERROR_EVALUATE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_ACCESS_RESTRICTION_EVALUATE_REQUEST_REQUIRED";
    /** 访问限制保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_ACCESS_RESTRICTION_SAVE_REQUEST_REQUIRED";

    /** 系统访问限制业务服务。 */
    private final SysAccessRestrictionService accessRestrictionService;

    /**
     * 创建系统访问限制管理接口。
     *
     * @param accessRestrictionService 系统访问限制业务服务
     */
    public SysAccessRestrictionController(SysAccessRestrictionService accessRestrictionService) {
        this.accessRestrictionService = Objects.requireNonNull(accessRestrictionService,
                "系统访问限制业务服务不能为空");
    }

    /**
     * 查询当前生效的访问限制。
     *
     * @param tenantId 租户业务编码
     * @param restrictionType 限制类型
     * @return 当前生效的访问限制列表
     */
    @RequiresPermissions("system:access-restriction:query")
    @GetMapping
    public ApiResult<List<SysAccessRestrictionResponse>> listActiveRestrictions(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("restrictionType") String restrictionType) {
        return ApiResult.ok(accessRestrictionService.listActiveRestrictions(tenantId, restrictionType,
                LocalDateTime.now()));
    }

    /**
     * 判定指定访问标识是否允许访问。
     *
     * @param request 系统访问限制判定请求
     * @return 系统访问限制判定结果
     */
    @RequiresPermissions("system:access-restriction:evaluate")
    @PostMapping("/evaluate")
    public ApiResult<SysAccessRestrictionEvaluationResult> evaluateAccess(
            @RequestBody SysAccessRestrictionEvaluateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_EVALUATE_REQUEST_REQUIRED, "系统访问限制判定请求不能为空");
        }
        return ApiResult.ok(accessRestrictionService.evaluateAccess(request.getTenantId(),
                request.getRestrictionType(), request.getAccessValue(), LocalDateTime.now()));
    }

    /**
     * 保存或更新访问限制。
     *
     * @param request 系统访问限制保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:access-restriction:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody SysAccessRestrictionSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "系统访问限制保存请求不能为空");
        }
        accessRestrictionService.save(new SysAccessRestrictionSaveCommand(request.getTenantId(),
                request.getRestrictionType(), request.getRuleValue(), request.getEffect(), request.getStartAt(),
                request.getEndAt()));
        return ApiResult.ok(null);
    }
}
