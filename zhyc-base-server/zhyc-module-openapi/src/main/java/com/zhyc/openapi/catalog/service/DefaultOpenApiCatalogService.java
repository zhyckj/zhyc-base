/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.catalog.domain.OpenApiCatalog;
import com.zhyc.openapi.catalog.domain.OpenApiCatalogStatus;
import com.zhyc.openapi.catalog.repository.OpenApiCatalogRepository;
import com.zhyc.openapi.support.OpenApiHttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认开放 API 目录业务服务实现。
 */
@Service
public class DefaultOpenApiCatalogService implements OpenApiCatalogService {

    /** API 目录保存命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_CATALOG_COMMAND_REQUIRED";
    /** API 业务编码为空错误码。 */
    private static final String ERROR_API_CODE_REQUIRED = "ZHYC_OPENAPI_CATALOG_API_CODE_REQUIRED";
    /** API 业务编码格式非法错误码。 */
    private static final String ERROR_API_CODE_INVALID = "ZHYC_OPENAPI_CATALOG_API_CODE_INVALID";
    /** API 名称为空错误码。 */
    private static final String ERROR_API_NAME_REQUIRED = "ZHYC_OPENAPI_CATALOG_API_NAME_REQUIRED";
    /** API 分组编码为空错误码。 */
    private static final String ERROR_GROUP_CODE_REQUIRED = "ZHYC_OPENAPI_CATALOG_GROUP_CODE_REQUIRED";
    /** API 分组编码格式非法错误码。 */
    private static final String ERROR_GROUP_CODE_INVALID = "ZHYC_OPENAPI_CATALOG_GROUP_CODE_INVALID";
    /** 请求路径匹配规则为空错误码。 */
    private static final String ERROR_PATH_PATTERN_REQUIRED = "ZHYC_OPENAPI_CATALOG_PATH_PATTERN_REQUIRED";
    /** 请求路径匹配规则不是根相对路径错误码。 */
    private static final String ERROR_PATH_PATTERN_NOT_ROOT_RELATIVE =
            "ZHYC_OPENAPI_CATALOG_PATH_PATTERN_NOT_ROOT_RELATIVE";
    /** 请求路径匹配规则格式非法错误码。 */
    private static final String ERROR_PATH_PATTERN_INVALID = "ZHYC_OPENAPI_CATALOG_PATH_PATTERN_INVALID";
    /** API 目录状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_CATALOG_STATUS_REQUIRED";
    /** API 目录状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_CATALOG_STATUS_UNSUPPORTED";
    /** HTTP 方法为空错误码。 */
    private static final String ERROR_HTTP_METHOD_REQUIRED = "ZHYC_OPENAPI_CATALOG_HTTP_METHOD_REQUIRED";
    /** HTTP 方法不受支持错误码。 */
    private static final String ERROR_HTTP_METHOD_UNSUPPORTED = "ZHYC_OPENAPI_CATALOG_HTTP_METHOD_UNSUPPORTED";

    /** API 目录仓储。 */
    private final OpenApiCatalogRepository catalogRepository;

    /**
     * 创建默认开放 API 目录业务服务。
     *
     * @param catalogRepository API 目录仓储
     */
    public DefaultOpenApiCatalogService(OpenApiCatalogRepository catalogRepository) {
        this.catalogRepository = Objects.requireNonNull(catalogRepository, "API 目录仓储不能为空");
    }

    @Override
    public List<OpenApiCatalogResponse> listCatalogs(String groupCode) {
        String requiredGroupCode = requireValidGroupCode(groupCode);
        return catalogRepository.findByGroupCode(requiredGroupCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiCatalogSaveCommand command) {
        OpenApiCatalogSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "API 目录保存命令不能为空");
        OpenApiCatalog catalog = new OpenApiCatalog(null,
                requireValidApiCode(requiredCommand.getApiCode()),
                requireText(requiredCommand.getApiName(), ERROR_API_NAME_REQUIRED, "API 名称不能为空"),
                requireValidGroupCode(requiredCommand.getGroupCode()),
                requireSupportedHttpMethod(requiredCommand.getHttpMethod()),
                requireValidPathPattern(requiredCommand.getPathPattern()),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        catalogRepository.save(catalog);
    }

    private OpenApiCatalogResponse toResponse(OpenApiCatalog catalog) {
        return new OpenApiCatalogResponse(catalog.getApiCode(), catalog.getApiName(), catalog.getGroupCode(),
                catalog.getHttpMethod(), catalog.getPathPattern(), catalog.getStatus());
    }

    /**
     * 校验并规范化 API 业务编码。
     *
     * <p>API 业务编码用于开放 API 目录、版本、授权和调用审计的关联匹配，禁止包含空白字符。</p>
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
     * 校验并规范化 API 分组编码。
     *
     * <p>分组编码用于开放 API 目录查询和后台管理筛选，禁止包含空白字符，避免同一分组出现多种编码形态。</p>
     *
     * @param groupCode API 分组编码
     * @return 规范化后的 API 分组编码
     */
    private String requireValidGroupCode(String groupCode) {
        String normalized = requireText(groupCode, ERROR_GROUP_CODE_REQUIRED, "API 分组编码不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_GROUP_CODE_INVALID, "API 分组编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化请求路径匹配规则。
     *
     * <p>请求路径匹配规则用于开放 API 网关路由匹配，必须是以根斜杠开头且不含空白字符的根相对路径。</p>
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
     * 校验并规范化 API 目录状态。
     *
     * @param status API 目录状态
     * @return 规范化后的 API 目录状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "API 目录状态不能为空");
        try {
            return OpenApiCatalogStatus.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_STATUS_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验并规范化 API 目录 HTTP 方法。
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
