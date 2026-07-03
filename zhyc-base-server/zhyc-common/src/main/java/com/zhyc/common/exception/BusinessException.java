/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.exception;

import com.zhyc.common.util.TextHelper;

/**
 * 面向接口调用方的业务异常。
 *
 * <p>用于承载稳定业务错误码和中文错误消息，由 Web 层统一转换为 API 失败响应。</p>
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码，供前端和开放 API 调用方进行稳定判断。 */
    private final String code;

    /**
     * 创建业务异常。
     *
     * @param code 业务错误码
     * @param message 面向调用方的中文错误消息
     */
    public BusinessException(String code, String message) {
        super(TextHelper.requireText(message, "业务异常消息不能为空"));
        this.code = TextHelper.requireText(code, "业务错误码不能为空");
    }

    /**
     * 返回业务错误码。
     *
     * @return 业务错误码
     */
    public String getCode() {
        return code;
    }
}
