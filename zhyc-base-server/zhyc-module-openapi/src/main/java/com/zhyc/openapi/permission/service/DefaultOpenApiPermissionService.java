/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.permission.domain.OpenApiPermission;
import com.zhyc.openapi.permission.domain.OpenApiPermissionStatus;
import com.zhyc.openapi.permission.repository.OpenApiPermissionRepository;
import com.zhyc.openapi.support.OpenApiHttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认开放 API 权限授权业务服务实现。
 */
@Service
public class DefaultOpenApiPermissionService implements OpenApiPermissionService {

    /** 开放 API 权限授权保存命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_PERMISSION_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_PERMISSION_TENANT_ID_REQUIRED";
    /** 租户业务编码格式非法错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_PERMISSION_TENANT_ID_INVALID";
    /** 开发者应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_PERMISSION_APP_CODE_REQUIRED";
    /** 开发者应用编码格式非法错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_PERMISSION_APP_CODE_INVALID";
    /** API 业务编码为空错误码。 */
    private static final String ERROR_API_CODE_REQUIRED = "ZHYC_OPENAPI_PERMISSION_API_CODE_REQUIRED";
    /** API 业务编码格式非法错误码。 */
    private static final String ERROR_API_CODE_INVALID = "ZHYC_OPENAPI_PERMISSION_API_CODE_INVALID";
    /** API 名称为空错误码。 */
    private static final String ERROR_API_NAME_REQUIRED = "ZHYC_OPENAPI_PERMISSION_API_NAME_REQUIRED";
    /** 请求路径匹配规则为空错误码。 */
    private static final String ERROR_PATH_PATTERN_REQUIRED = "ZHYC_OPENAPI_PERMISSION_PATH_PATTERN_REQUIRED";
    /** 请求路径匹配规则不是根相对路径错误码。 */
    private static final String ERROR_PATH_PATTERN_NOT_ROOT_RELATIVE =
            "ZHYC_OPENAPI_PERMISSION_PATH_PATTERN_NOT_ROOT_RELATIVE";
    /** 请求路径匹配规则格式非法错误码。 */
    private static final String ERROR_PATH_PATTERN_INVALID = "ZHYC_OPENAPI_PERMISSION_PATH_PATTERN_INVALID";
    /** 授权状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_PERMISSION_STATUS_REQUIRED";
    /** 授权状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_PERMISSION_STATUS_UNSUPPORTED";
    /** HTTP 方法为空错误码。 */
    private static final String ERROR_HTTP_METHOD_REQUIRED = "ZHYC_OPENAPI_PERMISSION_HTTP_METHOD_REQUIRED";
    /** HTTP 方法不受支持错误码。 */
    private static final String ERROR_HTTP_METHOD_UNSUPPORTED = "ZHYC_OPENAPI_PERMISSION_HTTP_METHOD_UNSUPPORTED";
    /** 开发者应用不存在错误码。 */
    private static final String ERROR_APP_NOT_FOUND = "ZHYC_OPENAPI_PERMISSION_APP_NOT_FOUND";

    /** 开放 API 权限授权仓储。 */
    private final OpenApiPermissionRepository permissionRepository;
    /** 开发者应用仓储。 */
    private final OpenApiAppRepository appRepository;

    /**
     * 创建默认开放 API 权限授权业务服务。
     *
     * @param permissionRepository 开放 API 权限授权仓储
     * @param appRepository 开发者应用仓储
     */
    public DefaultOpenApiPermissionService(OpenApiPermissionRepository permissionRepository,
                                           OpenApiAppRepository appRepository) {
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "开放 API 权限授权仓储不能为空");
        this.appRepository = Objects.requireNonNull(appRepository, "开发者应用仓储不能为空");
    }

    @Override
    public List<OpenApiPermissionResponse> listPermissions(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return permissionRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiPermissionSaveCommand command) {
        OpenApiPermissionSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "开放 API 权限授权保存命令不能为空");
        String tenantId = requireValidTenantId(requiredCommand.getTenantId());
        String appCode = requireValidAppCode(requiredCommand.getAppCode());
        requireAppBelongsToTenant(tenantId, appCode);
        OpenApiPermission permission = new OpenApiPermission(null,
                tenantId,
                appCode,
                requireValidApiCode(requiredCommand.getApiCode()),
                requireText(requiredCommand.getApiName(), ERROR_API_NAME_REQUIRED, "API 名称不能为空"),
                requireSupportedHttpMethod(requiredCommand.getHttpMethod()),
                requireValidPathPattern(requiredCommand.getPathPattern()),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        permissionRepository.save(permission);
    }

    private OpenApiPermissionResponse toResponse(OpenApiPermission permission) {
        return new OpenApiPermissionResponse(permission.getApiCode(), permission.getApiName(),
                permission.getHttpMethod(), permission.getPathPattern(), permission.getStatus());
    }

    /**
     * 校验开发者应用属于当前租户。
     *
     * <p>开放 API 授权必须挂在真实租户应用下，避免给不存在或其他租户应用开放接口访问权。</p>
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     */
    private void requireAppBelongsToTenant(String tenantId, String appCode) {
        appRepository.findByTenantIdAndAppCode(tenantId, appCode)
                .orElseThrow(() -> businessFailure(ERROR_APP_NOT_FOUND,
                        "开发者应用不存在或不属于当前租户: " + appCode));
    }

    /**
     * 校验并规范化租户业务编码。
     *
     * <p>租户业务编码是开放 API 授权共享表隔离键，禁止包含空白字符，避免授权规则跨租户边界出现歧义。</p>
     *
     * @param tenantId 租户业务编码
     * @return 规范化后的租户业务编码
     */
    private String requireValidTenantId(String tenantId) {
        String normalized = requireText(tenantId, ERROR_TENANT_ID_REQUIRED, "租户业务编码不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_TENANT_ID_INVALID, "租户业务编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化开发者应用编码。
     *
     * <p>应用编码用于开放 API 授权边界匹配，禁止包含空白字符，避免查询条件和授权配置出现歧义。</p>
     *
     * @param appCode 开发者应用编码
     * @return 规范化后的开发者应用编码
     */
    private String requireValidAppCode(String appCode) {
        String normalized = requireText(appCode, ERROR_APP_CODE_REQUIRED, "开发者应用编码不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_APP_CODE_INVALID, "开发者应用编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化 API 业务编码。
     *
     * <p>API 业务编码用于授权规则和开放 API 目录关联，禁止包含空白字符，避免授权配置与目录定义无法稳定匹配。</p>
     *
     * @param apiCode API 业务编码
     * @return 规范化后的 API 业务编码
     */
    private String requireValidApiCode(String apiCode) {
        String normalized = requireText(apiCode, ERROR_API_CODE_REQUIRED, "API 业务编码不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_API_CODE_INVALID, "API 业务编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化请求路径匹配规则。
     *
     * <p>请求路径匹配规则用于开放 API 授权匹配，必须是以根斜杠开头且不含空白字符的根相对路径。</p>
     *
     * @param pathPattern 请求路径匹配规则
     * @return 规范化后的请求路径匹配规则
     */
    private String requireValidPathPattern(String pathPattern) {
        String normalized = requireText(pathPattern, ERROR_PATH_PATTERN_REQUIRED, "请求路径匹配规则不能为空");
        if (!normalized.startsWith("/")) {
            throw businessFailure(ERROR_PATH_PATTERN_NOT_ROOT_RELATIVE, "请求路径匹配规则必须以 / 开头");
        }
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_PATH_PATTERN_INVALID, "请求路径匹配规则不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验必填对象不能为空。
     *
     * @param value 待校验对象
     * @param code 业务错误码
     * @param message 校验失败消息
     * @return 非空对象
     */
    private <T> T requireObject(T value, String code, String message) {
        if (value == null) {
            throw businessFailure(code, message);
        }
        return value;
    }

    /**
     * 校验必填文本不能为空白。
     *
     * @param value 待校验文本
     * @param code 业务错误码
     * @param message 校验失败消息
     * @return 去除首尾空白后的文本
     */
    private String requireText(String value, String code, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw businessFailure(code, message);
        }
        return normalized;
    }

    /**
     * 校验并规范化开放 API 授权状态。
     *
     * @param status 开放 API 授权状态
     * @return 规范化后的开放 API 授权状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "授权状态不能为空");
        try {
            return OpenApiPermissionStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验并规范化开放 API 授权 HTTP 方法。
     *
     * @param httpMethod HTTP 方法
     * @return 规范化后的 HTTP 方法
     */
    private String requireSupportedHttpMethod(String httpMethod) {
        String normalized = requireText(httpMethod, ERROR_HTTP_METHOD_REQUIRED, "HTTP 方法不能为空");
        try {
            return OpenApiHttpMethod.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_HTTP_METHOD_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 构造面向开放 API 调用方的业务异常。
     *
     * @param code 稳定业务错误码
     * @param message 中文错误消息
     * @return 业务异常实例
     */
    private BusinessException businessFailure(String code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 裁剪文本并把空白文本转为 null。
     *
     * @param value 原始文本
     * @return 裁剪后的文本；空白或 null 返回 null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
