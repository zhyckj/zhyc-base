/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.version.domain.OpenApiVersion;
import com.zhyc.openapi.version.domain.OpenApiVersionStatus;
import com.zhyc.openapi.version.repository.OpenApiVersionRepository;
import com.zhyc.openapi.support.JsonDocumentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 默认开放 API 版本发布业务服务实现。
 */
@Service
public class DefaultOpenApiVersionService implements OpenApiVersionService {

    /** API 版本发布命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_VERSION_COMMAND_REQUIRED";
    /** API 业务编码为空错误码。 */
    private static final String ERROR_API_CODE_REQUIRED = "ZHYC_OPENAPI_VERSION_API_CODE_REQUIRED";
    /** API 业务编码格式非法错误码。 */
    private static final String ERROR_API_CODE_INVALID = "ZHYC_OPENAPI_VERSION_API_CODE_INVALID";
    /** API 版本号为空错误码。 */
    private static final String ERROR_VERSION_REQUIRED = "ZHYC_OPENAPI_VERSION_VERSION_REQUIRED";
    /** API 版本号格式非法错误码。 */
    private static final String ERROR_VERSION_INVALID = "ZHYC_OPENAPI_VERSION_VERSION_INVALID";
    /** 后端转发路由为空错误码。 */
    private static final String ERROR_BACKEND_ROUTE_REQUIRED = "ZHYC_OPENAPI_VERSION_BACKEND_ROUTE_REQUIRED";
    /** 后端转发路由格式非法错误码。 */
    private static final String ERROR_BACKEND_ROUTE_INVALID = "ZHYC_OPENAPI_VERSION_BACKEND_ROUTE_INVALID";
    /** 后端转发路由协议不受支持错误码。 */
    private static final String ERROR_BACKEND_ROUTE_SCHEME_UNSUPPORTED =
            "ZHYC_OPENAPI_VERSION_BACKEND_ROUTE_SCHEME_UNSUPPORTED";
    /** 请求 JSON Schema 顶层不是对象错误码。 */
    private static final String ERROR_REQUEST_SCHEMA_NOT_OBJECT = "ZHYC_OPENAPI_VERSION_REQUEST_SCHEMA_NOT_OBJECT";
    /** 请求 JSON Schema 非法错误码。 */
    private static final String ERROR_REQUEST_SCHEMA_INVALID_JSON =
            "ZHYC_OPENAPI_VERSION_REQUEST_SCHEMA_INVALID_JSON";
    /** 响应 JSON Schema 顶层不是对象错误码。 */
    private static final String ERROR_RESPONSE_SCHEMA_NOT_OBJECT = "ZHYC_OPENAPI_VERSION_RESPONSE_SCHEMA_NOT_OBJECT";
    /** 响应 JSON Schema 非法错误码。 */
    private static final String ERROR_RESPONSE_SCHEMA_INVALID_JSON =
            "ZHYC_OPENAPI_VERSION_RESPONSE_SCHEMA_INVALID_JSON";
    /** API 版本状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_VERSION_STATUS_REQUIRED";
    /** API 版本状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_VERSION_STATUS_UNSUPPORTED";

    /** API 版本发布仓储。 */
    private final OpenApiVersionRepository versionRepository;

    /**
     * 创建默认开放 API 版本发布业务服务。
     *
     * @param versionRepository API 版本发布仓储
     */
    public DefaultOpenApiVersionService(OpenApiVersionRepository versionRepository) {
        this.versionRepository = Objects.requireNonNull(versionRepository, "API 版本发布仓储不能为空");
    }

    @Override
    public List<OpenApiVersionResponse> listVersions(String apiCode) {
        String requiredApiCode = requireValidApiCode(apiCode);
        return versionRepository.findByApiCode(requiredApiCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void publish(OpenApiVersionPublishCommand command) {
        OpenApiVersionPublishCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "API 版本发布命令不能为空");
        OpenApiVersion version = new OpenApiVersion(null,
                requireValidApiCode(requiredCommand.getApiCode()),
                requireValidVersion(requiredCommand.getVersion()),
                requireSupportedBackendRoute(requiredCommand.getBackendRoute()),
                normalizeSchema(requiredCommand.getRequestSchema(), "请求 JSON Schema",
                        ERROR_REQUEST_SCHEMA_NOT_OBJECT, ERROR_REQUEST_SCHEMA_INVALID_JSON),
                normalizeSchema(requiredCommand.getResponseSchema(), "响应 JSON Schema",
                        ERROR_RESPONSE_SCHEMA_NOT_OBJECT, ERROR_RESPONSE_SCHEMA_INVALID_JSON),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        versionRepository.save(version);
    }

    private OpenApiVersionResponse toResponse(OpenApiVersion version) {
        return new OpenApiVersionResponse(version.getVersion(), version.getBackendRoute(),
                version.getRequestSchema(), version.getResponseSchema(), version.getStatus());
    }

    /**
     * 校验并规范化 JSON Schema 文本。
     *
     * <p>Schema 会写入 MySQL JSON 字段并用于开发者门户展示，必须保持合法 JSON；空值按空对象处理。</p>
     *
     * @param schema JSON Schema 文本
     * @param schemaName Schema 中文名称
     * @param notObjectCode 顶层不是对象错误码
     * @param invalidJsonCode 非法 JSON 错误码
     * @return 规范化后的 JSON Schema 文本
     */
    private String normalizeSchema(String schema, String schemaName, String notObjectCode, String invalidJsonCode) {
        String normalized = trimToNull(schema);
        if (normalized == null) {
            return "{}";
        }
        if (!normalized.startsWith("{")) {
            throw businessFailure(notObjectCode, schemaName + " 必须是 JSON 对象");
        }
        if (!isJsonDocument(normalized)) {
            throw businessFailure(invalidJsonCode, schemaName + " 必须是合法 JSON");
        }
        return normalized;
    }

    /**
     * 判断文本是否符合首期 JSON 文档形态。
     *
     * <p>当前模块不引入额外 JSON 依赖，因此使用轻量解析器校验对象、数组、字符串、数字和字面量语法。</p>
     *
     * @param value JSON 文本
     * @return 符合 JSON 文档基本形态返回 {@code true}
     */
    private boolean isJsonDocument(String value) {
        return JsonDocumentValidator.isJsonDocument(value);
    }

    /**
     * 校验并规范化 API 业务编码。
     *
     * <p>API 业务编码用于版本记录、目录定义和网关路由关联，禁止包含空白字符，避免发布版本无法稳定匹配 API。</p>
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
     * 校验并规范化 API 版本号。
     *
     * <p>版本号用于发布版本排序和网关运行态路由匹配，禁止包含空白字符，避免同一版本出现不可见差异。</p>
     *
     * @param version API 版本号
     * @return 规范化后的 API 版本号
     */
    private String requireValidVersion(String version) {
        String normalized = requireText(version, ERROR_VERSION_REQUIRED, "API 版本号不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_VERSION_INVALID, "API 版本号不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化后端转发路由。
     *
     * <p>后端转发路由由开放 API 网关直接消费，首期仅允许 HTTP(S) 目标，避免错误协议进入运行态路由。</p>
     *
     * @param backendRoute 后端转发路由
     * @return 规范化后的后端转发路由
     */
    private String requireSupportedBackendRoute(String backendRoute) {
        String normalized = requireText(backendRoute, ERROR_BACKEND_ROUTE_REQUIRED, "后端转发路由不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_BACKEND_ROUTE_INVALID, "后端转发路由不能包含空白字符");
        }
        String lowerCaseRoute = normalized.toLowerCase(Locale.ROOT);
        if (!lowerCaseRoute.startsWith("http://") && !lowerCaseRoute.startsWith("https://")) {
            throw businessFailure(ERROR_BACKEND_ROUTE_SCHEME_UNSUPPORTED, "后端转发路由只支持 http:// 或 https://");
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
     * 校验并规范化 API 版本状态。
     *
     * @param status API 版本状态
     * @return 规范化后的 API 版本状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "API 版本状态不能为空");
        try {
            return OpenApiVersionStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
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
