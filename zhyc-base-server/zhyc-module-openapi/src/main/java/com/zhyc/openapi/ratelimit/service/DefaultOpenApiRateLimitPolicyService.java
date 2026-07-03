/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.ratelimit.domain.OpenApiRateLimitPolicy;
import com.zhyc.openapi.ratelimit.domain.OpenApiRateLimitPolicyStatus;
import com.zhyc.openapi.ratelimit.repository.OpenApiRateLimitPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认开放 API 限流策略业务服务实现。
 */
@Service
public class DefaultOpenApiRateLimitPolicyService implements OpenApiRateLimitPolicyService {

    /** 限流策略保存命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_RATE_LIMIT_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_RATE_LIMIT_TENANT_ID_REQUIRED";
    /** 租户业务编码格式非法错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_RATE_LIMIT_TENANT_ID_INVALID";
    /** 开发者应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_RATE_LIMIT_APP_CODE_REQUIRED";
    /** 开发者应用编码格式非法错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_RATE_LIMIT_APP_CODE_INVALID";
    /** 开放 API 业务编码为空错误码。 */
    private static final String ERROR_API_CODE_REQUIRED = "ZHYC_OPENAPI_RATE_LIMIT_API_CODE_REQUIRED";
    /** 开放 API 业务编码格式非法错误码。 */
    private static final String ERROR_API_CODE_INVALID = "ZHYC_OPENAPI_RATE_LIMIT_API_CODE_INVALID";
    /** 限流次数非法错误码。 */
    private static final String ERROR_LIMIT_COUNT_INVALID = "ZHYC_OPENAPI_RATE_LIMIT_COUNT_INVALID";
    /** 限流窗口非法错误码。 */
    private static final String ERROR_WINDOW_SECONDS_INVALID = "ZHYC_OPENAPI_RATE_LIMIT_WINDOW_SECONDS_INVALID";
    /** 限流策略状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_RATE_LIMIT_STATUS_REQUIRED";
    /** 限流策略状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_RATE_LIMIT_STATUS_UNSUPPORTED";
    /** 开发者应用不存在错误码。 */
    private static final String ERROR_APP_NOT_FOUND = "ZHYC_OPENAPI_RATE_LIMIT_APP_NOT_FOUND";

    /** 限流策略仓储。 */
    private final OpenApiRateLimitPolicyRepository rateLimitPolicyRepository;
    /** 开发者应用仓储。 */
    private final OpenApiAppRepository appRepository;

    /**
     * 创建默认限流策略业务服务。
     *
     * @param rateLimitPolicyRepository 限流策略仓储
     * @param appRepository 开发者应用仓储
     */
    public DefaultOpenApiRateLimitPolicyService(OpenApiRateLimitPolicyRepository rateLimitPolicyRepository,
                                                OpenApiAppRepository appRepository) {
        this.rateLimitPolicyRepository = Objects.requireNonNull(rateLimitPolicyRepository, "限流策略仓储不能为空");
        this.appRepository = Objects.requireNonNull(appRepository, "开发者应用仓储不能为空");
    }

    @Override
    public List<OpenApiRateLimitPolicyResponse> listPolicies(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return rateLimitPolicyRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiRateLimitPolicySaveCommand command) {
        OpenApiRateLimitPolicySaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "限流策略保存命令不能为空");
        String tenantId = requireValidTenantId(requiredCommand.getTenantId());
        String appCode = requireValidAppCode(requiredCommand.getAppCode());
        requireAppBelongsToTenant(tenantId, appCode);
        OpenApiRateLimitPolicy policy = new OpenApiRateLimitPolicy(null,
                tenantId,
                appCode,
                requireValidApiCode(requiredCommand.getApiCode()),
                requirePositive(requiredCommand.getLimitCount(), ERROR_LIMIT_COUNT_INVALID, "限流次数必须大于 0"),
                requirePositive(requiredCommand.getWindowSeconds(), ERROR_WINDOW_SECONDS_INVALID,
                        "限流窗口必须大于 0"),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        rateLimitPolicyRepository.save(policy);
    }

    private OpenApiRateLimitPolicyResponse toResponse(OpenApiRateLimitPolicy policy) {
        return new OpenApiRateLimitPolicyResponse(policy.getApiCode(), policy.getLimitCount(),
                policy.getWindowSeconds(), policy.getStatus());
    }

    /**
     * 校验开发者应用属于当前租户。
     *
     * <p>限流策略是应用级网关控制项，必须绑定真实租户应用，避免孤立策略影响运行态判断。</p>
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
     * <p>租户业务编码是限流策略共享表隔离键，禁止包含空白字符，避免网关限流策略跨租户边界出现歧义。</p>
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
     * <p>应用编码会参与限流策略匹配，禁止包含空白字符，避免同一应用出现多种不可见编码形态。</p>
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
     * 校验并规范化开放 API 业务编码。
     *
     * <p>开放 API 业务编码会参与网关限流匹配，禁止包含空白字符，避免限流策略无法命中实际 API。</p>
     *
     * @param apiCode 开放 API 业务编码
     * @return 规范化后的开放 API 业务编码
     */
    private String requireValidApiCode(String apiCode) {
        String normalized = requireText(apiCode, ERROR_API_CODE_REQUIRED, "开放 API 业务编码不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_API_CODE_INVALID, "开放 API 业务编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验限流数值必须为正数。
     *
     * @param value 待校验数值
     * @param code 业务错误码
     * @param message 校验失败消息
     * @return 原始正数值
     */
    private int requirePositive(int value, String code, String message) {
        if (value <= 0) {
            throw businessFailure(code, message);
        }
        return value;
    }

    /**
     * 校验并规范化限流策略状态。
     *
     * @param status 限流策略状态
     * @return 规范化后的状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "限流策略状态不能为空");
        try {
            return OpenApiRateLimitPolicyStatus.fromCode(normalized).getCode();
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
