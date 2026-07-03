/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.support.OpenApiHttpMethod;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 开放 API 调试代理业务服务默认实现。
 */
@Service
public class DefaultOpenApiDebugService implements OpenApiDebugService {

    /** 调试请求缺失错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_DEBUG_COMMAND_REQUIRED";
    /** 调试路径无效错误码。 */
    private static final String ERROR_PATH_INVALID = "ZHYC_OPENAPI_DEBUG_PATH_INVALID";
    /** 调试参数缺失错误码。 */
    private static final String ERROR_ARGUMENT_REQUIRED = "ZHYC_OPENAPI_DEBUG_ARGUMENT_REQUIRED";
    /** 调试认证方式无效错误码。 */
    private static final String ERROR_AUTH_MODE_INVALID = "ZHYC_OPENAPI_DEBUG_AUTH_MODE_INVALID";
    /** 调试 HTTP 方法不支持错误码。 */
    private static final String ERROR_METHOD_UNSUPPORTED = "ZHYC_OPENAPI_DEBUG_METHOD_UNSUPPORTED";
    /** 请求体摘要算法名称。 */
    private static final String SHA_256 = "SHA-256";

    /** 开放 API 调试网关客户端。 */
    private final OpenApiDebugGatewayClient gatewayClient;

    /**
     * 创建开放 API 调试代理业务服务默认实现。
     *
     * @param gatewayClient 开放 API 调试网关客户端
     */
    public DefaultOpenApiDebugService(OpenApiDebugGatewayClient gatewayClient) {
        this.gatewayClient = Objects.requireNonNull(gatewayClient, "开放 API 调试网关客户端不能为空");
    }

    /**
     * 通过后台代理发送开放 API 调试请求。
     *
     * @param command 开放 API 调试命令
     * @return 开放 API 调试响应
     */
    @Override
    public OpenApiDebugResponse invoke(OpenApiDebugCommand command) {
        validate(command);
        OpenApiDebugGatewayResponse gatewayResponse = gatewayClient.invoke(new OpenApiDebugGatewayRequest(
                command.getTenantId().trim(), normalizeMethod(command.getMethod()), command.getPath().trim(),
                buildGatewayHeaders(command), command.getBody() == null ? "" : command.getBody()));
        return new OpenApiDebugResponse(command.getRequestId(), command.getApiCode(), gatewayResponse.getHttpStatus(),
                gatewayResponse.isSuccess(), gatewayResponse.getErrorCode(), gatewayResponse.getCostMillis(),
                gatewayResponse.getResponseBody());
    }

    /**
     * 校验开放 API 调试命令。
     *
     * @param command 开放 API 调试命令
     */
    private void validate(OpenApiDebugCommand command) {
        if (command == null) {
            throw businessFailure(ERROR_COMMAND_REQUIRED, "开放 API 调试命令不能为空");
        }
        requireNotBlank(command.getTenantId(), "租户业务编码不能为空");
        requireNotBlank(command.getApiCode(), "开放 API 编码不能为空");
        requireNotBlank(command.getMethod(), "HTTP 请求方法不能为空");
        requireNotBlank(command.getRequestId(), "请求追踪编号不能为空");
        if (command.getPath() == null || !command.getPath().trim().startsWith("/openapi/")) {
            throw businessFailure(ERROR_PATH_INVALID, "开放 API 调试路径必须以 /openapi/ 开头");
        }
        String authMode = normalizeAuthMode(command.getAuthMode());
        if ("API_KEY".equals(authMode)) {
            requireNotBlank(command.getAccessKey(), "API Key Access Key 不能为空");
            requireNotBlank(command.getTimestamp(), "API Key 签名时间戳不能为空");
            requireNotBlank(command.getNonce(), "API Key 签名随机串不能为空");
            requireNotBlank(command.getSignature(), "API Key 签名值不能为空");
            return;
        }
        if ("OAUTH2".equals(authMode)) {
            requireNotBlank(command.getBearerToken(), "OAuth2/OIDC Access Token 不能为空");
            return;
        }
        throw businessFailure(ERROR_AUTH_MODE_INVALID, "开放 API 调试认证方式仅支持 API_KEY 或 OAUTH2");
    }

    /**
     * 构建转发到开放 API 网关的请求头。
     *
     * @param command 开放 API 调试命令
     * @return 网关请求头
     */
    private Map<String, String> buildGatewayHeaders(OpenApiDebugCommand command) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-ZHYC-Tenant-Id", command.getTenantId().trim());
        headers.put("X-ZHYC-Request-Id", command.getRequestId().trim());
        if ("OAUTH2".equals(normalizeAuthMode(command.getAuthMode()))) {
            headers.put("Authorization", "Bearer " + command.getBearerToken().trim());
            return headers;
        }
        headers.put("X-ZHYC-Access-Key", command.getAccessKey().trim());
        headers.put("X-ZHYC-Timestamp", command.getTimestamp().trim());
        headers.put("X-ZHYC-Nonce", command.getNonce().trim());
        headers.put("X-ZHYC-Signature", command.getSignature().trim());
        headers.put("X-ZHYC-Body-SHA256", bodySha256Hex(command.getBody()));
        return headers;
    }

    /**
     * 计算调试请求体 SHA-256 摘要。
     *
     * @param body 请求体文本，空值按空字符串计算
     * @return 小写 hex 格式的 SHA-256 摘要
     */
    private String bodySha256Hex(String body) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return toLowerHex(digest.digest((body == null ? "" : body).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    /**
     * 将字节数组转换为小写 hex 字符串。
     *
     * @param bytes 字节数组
     * @return 小写 hex 字符串
     */
    private String toLowerHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        }
        return builder.toString();
    }

    /**
     * 标准化 HTTP 请求方法。
     *
     * @param method HTTP 请求方法
     * @return 大写 HTTP 请求方法
     */
    private String normalizeMethod(String method) {
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        try {
            return OpenApiHttpMethod.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_METHOD_UNSUPPORTED, "开放 API 调试 HTTP 方法不受支持");
        }
    }

    /**
     * 标准化调试认证方式。
     *
     * @param authMode 调试认证方式
     * @return 大写认证方式，空值返回空字符串
     */
    private String normalizeAuthMode(String authMode) {
        return authMode == null ? "" : authMode.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 校验必填字符串。
     *
     * @param value 待校验字符串
     * @param message 业务错误提示
     */
    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw businessFailure(ERROR_ARGUMENT_REQUIRED, message);
        }
    }

    /**
     * 构建业务异常。
     *
     * @param code 稳定错误码
     * @param message 用户可读错误提示
     * @return 业务异常
     */
    private BusinessException businessFailure(String code, String message) {
        return new BusinessException(code, message);
    }
}
