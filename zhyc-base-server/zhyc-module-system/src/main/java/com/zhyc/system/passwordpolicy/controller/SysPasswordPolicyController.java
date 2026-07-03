/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.passwordpolicy.service.PasswordPolicyValidationResult;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyResponse;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicySaveCommand;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 系统密码策略管理接口。
 */
@RestController
@RequestMapping("/system/password-policies")
public class SysPasswordPolicyController {

    /** 系统密码策略保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_PASSWORD_POLICY_SAVE_REQUEST_REQUIRED";
    /** 密码策略校验请求缺失错误码。 */
    private static final String ERROR_VALIDATE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_PASSWORD_POLICY_VALIDATE_REQUEST_REQUIRED";
    /** 密码历史策略校验请求缺失错误码。 */
    private static final String ERROR_HISTORY_VALIDATE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_PASSWORD_POLICY_HISTORY_VALIDATE_REQUEST_REQUIRED";

    /** 系统密码策略业务服务。 */
    private final SysPasswordPolicyService passwordPolicyService;

    /**
     * 创建系统密码策略管理接口。
     *
     * @param passwordPolicyService 系统密码策略业务服务
     */
    public SysPasswordPolicyController(SysPasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = Objects.requireNonNull(passwordPolicyService,
                "系统密码策略业务服务不能为空");
    }

    /**
     * 查询租户默认密码策略。
     *
     * @param tenantId 租户业务编码
     * @return 默认密码策略
     */
    @RequiresPermissions("system:password-policy:query")
    @GetMapping
    public ApiResult<SysPasswordPolicyResponse> getPolicy(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(passwordPolicyService.getPolicy(tenantId));
    }

    /**
     * 保存或更新租户密码策略。
     *
     * @param request 系统密码策略保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:password-policy:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody SysPasswordPolicySaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "系统密码策略保存请求不能为空");
        }
        passwordPolicyService.save(new SysPasswordPolicySaveCommand(request.getTenantId(), request.getPolicyCode(),
                request.getPolicyName(), request.getMinLength(), request.isRequireUppercase(),
                request.isRequireLowercase(), request.isRequireDigit(), request.isRequireSpecial(),
                request.getExpireDays(), request.getHistoryCount(), request.getMaxRetryCount(),
                request.getLockMinutes(), request.isEnabled()));
        return ApiResult.ok(null);
    }

    /**
     * 校验密码是否满足租户默认密码策略。
     *
     * @param request 密码策略校验请求
     * @return 密码策略校验结果
     */
    @RequiresPermissions("system:password-policy:validate")
    @PostMapping("/validate")
    public ApiResult<PasswordPolicyValidationResult> validatePassword(
            @RequestBody PasswordPolicyValidateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_VALIDATE_REQUEST_REQUIRED, "密码策略校验请求不能为空");
        }
        return ApiResult.ok(passwordPolicyService.validatePassword(request.getTenantId(), request.getPassword()));
    }

    /**
     * 校验新密码哈希是否违反历史密码复用策略。
     *
     * @param request 密码历史策略校验请求
     * @return 密码历史策略校验结果
     */
    @RequiresPermissions("system:password-policy:validate")
    @PostMapping("/validate-history")
    public ApiResult<PasswordPolicyValidationResult> validatePasswordHistory(
            @RequestBody PasswordPolicyHistoryValidateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_HISTORY_VALIDATE_REQUEST_REQUIRED, "密码历史策略校验请求不能为空");
        }
        return ApiResult.ok(passwordPolicyService.validatePasswordHistory(request.getTenantId(),
                request.getPasswordHash(), request.getRecentPasswordHashes()));
    }
}
