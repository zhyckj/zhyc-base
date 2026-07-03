/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.api;

/**
 * 通用 API 响应包装对象。
 *
 * @param <T> 响应数据类型
 */
public class ApiResult<T> {

    /** 请求是否处理成功。 */
    private boolean success;
    /** 业务响应码，成功时默认为 {@code 0}。 */
    private String code;
    /** 面向调用方的响应消息。 */
    private String message;
    /** 响应数据主体，失败时通常为空。 */
    private T data;
    /** 响应创建时间戳，单位为毫秒。 */
    private long timestamp;

    /**
     * 创建空响应对象，供序列化框架或手动赋值场景使用。
     */
    public ApiResult() {
    }

    /**
     * 创建完整响应对象。
     *
     * @param success 请求是否处理成功
     * @param code 业务响应码
     * @param message 响应消息
     * @param data 响应数据主体
     * @param timestamp 响应创建时间戳，单位为毫秒
     */
    public ApiResult(boolean success, String code, String message, T data, long timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * 创建成功响应。
     *
     * @param data 响应数据主体
     * @param <T> 响应数据类型
     * @return 成功响应对象，响应码为 {@code 0}
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, "0", "success", data, System.currentTimeMillis());
    }

    /**
     * 创建失败响应。
     *
     * @param code 业务错误码
     * @param message 失败原因说明
     * @param <T> 响应数据类型
     * @return 失败响应对象，数据主体为空
     */
    public static <T> ApiResult<T> fail(String code, String message) {
        return new ApiResult<>(false, code, message, null, System.currentTimeMillis());
    }

    /**
     * 返回请求是否处理成功。
     *
     * @return 成功返回 {@code true}，失败返回 {@code false}
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置请求处理结果。
     *
     * @param success 成功标记
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 返回业务响应码。
     *
     * @return 业务响应码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置业务响应码。
     *
     * @param code 业务响应码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 返回响应消息。
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息。
     *
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 返回响应数据主体。
     *
     * @return 响应数据主体
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据主体。
     *
     * @param data 响应数据主体
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 返回响应创建时间戳。
     *
     * @return 毫秒时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置响应创建时间戳。
     *
     * @param timestamp 毫秒时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
