/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.domain.OpenApiAppAuthMode;
import com.zhyc.openapi.app.domain.OpenApiAppStatus;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.support.JsonDocumentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认开发者应用业务服务实现。
 */
@Service
public class DefaultOpenApiAppService implements OpenApiAppService {

    /** 开发者应用保存命令为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_OPENAPI_APP_COMMAND_REQUIRED";
    /** 租户业务编码为空错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_OPENAPI_APP_TENANT_ID_REQUIRED";
    /** 租户业务编码格式非法错误码。 */
    private static final String ERROR_TENANT_ID_INVALID = "ZHYC_OPENAPI_APP_TENANT_ID_INVALID";
    /** 应用编码为空错误码。 */
    private static final String ERROR_APP_CODE_REQUIRED = "ZHYC_OPENAPI_APP_CODE_REQUIRED";
    /** 应用编码格式非法错误码。 */
    private static final String ERROR_APP_CODE_INVALID = "ZHYC_OPENAPI_APP_CODE_INVALID";
    /** 应用名称为空错误码。 */
    private static final String ERROR_APP_NAME_REQUIRED = "ZHYC_OPENAPI_APP_NAME_REQUIRED";
    /** 应用负责人为空错误码。 */
    private static final String ERROR_OWNER_USER_REQUIRED = "ZHYC_OPENAPI_APP_OWNER_USER_REQUIRED";
    /** 鉴权方式为空错误码。 */
    private static final String ERROR_AUTH_MODE_REQUIRED = "ZHYC_OPENAPI_APP_AUTH_MODE_REQUIRED";
    /** 鉴权方式不受支持错误码。 */
    private static final String ERROR_AUTH_MODE_UNSUPPORTED = "ZHYC_OPENAPI_APP_AUTH_MODE_UNSUPPORTED";
    /** 应用状态为空错误码。 */
    private static final String ERROR_STATUS_REQUIRED = "ZHYC_OPENAPI_APP_STATUS_REQUIRED";
    /** 应用状态不受支持错误码。 */
    private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_OPENAPI_APP_STATUS_UNSUPPORTED";
    /** IP 白名单不是 JSON 数组错误码。 */
    private static final String ERROR_IP_WHITELIST_NOT_ARRAY = "ZHYC_OPENAPI_APP_IP_WHITELIST_NOT_ARRAY";
    /** IP 白名单 JSON 不合法错误码。 */
    private static final String ERROR_IP_WHITELIST_INVALID_JSON = "ZHYC_OPENAPI_APP_IP_WHITELIST_INVALID_JSON";
    /** IP 白名单不是字符串数组错误码。 */
    private static final String ERROR_IP_WHITELIST_NOT_STRING_ARRAY =
            "ZHYC_OPENAPI_APP_IP_WHITELIST_NOT_STRING_ARRAY";
    /** IP 白名单包含空白项错误码。 */
    private static final String ERROR_IP_WHITELIST_BLANK_ITEM = "ZHYC_OPENAPI_APP_IP_WHITELIST_BLANK_ITEM";
    /** IP 白名单项不受支持错误码。 */
    private static final String ERROR_IP_WHITELIST_ITEM_UNSUPPORTED =
            "ZHYC_OPENAPI_APP_IP_WHITELIST_ITEM_UNSUPPORTED";

    /** 开发者应用仓储。 */
    private final OpenApiAppRepository appRepository;

    /**
     * 创建默认开发者应用业务服务。
     *
     * @param appRepository 开发者应用仓储
     */
    public DefaultOpenApiAppService(OpenApiAppRepository appRepository) {
        this.appRepository = Objects.requireNonNull(appRepository, "开发者应用仓储不能为空");
    }

    @Override
    public List<OpenApiAppResponse> listApps(String tenantId) {
        String requiredTenantId = requireValidTenantId(tenantId);
        return appRepository.findByTenantId(requiredTenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void save(OpenApiAppSaveCommand command) {
        OpenApiAppSaveCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "开发者应用保存命令不能为空");
        OpenApiApp app = new OpenApiApp(null, requireValidTenantId(requiredCommand.getTenantId()),
                requireValidAppCode(requiredCommand.getAppCode()),
                requireText(requiredCommand.getAppName(), ERROR_APP_NAME_REQUIRED, "应用名称不能为空"),
                requireObject(requiredCommand.getOwnerUserId(), ERROR_OWNER_USER_REQUIRED, "应用负责人不能为空"),
                requireSupportedAuthMode(requiredCommand.getAuthMode()),
                normalizeIpWhitelist(requiredCommand.getIpWhitelist()),
                requireSupportedStatus(requiredCommand.getStatus()), null, null);
        appRepository.save(app);
    }

    private OpenApiAppResponse toResponse(OpenApiApp app) {
        return new OpenApiAppResponse(app.getAppCode(), app.getAppName(), app.getOwnerUserId(),
                app.getAuthMode(), app.getIpWhitelist(), app.getStatus());
    }

    /**
     * 校验并规范化租户业务编码。
     *
     * <p>租户业务编码是共享表模式下的隔离键，禁止包含空白字符，避免同一租户出现不可见的编码差异。</p>
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
     * 校验并规范化开放 API 应用编码。
     *
     * @param appCode 开放 API 应用编码
     * @return 规范化后的开放 API 应用编码
     */
    private String requireValidAppCode(String appCode) {
        String normalized = requireText(appCode, ERROR_APP_CODE_REQUIRED, "应用编码不能为空");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_APP_CODE_INVALID, "应用编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验并规范化开放 API 应用鉴权方式。
     *
     * @param authMode 鉴权方式
     * @return 规范化后的鉴权方式
     */
    private String requireSupportedAuthMode(String authMode) {
        String normalized = requireText(authMode, ERROR_AUTH_MODE_REQUIRED, "鉴权方式不能为空");
        try {
            return OpenApiAppAuthMode.fromCode(normalized).getCode();
        } catch (IllegalArgumentException ex) {
            throw businessFailure(ERROR_AUTH_MODE_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 规范化开放 API 应用 IP 白名单。
     *
     * <p>IP 白名单供网关进行来源限制，允许为空；非空时必须是合法 JSON 字符串数组，避免运行时白名单解析失败。</p>
     *
     * @param ipWhitelist IP 白名单 JSON
     * @return 规范化后的 IP 白名单 JSON；空白值返回 null
     */
    private String normalizeIpWhitelist(String ipWhitelist) {
        String normalized = trimToNull(ipWhitelist);
        if (normalized == null) {
            return null;
        }
        if (!normalized.startsWith("[") || !normalized.endsWith("]")) {
            throw businessFailure(ERROR_IP_WHITELIST_NOT_ARRAY, "IP 白名单必须是 JSON 数组");
        }
        if (!JsonDocumentValidator.isJsonArray(normalized)) {
            throw businessFailure(ERROR_IP_WHITELIST_INVALID_JSON, "IP 白名单必须是合法 JSON 数组");
        }
        if (!JsonDocumentValidator.isJsonStringArray(normalized)) {
            throw businessFailure(ERROR_IP_WHITELIST_NOT_STRING_ARRAY, "IP 白名单必须是 JSON 字符串数组");
        }
        if (!JsonDocumentValidator.isNonBlankJsonStringArray(normalized)) {
            throw businessFailure(ERROR_IP_WHITELIST_BLANK_ITEM, "IP 白名单不能包含空白项");
        }
        requireSupportedIpWhitelistItems(JsonDocumentValidator.readJsonStringArray(normalized));
        return normalized;
    }

    /**
     * 校验 IP 白名单项是否符合首期网关支持范围。
     *
     * <p>首期仅支持 IPv4 或 IPv4 CIDR，后续如需支持 IPv6 可在网关运行态匹配器一起扩展。</p>
     *
     * @param items IP 白名单项
     */
    private void requireSupportedIpWhitelistItems(List<String> items) {
        for (String item : items) {
            if (!isSupportedIpWhitelistItem(item.trim())) {
                throw businessFailure(ERROR_IP_WHITELIST_ITEM_UNSUPPORTED, "IP 白名单只支持 IPv4 或 IPv4 CIDR");
            }
        }
    }

    /**
     * 判断单个白名单项是否为 IPv4 或 IPv4 CIDR。
     *
     * @param item 白名单项
     * @return 符合首期格式返回 {@code true}
     */
    private boolean isSupportedIpWhitelistItem(String item) {
        String[] parts = item.split("/", -1);
        if (parts.length == 1) {
            return isIpv4Address(parts[0]);
        }
        if (parts.length != 2 || !isIpv4Address(parts[0]) || parts[1].isEmpty()) {
            return false;
        }
        return isIntegerInRange(parts[1], 0, 32);
    }

    /**
     * 判断文本是否为 IPv4 地址。
     *
     * @param value 待判断文本
     * @return 合法 IPv4 地址返回 {@code true}
     */
    private boolean isIpv4Address(String value) {
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (!isIntegerInRange(octet, 0, 255)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断文本是否为指定范围内的十进制整数。
     *
     * @param value 待判断文本
     * @param min 最小值
     * @param max 最大值
     * @return 范围内整数返回 {@code true}
     */
    private boolean isIntegerInRange(String value, int min, int max) {
        if (value.isEmpty() || value.chars().anyMatch(character -> !Character.isDigit(character))) {
            return false;
        }
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return false;
        }
        return number >= min && number <= max;
    }

    /**
     * 校验并规范化开放 API 应用状态。
     *
     * @param status 应用状态
     * @return 规范化后的应用状态
     */
    private String requireSupportedStatus(String status) {
        String normalized = requireText(status, ERROR_STATUS_REQUIRED, "应用状态不能为空");
        try {
            return OpenApiAppStatus.fromCode(normalized).getCode();
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
