/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.domain.OpenApiAppAuthMode;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.signature.domain.OpenApiSignatureAlgorithm;
import com.zhyc.openapi.signature.domain.OpenApiSignaturePolicy;
import com.zhyc.openapi.signature.domain.OpenApiSignaturePolicyStatus;
import com.zhyc.openapi.signature.repository.OpenApiSignaturePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认开放 API 签名策略业务服务实现。
 */
@Service
public class DefaultOpenApiSignaturePolicyService implements OpenApiSignaturePolicyService {

    /** 签名策略保存命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_SIGNATURE_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_SIGNATURE_TENANT_ID_REQUIRED";
    /** 租户业务编码格式非法错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_SIGNATURE_TENANT_ID_INVALID";
    /** 开发者应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_SIGNATURE_APP_CODE_REQUIRED";
    /** 开发者应用编码格式非法错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_SIGNATURE_APP_CODE_INVALID";
    /** 签名算法为空错误码。 */
    private static final String ERROR_ALGORITHM_REQUIRED = "ZHYC_OPENAPI_SIGNATURE_ALGORITHM_REQUIRED";
    /** 签名算法不受支持错误码。 */
    private static final String ERROR_ALGORITHM_UNSUPPORTED = "ZHYC_OPENAPI_SIGNATURE_ALGORITHM_UNSUPPORTED";
    /** 时间戳容忍窗口非法错误码。 */
    private static final String ERROR_TIMESTAMP_TOLERANCE_INVALID =
            "ZHYC_OPENAPI_SIGNATURE_TIMESTAMP_TOLERANCE_INVALID";
    /** nonce 有效期非法错误码。 */
    private static final String ERROR_NONCE_TTL_INVALID = "ZHYC_OPENAPI_SIGNATURE_NONCE_TTL_INVALID";
    /** 请求体摘要开关非法错误码。 */
    private static final String ERROR_REQUIRE_BODY_HASH_INVALID =
            "ZHYC_OPENAPI_SIGNATURE_REQUIRE_BODY_HASH_INVALID";
    /** 签名策略状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_SIGNATURE_STATUS_REQUIRED";
    /** 签名策略状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_SIGNATURE_STATUS_UNSUPPORTED";
    /** 开发者应用不存在错误码。 */
    private static final String ERROR_APP_NOT_FOUND = "ZHYC_OPENAPI_SIGNATURE_APP_NOT_FOUND";
    /** 开发者应用鉴权模式不允许签名策略错误码。 */
    private static final String ERROR_APP_AUTH_MODE_INVALID = "ZHYC_OPENAPI_SIGNATURE_APP_AUTH_MODE_INVALID";

    /** 签名策略仓储。 */
    private final OpenApiSignaturePolicyRepository signaturePolicyRepository;
    /** 开发者应用仓储。 */
    private final OpenApiAppRepository appRepository;

    /**
     * 创建默认签名策略业务服务。
     *
     * @param signaturePolicyRepository 签名策略仓储
     * @param appRepository 开发者应用仓储
     */
    public DefaultOpenApiSignaturePolicyService(OpenApiSignaturePolicyRepository signaturePolicyRepository,
                                                OpenApiAppRepository appRepository) {
        this.signaturePolicyRepository = Objects.requireNonNull(signaturePolicyRepository, "签名策略仓储不能为空");
        this.appRepository = Objects.requireNonNull(appRepository, "开发者应用仓储不能为空");
    }

    @Override
    public List<OpenApiSignaturePolicyResponse> listPolicies(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return signaturePolicyRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiSignaturePolicySaveCommand command) {
        OpenApiSignaturePolicySaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "签名策略保存命令不能为空");
        String tenantId = requireValidTenantId(requiredCommand.getTenantId());
        String appCode = requireValidAppCode(requiredCommand.getAppCode());
        requireAppSupportsApiKey(tenantId, appCode);
        OpenApiSignaturePolicy policy = new OpenApiSignaturePolicy(null,
                tenantId,
                appCode,
                requireSupportedAlgorithm(requiredCommand.getAlgorithm()),
                requirePositive(requiredCommand.getTimestampToleranceSeconds(), ERROR_TIMESTAMP_TOLERANCE_INVALID,
                        "时间戳容忍窗口必须大于 0"),
                requirePositive(requiredCommand.getNonceTtlSeconds(), ERROR_NONCE_TTL_INVALID,
                        "nonce 有效期必须大于 0"),
                requireZeroOrOne(requiredCommand.getRequireBodyHash()),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        signaturePolicyRepository.save(policy);
    }

    private OpenApiSignaturePolicyResponse toResponse(OpenApiSignaturePolicy policy) {
        return new OpenApiSignaturePolicyResponse(policy.getAlgorithm(), policy.getTimestampToleranceSeconds(),
                policy.getNonceTtlSeconds(), policy.getRequireBodyHash(), policy.getStatus());
    }

    /**
     * 校验开发者应用属于当前租户且启用 API Key 鉴权。
     *
     * <p>签名策略直接影响网关验签，必须绑定真实租户应用，避免孤立策略或跨租户策略生效。</p>
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

    /**
     * 校验并规范化租户业务编码。
     *
     * <p>租户业务编码是签名策略共享表隔离键，禁止包含空白字符，避免签名策略跨租户生效。</p>
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
     * <p>签名策略按应用生效，禁止包含空白字符，避免同一应用出现多个不可见编码形态。</p>
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
     * 校验并规范化签名算法。
     *
     * @param algorithm 签名算法
     * @return 规范化后的签名算法
     */
    private String requireSupportedAlgorithm(String algorithm) {
        String normalized = requireText(algorithm, ERROR_ALGORITHM_REQUIRED, "签名算法不能为空");
        try {
            return OpenApiSignatureAlgorithm.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_ALGORITHM_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验并规范化签名策略状态。
     *
     * @param status 签名策略状态
     * @return 规范化后的状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "签名策略状态不能为空");
        try {
            return OpenApiSignaturePolicyStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验数值必须为正数。
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
     * 校验请求体摘要开关只能为 0 或 1。
     *
     * @param value 请求体摘要开关
     * @return 原始开关值
     */
    private int requireZeroOrOne(int value) {
        if (value != 0 && value != 1) {
            throw businessFailure(ERROR_REQUIRE_BODY_HASH_INVALID, "请求体摘要开关只支持 0 或 1");
        }
        return value;
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
     * 构造面向开放 API 管理端的业务异常。
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
