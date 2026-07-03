/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.audit.domain.OpenApiCallAudit;
import com.zhyc.openapi.audit.repository.OpenApiCallAuditRepository;
import com.zhyc.openapi.support.OpenApiHttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 默认开放 API 调用审计业务服务实现。
 */
@Service
public class DefaultOpenApiCallAuditService implements OpenApiCallAuditService {

    /** 开放 API 调用审计仓储。 */
    private final OpenApiCallAuditRepository auditRepository;
    /** 调用审计记录命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_AUDIT_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_AUDIT_TENANT_ID_REQUIRED";
    /** 租户业务编码格式错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_AUDIT_TENANT_ID_INVALID";
    /** 开发者应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_AUDIT_APP_CODE_REQUIRED";
    /** 开发者应用编码格式错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_AUDIT_APP_CODE_INVALID";
    /** API 访问密钥为空错误码。 */
    private static final String ERROR_ACCESS_KEY_REQUIRED = "ZHYC_OPENAPI_AUDIT_ACCESS_KEY_REQUIRED";
    /** API 访问密钥格式错误码。 */
    private static final String ERROR_ACCESS_KEY_INVALID = "ZHYC_OPENAPI_AUDIT_ACCESS_KEY_INVALID";
    /** API 业务编码为空错误码。 */
    private static final String ERROR_API_CODE_REQUIRED = "ZHYC_OPENAPI_AUDIT_API_CODE_REQUIRED";
    /** API 业务编码格式错误码。 */
    private static final String ERROR_API_CODE_INVALID = "ZHYC_OPENAPI_AUDIT_API_CODE_INVALID";
    /** HTTP 方法为空错误码。 */
    private static final String ERROR_HTTP_METHOD_REQUIRED = "ZHYC_OPENAPI_AUDIT_HTTP_METHOD_REQUIRED";
    /** HTTP 方法不支持错误码。 */
    private static final String ERROR_HTTP_METHOD_UNSUPPORTED = "ZHYC_OPENAPI_AUDIT_HTTP_METHOD_UNSUPPORTED";
    /** 请求路径为空错误码。 */
    private static final String ERROR_REQUEST_PATH_REQUIRED = "ZHYC_OPENAPI_AUDIT_REQUEST_PATH_REQUIRED";
    /** 请求路径格式错误码。 */
    private static final String ERROR_REQUEST_PATH_INVALID = "ZHYC_OPENAPI_AUDIT_REQUEST_PATH_INVALID";
    /** 响应状态码为空错误码。 */
    private static final String ERROR_RESPONSE_STATUS_REQUIRED = "ZHYC_OPENAPI_AUDIT_RESPONSE_STATUS_REQUIRED";
    /** 响应状态码范围错误码。 */
    private static final String ERROR_RESPONSE_STATUS_INVALID = "ZHYC_OPENAPI_AUDIT_RESPONSE_STATUS_INVALID";
    /** 调用耗时为空错误码。 */
    private static final String ERROR_DURATION_MS_REQUIRED = "ZHYC_OPENAPI_AUDIT_DURATION_MS_REQUIRED";
    /** 调用耗时范围错误码。 */
    private static final String ERROR_DURATION_MS_INVALID = "ZHYC_OPENAPI_AUDIT_DURATION_MS_INVALID";
    /** 失败调用错误编码为空错误码。 */
    private static final String ERROR_ERROR_CODE_REQUIRED = "ZHYC_OPENAPI_AUDIT_ERROR_CODE_REQUIRED";
    /** 失败调用错误编码格式错误码。 */
    private static final String ERROR_ERROR_CODE_INVALID = "ZHYC_OPENAPI_AUDIT_ERROR_CODE_INVALID";
    /** 客户端 IP 为空错误码。 */
    private static final String ERROR_CLIENT_IP_REQUIRED = "ZHYC_OPENAPI_AUDIT_CLIENT_IP_REQUIRED";
    /** 客户端 IP 格式错误码。 */
    private static final String ERROR_CLIENT_IP_INVALID = "ZHYC_OPENAPI_AUDIT_CLIENT_IP_INVALID";
    /** 请求追踪 ID 为空错误码。 */
    private static final String ERROR_REQUEST_ID_REQUIRED = "ZHYC_OPENAPI_AUDIT_REQUEST_ID_REQUIRED";
    /** 请求追踪 ID 格式错误码。 */
    private static final String ERROR_REQUEST_ID_INVALID = "ZHYC_OPENAPI_AUDIT_REQUEST_ID_INVALID";

    /**
     * 创建默认开放 API 调用审计业务服务。
     *
     * @param auditRepository 开放 API 调用审计仓储
     */
    public DefaultOpenApiCallAuditService(OpenApiCallAuditRepository auditRepository) {
        this.auditRepository = Objects.requireNonNull(auditRepository, "开放 API 调用审计仓储不能为空");
    }

    @Override
    public List<OpenApiCallAuditResponse> listAudits(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return auditRepository.findByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<OpenApiCallAuditResponse> listErrorLogs(String tenantId, String appCode) {
        String requiredTenantId = requireValidTenantId(tenantId);
        String requiredAppCode = requireValidAppCode(appCode);
        return auditRepository.findErrorLogsByTenantIdAndAppCode(requiredTenantId, requiredAppCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void record(OpenApiCallAuditRecordCommand command) {
        OpenApiCallAuditRecordCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "开放 API 调用审计记录命令不能为空");
        int successFlag = Boolean.TRUE.equals(requiredCommand.getSuccess()) ? 1 : 0;
        OpenApiCallAudit audit = new OpenApiCallAudit(null,
                requireValidTenantId(requiredCommand.getTenantId()),
                requireValidAppCode(requiredCommand.getAppCode()),
                requireValidAccessKey(requiredCommand.getAccessKey()),
                requireValidApiCode(requiredCommand.getApiCode()),
                requireSupportedHttpMethod(requiredCommand.getHttpMethod()),
                requireValidRequestPath(requiredCommand.getRequestPath()),
                requireValidResponseStatus(requiredCommand.getResponseStatus()),
                requireValidDurationMs(requiredCommand.getDurationMs()),
                successFlag,
                normalizeErrorCode(successFlag, requiredCommand.getErrorCode()),
                requireValidClientIp(requiredCommand.getClientIp()),
                requireValidRequestId(requiredCommand.getRequestId()),
                requiredCommand.getCalledAt() == null ? LocalDateTime.now() : requiredCommand.getCalledAt(),
                null);
        auditRepository.save(audit);
    }

    private OpenApiCallAuditResponse toResponse(OpenApiCallAudit audit) {
        return new OpenApiCallAuditResponse(audit.getAccessKey(), audit.getApiCode(), audit.getHttpMethod(),
                audit.getRequestPath(), audit.getResponseStatus(), audit.getDurationMs(), audit.getSuccess(),
                audit.getErrorCode(), audit.getClientIp(), audit.getRequestId(), audit.getCalledAt());
    }

    /**
     * 校验并规范化租户业务编码。
     *
     * <p>租户业务编码是开放 API 调用审计共享表隔离键，禁止包含空白字符，避免审计记录跨租户归因出现歧义。</p>
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
     * <p>调用审计需要与授权、限流和密钥配置按应用编码准确关联，禁止包含空白字符。</p>
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
     * <p>访问密钥用于开放 API 调用归因和安全审计，禁止包含空白字符，避免审计记录与密钥台账无法匹配。</p>
     *
     * @param accessKey API 访问密钥
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
     * 校验并规范化 API 业务编码。
     *
     * <p>API 业务编码用于把调用审计关联到开放 API 目录、授权和限流策略，禁止包含空白字符。</p>
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
     * 校验并规范化开放 API 调用请求路径。
     *
     * <p>请求路径用于审计追踪和开放 API 目录规则匹配，禁止包含空白字符，避免路径归因和规则匹配出现歧义。</p>
     *
     * @param requestPath 请求路径
     * @return 规范化后的请求路径
     */
    private String requireValidRequestPath(String requestPath) {
        String normalized = requireText(requestPath, ERROR_REQUEST_PATH_REQUIRED, "请求路径不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_REQUEST_PATH_INVALID, "请求路径不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 规范化开放 API 调用失败错误编码。
     *
     * <p>错误编码用于失败原因统计、告警聚合和问题定位；成功调用允许为空，失败调用必须提供稳定错误编码。</p>
     *
     * @param successFlag 调用是否成功，1 表示成功，0 表示失败
     * @param errorCode 错误编码
     * @return 规范化后的错误编码；空白值返回 null
     */
    private String normalizeErrorCode(int successFlag, String errorCode) {
        String normalized = trimToNull(errorCode);
        if (successFlag == 0 && normalized == null) {
            throw businessFailure(ERROR_ERROR_CODE_REQUIRED, "失败调用错误编码不能为空");
        }
        if (normalized != null && normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_ERROR_CODE_INVALID, "错误编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化客户端 IP。
     *
     * <p>客户端 IP 用于开放 API 访问来源追踪、风控和审计检索，禁止包含空白字符，避免来源归因出现歧义。</p>
     *
     * @param clientIp 客户端 IP
     * @return 规范化后的客户端 IP
     */
    private String requireValidClientIp(String clientIp) {
        String normalized = requireText(clientIp, ERROR_CLIENT_IP_REQUIRED, "客户端 IP 不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_CLIENT_IP_INVALID, "客户端 IP 不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化请求追踪 ID。
     *
     * <p>请求追踪 ID 用于串联开放 API 网关日志、业务日志和审计记录，禁止包含空白字符，避免链路关联失败。</p>
     *
     * @param requestId 请求追踪 ID
     * @return 规范化后的请求追踪 ID
     */
    private String requireValidRequestId(String requestId) {
        String normalized = requireText(requestId, ERROR_REQUEST_ID_REQUIRED, "请求追踪 ID 不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_REQUEST_ID_INVALID, "请求追踪 ID 不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验文本参数不能为空白。
     *
     * @param value 待校验文本
     * @param code 业务错误码
     * @param message 中文错误消息
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
     * 校验并规范化开放 API 调用审计 HTTP 方法。
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
     * 校验开放 API 调用审计 HTTP 响应状态码。
     *
     * @param responseStatus HTTP 响应状态码
     * @return 合法的 HTTP 响应状态码
     */
    private Integer requireValidResponseStatus(Integer responseStatus) {
        Integer requiredResponseStatus = requireObject(responseStatus, ERROR_RESPONSE_STATUS_REQUIRED,
                "HTTP 响应状态码不能为空");
        if (requiredResponseStatus < 100 || requiredResponseStatus > 599) {
            throw businessFailure(ERROR_RESPONSE_STATUS_INVALID, "HTTP 响应状态码必须在 100 到 599 之间");
        }
        return requiredResponseStatus;
    }

    /**
     * 校验开放 API 调用审计耗时。
     *
     * @param durationMs 调用耗时毫秒
     * @return 合法的调用耗时毫秒
     */
    private Long requireValidDurationMs(Long durationMs) {
        Long requiredDurationMs = requireObject(durationMs, ERROR_DURATION_MS_REQUIRED, "调用耗时不能为空");
        if (requiredDurationMs < 0) {
            throw businessFailure(ERROR_DURATION_MS_INVALID, "调用耗时不能小于 0 毫秒");
        }
        return requiredDurationMs;
    }

    /**
     * 校验对象参数不能为空。
     *
     * @param value 待校验对象
     * @param code 业务错误码
     * @param message 中文错误消息
     * @param <T> 对象类型
     * @return 非空对象
     */
    private <T> T requireObject(T value, String code, String message) {
        if (value == null) {
            throw businessFailure(code, message);
        }
        return value;
    }

    /**
     * 创建面向调用方的业务异常。
     *
     * @param code 业务错误码
     * @param message 中文错误消息
     * @return 业务异常
     */
    private BusinessException businessFailure(String code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 裁剪文本，空白文本统一转换为 null。
     *
     * @param value 待裁剪文本
     * @return 裁剪后的文本，空白文本返回 null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
