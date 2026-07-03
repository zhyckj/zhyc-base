/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

/**
 * 开放 API 调试网关响应。
 */
public class OpenApiDebugGatewayResponse {

    /** 网关或后端响应状态码。 */
    private final int httpStatus;
    /** 网关调用是否成功。 */
    private final boolean success;
    /** 对外稳定错误码。 */
    private final String errorCode;
    /** 网关调用耗时毫秒数。 */
    private final long costMillis;
    /** 响应体文本。 */
    private final String responseBody;

    /**
     * 创建开放 API 调试网关响应。
     *
     * @param httpStatus 网关或后端响应状态码
     * @param success 网关调用是否成功
     * @param errorCode 对外稳定错误码
     * @param costMillis 网关调用耗时毫秒数
     * @param responseBody 响应体文本
     */
    public OpenApiDebugGatewayResponse(int httpStatus, boolean success, String errorCode, long costMillis,
                                       String responseBody) {
        this.httpStatus = httpStatus;
        this.success = success;
        this.errorCode = errorCode;
        this.costMillis = costMillis;
        this.responseBody = responseBody;
    }

    /**
     * 获取网关或后端响应状态码。
     *
     * @return 网关或后端响应状态码
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 判断网关调用是否成功。
     *
     * @return 网关调用成功时返回 true
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取对外稳定错误码。
     *
     * @return 对外稳定错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取网关调用耗时毫秒数。
     *
     * @return 网关调用耗时毫秒数
     */
    public long getCostMillis() {
        return costMillis;
    }

    /**
     * 获取响应体文本。
     *
     * @return 响应体文本
     */
    public String getResponseBody() {
        return responseBody;
    }
}
