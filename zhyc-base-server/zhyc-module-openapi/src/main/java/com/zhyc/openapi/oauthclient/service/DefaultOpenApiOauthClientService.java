/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.domain.OpenApiAppAuthMode;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClient;
import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClientStatus;
import com.zhyc.openapi.oauthclient.repository.OpenApiOauthClientRepository;
import com.zhyc.openapi.support.OpenApiOauthScopeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认开放平台 OAuth2 客户端映射业务服务实现。
 */
@Service
public class DefaultOpenApiOauthClientService implements OpenApiOauthClientService {

    /** OAuth2 客户端映射保存命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_TENANT_ID_REQUIRED";
    /** 租户业务编码格式非法错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_OAUTH_CLIENT_TENANT_ID_INVALID";
    /** 开发者应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_APP_CODE_REQUIRED";
    /** 开发者应用编码格式非法错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_OAUTH_CLIENT_APP_CODE_INVALID";
    /** OAuth2 客户端 ID 为空错误码。 */
    private static final String ERROR_CLIENT_ID_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_CLIENT_ID_REQUIRED";
    /** OAuth2 客户端 ID 格式非法错误码。 */
    private static final String ERROR_CLIENT_ID_INVALID = "ZHYC_OPENAPI_OAUTH_CLIENT_CLIENT_ID_INVALID";
    /** OAuth2 授权范围为空错误码。 */
    private static final String ERROR_SCOPE_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_SCOPE_REQUIRED";
    /** OAuth2 授权范围不受支持错误码。 */
    private static final String ERROR_SCOPE_UNSUPPORTED = "ZHYC_OPENAPI_OAUTH_CLIENT_SCOPE_UNSUPPORTED";
    /** OAuth2 客户端映射状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_STATUS_REQUIRED";
    /** OAuth2 客户端映射状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_OAUTH_CLIENT_STATUS_UNSUPPORTED";
    /** 开发者应用不存在错误码。 */
    private static final String ERROR_APP_NOT_FOUND = "ZHYC_OPENAPI_OAUTH_CLIENT_APP_NOT_FOUND";
    /** 开发者应用鉴权模式不允许 OAuth2 客户端映射错误码。 */
    private static final String ERROR_APP_AUTH_MODE_INVALID = "ZHYC_OPENAPI_OAUTH_CLIENT_APP_AUTH_MODE_INVALID";

    /** OAuth2 客户端映射仓储。 */
    private final OpenApiOauthClientRepository oauthClientRepository;
    /** 开发者应用仓储。 */
    private final OpenApiAppRepository appRepository;

    /**
     * 创建默认 OAuth2 客户端映射业务服务。
     *
     * @param oauthClientRepository OAuth2 客户端映射仓储
     * @param appRepository 开发者应用仓储
     */
    public DefaultOpenApiOauthClientService(OpenApiOauthClientRepository oauthClientRepository,
                                            OpenApiAppRepository appRepository) {
        this.oauthClientRepository = Objects.requireNonNull(oauthClientRepository, "OAuth2 客户端映射仓储不能为空");
        this.appRepository = Objects.requireNonNull(appRepository, "开发者应用仓储不能为空");
    }

    @Override
    public List<OpenApiOauthClientResponse> listClients(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return oauthClientRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiOauthClientSaveCommand command) {
        OpenApiOauthClientSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "OAuth2 客户端映射保存命令不能为空");
        String tenantId = requireValidTenantId(requiredCommand.getTenantId());
        String appCode = requireValidAppCode(requiredCommand.getAppCode());
        requireAppSupportsOauth2(tenantId, appCode);
        OpenApiOauthClient client = new OpenApiOauthClient(null,
                tenantId,
                appCode,
                requireValidClientId(requiredCommand.getClientId()),
                normalizeScopes(requiredCommand.getAllowedScopes()),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        oauthClientRepository.save(client);
    }

    private OpenApiOauthClientResponse toResponse(OpenApiOauthClient client) {
        return new OpenApiOauthClientResponse(client.getClientId(), client.getAllowedScopes(), client.getStatus());
    }

    /**
     * 校验开发者应用属于当前租户且启用 OAuth2/OIDC 鉴权。
     *
     * <p>OAuth2 客户端映射是第三方应用入口，必须绑定真实租户应用，避免授权中心客户端越过开放平台应用边界。</p>
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     */
    private void requireAppSupportsOauth2(String tenantId, String appCode) {
        OpenApiApp app = appRepository.findByTenantIdAndAppCode(tenantId, appCode)
                .orElseThrow(() -> businessFailure(ERROR_APP_NOT_FOUND,
                        "开发者应用不存在或不属于当前租户: " + appCode));
        if (!OpenApiAppAuthMode.OAUTH2.getCode().equals(app.getAuthMode())
                && !OpenApiAppAuthMode.BOTH.getCode().equals(app.getAuthMode())) {
            throw businessFailure(ERROR_APP_AUTH_MODE_INVALID, "开发者应用未启用 OAuth2/OIDC 鉴权: " + appCode);
        }
    }

    /**
     * 校验并规范化租户业务编码。
     *
     * <p>租户业务编码是 OAuth2 客户端映射共享表隔离键，禁止包含空白字符，避免第三方应用授权边界出现歧义。</p>
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
     * 校验并规范化 OAuth2 客户端 ID。
     *
     * @param clientId 认证中心 OAuth2 客户端 ID
     * @return 规范化后的 OAuth2 客户端 ID
     */
    private String requireValidClientId(String clientId) {
        String normalized = requireText(clientId, ERROR_CLIENT_ID_REQUIRED, "OAuth2 客户端 ID 不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_CLIENT_ID_INVALID, "OAuth2 客户端 ID 不能包含空白字符");
        }
        return normalized;
    }

    private String normalizeScopes(String scopes) {
        String normalized = requireText(scopes, ERROR_SCOPE_REQUIRED, "OAuth2 授权范围不能为空")
                .replaceAll("\\s+", " ");
        requireSupportedScopes(normalized);
        return normalized;
    }

    /**
     * 校验 OAuth2 授权范围。
     *
     * @param normalizedScopes 已规范化的授权范围，多个 scope 使用空格分隔
     */
    private void requireSupportedScopes(String normalizedScopes) {
        for (String scope : normalizedScopes.split(" ")) {
            try {
                OpenApiOauthScopeValidator.requireSupportedScope(scope);
            } catch (IllegalArgumentException ex) {
                throw businessFailure(ERROR_SCOPE_UNSUPPORTED, ex.getMessage());
            }
        }
    }

    /**
     * 校验并规范化 OAuth2 客户端映射状态。
     *
     * @param status 客户端映射状态
     * @return 规范化后的状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "客户端映射状态不能为空");
        try {
            return OpenApiOauthClientStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
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
