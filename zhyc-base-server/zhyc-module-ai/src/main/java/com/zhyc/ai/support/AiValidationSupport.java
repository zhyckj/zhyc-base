/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.support;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretReference;
import com.zhyc.common.util.TextHelper;

/**
 * AI 能力中心基础校验工具。
 *
 * <p>集中处理租户、编码、密钥引用等首期公共校验，避免各服务重复实现。</p>
 */
public final class AiValidationSupport {

    private AiValidationSupport() {
    }

    /**
     * 校验并规范化租户业务编码。
     *
     * @param tenantId 租户业务编码
     * @param code 业务错误码
     * @return 去除首尾空白后的租户业务编码
     */
    public static String requireTenantId(String tenantId, String code) {
        String normalized = requireText(tenantId, code, "租户业务编码不能为空");
        if (TextHelper.containsWhitespace(normalized)) {
            throw new BusinessException(code.replace("REQUIRED", "INVALID"), "租户业务编码不能包含空白字符");
        }
        return normalized;
    }

    /**
     * 校验必填文本不能为空。
     *
     * @param value 待校验文本
     * @param code 业务错误码
     * @param message 中文错误消息
     * @return 去除首尾空白后的文本
     */
    public static String requireText(String value, String code, String message) {
        String normalized = TextHelper.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(code, message);
        }
        return normalized;
    }

    /**
     * 校验对象不能为空。
     *
     * @param value 待校验对象
     * @param code 业务错误码
     * @param message 中文错误消息
     * @param <T> 对象类型
     * @return 非空对象
     */
    public static <T> T requireObject(T value, String code, String message) {
        if (value == null) {
            throw new BusinessException(code, message);
        }
        return value;
    }

    /**
     * 校验密钥中心引用。
     *
     * @param value 密钥引用
     * @param code 业务错误码
     * @return 规范化后的密钥引用
     */
    public static String requireSecretRef(String value, String code) {
        String normalized = requireText(value, code, "模型供应商密钥不能为空");
        try {
            return SecretReference.parse(normalized).getValue();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(code, "模型供应商密钥必须使用 secret:<secretCode> 引用");
        }
    }

    /**
     * 裁剪文本并把空白文本转为空值。
     *
     * @param value 原始文本
     * @return 裁剪后的文本；空白或 null 返回 null
     */
    public static String trimToNull(String value) {
        return TextHelper.trimToNull(value);
    }
}
