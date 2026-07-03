/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.support;

import com.zhyc.common.exception.BusinessException;

/**
 * 系统模块服务层通用参数校验工具。
 *
 * <p>用于将面向调用方的服务参数错误统一转换为带稳定错误码的业务异常，避免 Web 层暴露 Java 参数异常类型。</p>
 */
public final class SystemServiceValidation {

    /** 系统模块通用参数错误码。 */
    public static final String ERROR_ARGUMENT_INVALID = "ZHYC_SYSTEM_ARGUMENT_INVALID";

    private SystemServiceValidation() {
    }

    /**
     * 校验文本参数不能为空白。
     *
     * @param value 待校验文本
     * @param message 中文错误消息
     * @return 去除首尾空白后的文本
     */
    public static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw businessFailure(message);
        }
        return value.trim();
    }

    /**
     * 校验对象参数不能为空。
     *
     * @param value 待校验对象
     * @param message 中文错误消息
     * @param <T> 对象类型
     * @return 非空对象
     */
    public static <T> T requireObject(T value, String message) {
        if (value == null) {
            throw businessFailure(message);
        }
        return value;
    }

    /**
     * 校验整数参数必须为正数。
     *
     * @param value 待校验整数
     * @param message 中文错误消息
     * @return 正整数
     */
    public static Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw businessFailure(message);
        }
        return value;
    }

    /**
     * 创建系统模块参数业务异常。
     *
     * @param message 中文错误消息
     * @return 业务异常
     */
    public static BusinessException businessFailure(String message) {
        return new BusinessException(ERROR_ARGUMENT_INVALID, message);
    }
}
