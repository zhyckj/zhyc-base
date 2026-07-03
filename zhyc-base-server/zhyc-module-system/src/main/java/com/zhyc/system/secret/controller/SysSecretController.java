/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.secret.service.SysSecretResponse;
import com.zhyc.system.secret.service.SysSecretSaveCommand;
import com.zhyc.system.secret.service.SysSecretService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 系统密钥中心管理接口。
 *
 * <p>面向后台管理端提供密钥查询、保存、启停、轮换和删除能力，所有响应均禁止回传明文密钥。</p>
 */
@RestController
@RequestMapping("/system/secrets")
public class SysSecretController {

    /** 密钥保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_SECRET_SAVE_REQUEST_REQUIRED";
    /** 密钥轮换请求缺失错误码。 */
    private static final String ERROR_ROTATE_REQUEST_REQUIRED = "ZHYC_SYSTEM_SECRET_ROTATE_REQUEST_REQUIRED";
    /** 密钥状态请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_SYSTEM_SECRET_STATUS_REQUEST_REQUIRED";

    /** 系统密钥业务服务。 */
    private final SysSecretService secretService;

    /**
     * 创建系统密钥中心管理接口。
     *
     * @param secretService 系统密钥业务服务
     */
    public SysSecretController(SysSecretService secretService) {
        this.secretService = Objects.requireNonNull(secretService, "系统密钥业务服务不能为空");
    }

    /**
     * 查询当前租户的密钥列表。
     *
     * @param tenantId 租户业务编码
     * @return 密钥列表，不包含明文密钥
     */
    @RequiresPermissions("system:secret:query")
    @GetMapping
    public ApiResult<List<SysSecretResponse>> listSecrets(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(secretService.listSecrets(tenantId));
    }

    /**
     * 查询当前租户的密钥详情。
     *
     * @param tenantId 租户业务编码
     * @param secretId 密钥主键
     * @return 密钥详情，不包含明文密钥
     */
    @RequiresPermissions("system:secret:query")
    @GetMapping("/{secretId}")
    public ApiResult<SysSecretResponse> getSecret(@RequestParam("tenantId") String tenantId,
                                                  @PathVariable("secretId") Long secretId) {
        return ApiResult.ok(secretService.getSecret(tenantId, secretId));
    }

    /**
     * 查询密钥下拉选项。
     *
     * <p>支持按密钥类型和状态过滤；不指定类型时返回低代码数据源兼容密钥，指定 API 密钥时供 AI 供应商选择。</p>
     *
     * @param tenantId 租户业务编码
     * @param secretKind 密钥类型，可为空
     * @param status 密钥状态，可为空
     * @return 密钥选项列表
     */
    @RequiresPermissions("system:secret:query")
    @GetMapping("/options")
    public ApiResult<List<SysSecretResponse>> listOptions(@RequestParam("tenantId") String tenantId,
                                                          @RequestParam(value = "secretKind", required = false)
                                                          String secretKind,
                                                          @RequestParam(value = "status", required = false)
                                                          String status) {
        return ApiResult.ok(secretService.listOptions(tenantId, secretKind, status));
    }

    /**
     * 创建系统密钥。
     *
     * @param request 密钥保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:secret:create")
    @PostMapping
    public ApiResult<Void> createSecret(@RequestBody SysSecretSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "密钥保存请求不能为空");
        }
        secretService.saveSecret(toCommand(null, request, null));
        return ApiResult.ok(null);
    }

    /**
     * 更新系统密钥基础信息。
     *
     * @param secretId 密钥主键
     * @param request 密钥保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:secret:update")
    @PutMapping("/{secretId}")
    public ApiResult<Void> updateSecret(@PathVariable("secretId") Long secretId,
                                        @RequestBody SysSecretSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "密钥保存请求不能为空");
        }
        secretService.saveSecret(toCommand(secretId, request, request.getTenantId()));
        return ApiResult.ok(null);
    }

    /**
     * 启用系统密钥。
     *
     * @param secretId 密钥主键
     * @param request 密钥状态请求
     * @return 空响应
     */
    @RequiresPermissions("system:secret:enable")
    @PutMapping("/{secretId}/enable")
    public ApiResult<Void> enableSecret(@PathVariable("secretId") Long secretId,
                                        @RequestBody SysSecretStatusRequest request) {
        return changeStatus(secretId, request, "enabled", ERROR_STATUS_REQUEST_REQUIRED, "密钥启用请求不能为空");
    }

    /**
     * 停用系统密钥。
     *
     * @param secretId 密钥主键
     * @param request 密钥状态请求
     * @return 空响应
     */
    @RequiresPermissions("system:secret:disable")
    @PutMapping("/{secretId}/disable")
    public ApiResult<Void> disableSecret(@PathVariable("secretId") Long secretId,
                                         @RequestBody SysSecretStatusRequest request) {
        return changeStatus(secretId, request, "disabled", ERROR_STATUS_REQUEST_REQUIRED, "密钥停用请求不能为空");
    }

    /**
     * 轮换系统密钥。
     *
     * @param secretId 密钥主键
     * @param request 密钥轮换请求
     * @return 空响应
     */
    @RequiresPermissions("system:secret:rotate")
    @PostMapping("/{secretId}/rotate")
    public ApiResult<Void> rotateSecret(@PathVariable("secretId") Long secretId,
                                        @RequestBody SysSecretRotateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_ROTATE_REQUEST_REQUIRED, "密钥轮换请求不能为空");
        }
        secretService.rotateSecret(request.getTenantId(), secretId, request.getSecretPlaintext(), request.getExpireAt());
        return ApiResult.ok(null);
    }

    /**
     * 删除系统密钥。
     *
     * @param secretId 密钥主键
     * @param tenantId 租户业务编码
     * @return 空响应
     */
    @RequiresPermissions("system:secret:delete")
    @DeleteMapping("/{secretId}")
    public ApiResult<Void> deleteSecret(@PathVariable("secretId") Long secretId,
                                        @RequestParam("tenantId") String tenantId) {
        secretService.deleteSecret(tenantId, secretId);
        return ApiResult.ok(null);
    }

    /**
     * 转换密钥保存命令。
     *
     * @param secretId 密钥主键
     * @param request 密钥保存请求
     * @param tenantIdPath 租户业务编码，更新时优先使用请求体租户
     * @return 密钥保存命令
     */
    private SysSecretSaveCommand toCommand(Long secretId, SysSecretSaveRequest request, String tenantIdPath) {
        String tenantId = tenantIdPath == null || tenantIdPath.trim().isEmpty()
                ? request.getTenantId()
                : tenantIdPath;
        return new SysSecretSaveCommand(secretId, tenantId, request.getSecretCode(), request.getSecretName(),
                request.getSecretKind(), request.getSecretPlaintext(), request.getStatus(), request.getExpireAt());
    }

    /**
     * 修改密钥状态。
     *
     * @param secretId 密钥主键
     * @param request 密钥状态请求
     * @param expectedStatus 期望状态
     * @param errorCode 空请求错误码
     * @param errorMessage 空请求错误提示
     * @return 空响应
     */
    private ApiResult<Void> changeStatus(Long secretId, SysSecretStatusRequest request, String expectedStatus,
                                         String errorCode, String errorMessage) {
        if (request == null) {
            throw new BusinessException(errorCode, errorMessage);
        }
        String status = request.getStatus();
        if (status == null || status.trim().isEmpty() || !expectedStatus.equals(status.trim())) {
            status = expectedStatus;
        }
        secretService.updateStatus(request.getTenantId(), secretId, status);
        return ApiResult.ok(null);
    }
}
