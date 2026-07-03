/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.debug.service.OpenApiDebugCommand;
import com.zhyc.openapi.debug.service.OpenApiDebugResponse;
import com.zhyc.openapi.debug.service.OpenApiDebugService;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放 API 调试代理接口。
 *
 * <p>该控制器只允许后台管理端通过统一权限调用，再由服务层受控转发到开放 API 网关。</p>
 */
@RestController
@RequestMapping("/openapi/debug")
public class OpenApiDebugController {

    /** 租户业务编码请求头。 */
    public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

    /** 调试请求缺失错误码。 */
    private static final String ERROR_DEBUG_REQUEST_REQUIRED = "ZHYC_OPENAPI_DEBUG_REQUEST_REQUIRED";

    /** 调试租户缺失错误码。 */
    private static final String ERROR_DEBUG_TENANT_REQUIRED = "ZHYC_OPENAPI_DEBUG_TENANT_REQUIRED";

    /** 调试租户不一致错误码。 */
    private static final String ERROR_DEBUG_TENANT_MISMATCH = "ZHYC_OPENAPI_DEBUG_TENANT_MISMATCH";

    /** 开放 API 调试业务服务。 */
    private final OpenApiDebugService debugService;

    /**
     * 创建开放 API 调试代理接口。
     *
     * @param debugService 开放 API 调试业务服务
     */
    public OpenApiDebugController(OpenApiDebugService debugService) {
        this.debugService = Objects.requireNonNull(debugService, "开放 API 调试业务服务不能为空");
    }

    /**
     * 通过后台代理发送开放 API 调试请求。
     *
     * @param tenantId 当前请求头租户业务编码
     * @param request 开放 API 调试代理请求
     * @return 开放 API 调试代理响应
     */
    @RequiresPermissions("openapi:debug:invoke")
    @PostMapping("/invoke")
    public ApiResult<OpenApiDebugResponse> invoke(@RequestHeader(HEADER_TENANT_ID) String tenantId,
                                                  @RequestBody OpenApiDebugInvokeRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_DEBUG_REQUEST_REQUIRED, "开放 API 调试请求不能为空");
        }
        String currentTenantId = requireCurrentTenant(tenantId, request.getTenantId());
        return ApiResult.ok(debugService.invoke(new OpenApiDebugCommand(currentTenantId, request.getApiCode(),
                request.getMethod(), request.getPath(), request.getAuthMode(), request.getAccessKey(),
                request.getTimestamp(), request.getNonce(), request.getSignature(), request.getBearerToken(),
                request.getRequestId(), request.getBody())));
    }

    /**
     * 校验调试请求租户与当前请求租户一致，并返回规范化后的当前租户。
     *
     * @param currentTenantId 当前请求头租户业务编码
     * @param requestTenantId 请求体租户业务编码
     * @return 规范化后的当前租户业务编码
     */
    private String requireCurrentTenant(String currentTenantId, String requestTenantId) {
        String normalizedCurrentTenantId = currentTenantId == null ? "" : currentTenantId.trim();
        if (normalizedCurrentTenantId.isEmpty()) {
            throw new BusinessException(ERROR_DEBUG_TENANT_REQUIRED, "开放 API 调试租户不能为空");
        }
        String normalizedRequestTenantId = requestTenantId == null ? "" : requestTenantId.trim();
        if (!normalizedRequestTenantId.isEmpty() && !normalizedCurrentTenantId.equals(normalizedRequestTenantId)) {
            throw new BusinessException(ERROR_DEBUG_TENANT_MISMATCH, "开放 API 调试租户与当前请求租户不一致");
        }
        return normalizedCurrentTenantId;
    }
}
