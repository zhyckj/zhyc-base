/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.apikey.domain.OpenApiApiKey;
import com.zhyc.openapi.apikey.domain.OpenApiApiKeyStatus;
import com.zhyc.openapi.apikey.repository.OpenApiApiKeyRepository;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.domain.OpenApiAppAuthMode;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 默认 API Key 业务服务实现。
 */
@Service
public class DefaultOpenApiApiKeyService implements OpenApiApiKeyService {

    /** API Key 保存命令为空错误码。 */
    private static final String ERROR_SAVE_COMMAND_REQUIRED = "ZHYC_OPENAPI_API_KEY_SAVE_COMMAND_REQUIRED";
    /** API Key Secret 轮换命令为空错误码。 */
    private static final String ERROR_ROTATE_COMMAND_REQUIRED = "ZHYC_OPENAPI_API_KEY_ROTATE_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_API_KEY_TENANT_ID_REQUIRED";
    /** 租户业务编码格式非法错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_API_KEY_TENANT_ID_INVALID";
    /** 开发者应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_API_KEY_APP_CODE_REQUIRED";
    /** 开发者应用编码格式非法错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_API_KEY_APP_CODE_INVALID";
    /** API 访问密钥为空错误码。 */
    private static final String ERROR_ACCESS_KEY_REQUIRED = "ZHYC_OPENAPI_API_KEY_ACCESS_KEY_REQUIRED";
    /** API 访问密钥格式非法错误码。 */
    private static final String ERROR_ACCESS_KEY_INVALID = "ZHYC_OPENAPI_API_KEY_ACCESS_KEY_INVALID";
    /** API Secret 密文为空错误码。 */
    private static final String ERROR_SECRET_CIPHER_REQUIRED = "ZHYC_OPENAPI_API_KEY_SECRET_CIPHER_REQUIRED";
    /** 新 API Secret 密文为空错误码。 */
    private static final String ERROR_ROTATE_SECRET_CIPHER_REQUIRED =
            "ZHYC_OPENAPI_API_KEY_ROTATE_SECRET_CIPHER_REQUIRED";
    /** API Secret 密文格式非法错误码。 */
    private static final String ERROR_SECRET_CIPHER_INVALID = "ZHYC_OPENAPI_API_KEY_SECRET_CIPHER_INVALID";
    /** API Key 状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_API_KEY_STATUS_REQUIRED";
    /** API Key 状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_API_KEY_STATUS_UNSUPPORTED";
    /** API Key 过期时间非法错误码。 */
    private static final String ERROR_EXPIRE_AT_INVALID = "ZHYC_OPENAPI_API_KEY_EXPIRE_AT_INVALID";
    /** 开发者应用不存在错误码。 */
    private static final String ERROR_APP_NOT_FOUND = "ZHYC_OPENAPI_API_KEY_APP_NOT_FOUND";
    /** 开发者应用鉴权模式不允许签发 API Key 错误码。 */
    private static final String ERROR_APP_AUTH_MODE_INVALID = "ZHYC_OPENAPI_API_KEY_APP_AUTH_MODE_INVALID";

    /** API Key 仓储。 */
    private final OpenApiApiKeyRepository apiKeyRepository;
    /** 开发者应用仓储。 */
    private final OpenApiAppRepository appRepository;

    /**
     * 创建默认 API Key 业务服务。
     *
     * @param apiKeyRepository API Key 仓储
     * @param appRepository 开发者应用仓储
     */
    public DefaultOpenApiApiKeyService(OpenApiApiKeyRepository apiKeyRepository,
                                       OpenApiAppRepository appRepository) {
        this.apiKeyRepository = Objects.requireNonNull(apiKeyRepository, "API Key 仓储不能为空");
        this.appRepository = Objects.requireNonNull(appRepository, "开发者应用仓储不能为空");
    }

    @Override
    public List<OpenApiApiKeyResponse> listApiKeys(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return apiKeyRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiApiKeySaveCommand command) {
        OpenApiApiKeySaveCommand requiredCommand = requireObject(command, ERROR_SAVE_COMMAND_REQUIRED,
                "API Key 保存命令不能为空");
        String tenantId = requireValidTenantId(requiredCommand.getTenantId());
        String appCode = requireValidAppCode(requiredCommand.getAppCode());
        requireAppSupportsApiKey(tenantId, appCode);
        OpenApiApiKey apiKey = new OpenApiApiKey(null,
                tenantId,
                appCode,
                requireValidAccessKey(requiredCommand.getAccessKey()),
                requireValidSecretCipher(requiredCommand.getSecretCipher(), ERROR_SECRET_CIPHER_REQUIRED,
                        "API Secret 密文不能为空"),
                requireSupportedStatus(requiredCommand.getStatus()),
                requireValidExpireAt(requiredCommand.getExpireAt()), null, null);
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public void rotateSecret(OpenApiApiKeyRotateCommand command) {
        OpenApiApiKeyRotateCommand requiredCommand = requireObject(command, ERROR_ROTATE_COMMAND_REQUIRED,
                "API Key Secret 轮换命令不能为空");
        String tenantId = requireValidTenantId(requiredCommand.getTenantId());
        String appCode = requireValidAppCode(requiredCommand.getAppCode());
        requireAppSupportsApiKey(tenantId, appCode);
        OpenApiApiKey apiKey = new OpenApiApiKey(null,
                tenantId,
                appCode,
                requireValidAccessKey(requiredCommand.getAccessKey()),
                requireValidSecretCipher(requiredCommand.getSecretCipher(), ERROR_ROTATE_SECRET_CIPHER_REQUIRED,
                        "新 API Secret 密文不能为空"),
                OpenApiApiKeyStatus.ENABLED.getCode(),
                requireValidExpireAt(requiredCommand.getExpireAt()), null, null);
        apiKeyRepository.save(apiKey);
    }

    private OpenApiApiKeyResponse toResponse(OpenApiApiKey apiKey) {
        return new OpenApiApiKeyResponse(apiKey.getAppCode(), apiKey.getAccessKey(),
                maskSecret(apiKey.getSecretCipher()), apiKey.getStatus(), apiKey.getExpireAt());
    }

    /**
     * 校验开发者应用属于当前租户且启用 API Key 鉴权。
     *
     * <p>API Key 是应用级凭证，保存和轮换前必须确认应用归属，避免给不存在或其他租户应用签发密钥。</p>
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     */
    private void requireAppSupportsApiKey(String tenantId, String appCode) {
        OpenApiApp app = appRepository.findByTenantIdAndAppCode(tenantId, appCode)
                .orElseThrow(() -> businessFailure(ERROR_APP_NOT_FOUND,
                        "开发者应用不存在或不属于当前租户: " + appCode));
        if (!OpenApiAppAuthMode.API_KEY.getCode().equals(app.getAuthMode())
                && !OpenApiAppAuthMode.BOTH.getCode().equals(app.getAuthMode())) {
            throw businessFailure(ERROR_APP_AUTH_MODE_INVALID, "开发者应用未启用 API Key 鉴权: " + appCode);
        }
    }

    private String maskSecret(String secretValue) {
        String normalized = trimToNull(secretValue);
        if (normalized == null || normalized.length() <= 8) {
            return "****";
        }
        return normalized.substring(0, 4) + "****" + normalized.substring(normalized.length() - 4);
    }

    /**
     * 校验并规范化租户业务编码。
     *
     * <p>租户业务编码是 API Key 共享表隔离键，禁止包含空白字符，避免密钥查询、保存和轮换跨租户边界出现歧义。</p>
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
     * 校验并规范化 API 访问密钥。
     *
     * @param accessKey API 访问密钥明文标识
     * @return 规范化后的 API 访问密钥
     */
    private String requireValidAccessKey(String accessKey) {
        String normalized = requireText(accessKey, ERROR_ACCESS_KEY_REQUIRED, "API 访问密钥不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_ACCESS_KEY_INVALID, "API 访问密钥不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化 API Secret 密文。
     *
     * <p>API Secret 密文是开放 API 网关验签的核心材料，禁止包含空白字符，避免密钥持久化和运行态比对结果不一致。</p>
     *
     * @param secretCipher API Secret 密文
     * @param blankCode 空值错误码
     * @param blankMessage 空值提示
     * @return 规范化后的 API Secret 密文
     */
    private String requireValidSecretCipher(String secretCipher, String blankCode, String blankMessage) {
        String normalized = requireText(secretCipher, blankCode, blankMessage);
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_SECRET_CIPHER_INVALID, "API Secret 密文不能包含空白字符");
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
     * 校验并规范化 API Key 状态。
     *
     * @param status API Key 状态
     * @return 规范化后的 API Key 状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "API Key 状态不能为空");
        try {
            return OpenApiApiKeyStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验 API Key 过期时间。
     *
     * @param expireAt API Key 过期时间
     * @return 合法的 API Key 过期时间；为空表示不设置固定过期时间
     */
    private LocalDateTime requireValidExpireAt(LocalDateTime expireAt) {
        if (expireAt != null && expireAt.isBefore(LocalDateTime.now())) {
            throw businessFailure(ERROR_EXPIRE_AT_INVALID, "API Key 过期时间不能早于当前时间");
        }
        return expireAt;
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
