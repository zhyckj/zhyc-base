/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.mobile;

/**
 * 移动端认证接口统一响应。
 *
 * @param success 是否成功
 * @param code 稳定业务响应码
 * @param message 用户可读响应消息
 * @param data 响应数据
 * @param <T> 响应数据类型
 */
public record AuthMobileApiResult<T>(boolean success, String code, String message, T data) {

  /**
   * 构建成功响应。
   *
   * @param data 响应数据
   * @param <T> 响应数据类型
   * @return 成功响应
   */
  public static <T> AuthMobileApiResult<T> ok(T data) {
    return new AuthMobileApiResult<>(true, "OK", "处理成功", data);
  }

  /**
   * 构建失败响应。
   *
   * @param code 稳定业务错误码
   * @param message 用户可读错误消息
   * @param <T> 响应数据类型
   * @return 失败响应
   */
  public static <T> AuthMobileApiResult<T> fail(String code, String message) {
    return new AuthMobileApiResult<>(false, code, message, null);
  }
}
