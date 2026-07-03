/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.config;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * 认证中心前端登录地址工具。
 *
 * <p>认证中心未登录访问 OAuth2 授权端点时，需要回到后台前端账号密码页。
 * 前端以 {@code authRequest=1} 判断授权请求已经由认证中心保存，缺少该标记会再次发起授权请求并造成循环跳转。</p>
 */
public final class AuthFrontendLoginUris {

  /** 前端识别授权请求已准备完成的查询参数名。 */
  static final String AUTH_REQUEST_QUERY_NAME = "authRequest";

  /** 前端识别授权请求已准备完成的查询参数值。 */
  static final String AUTH_REQUEST_QUERY_VALUE = "1";

  private AuthFrontendLoginUris() {
  }

  /**
   * 标准化前端授权登录页地址。
   *
   * <p>部署人员只配置 {@code /login} 时自动补齐 {@code authRequest=1}，避免浏览器在
   * {@code /oauth2/authorize} 和前端登录页之间循环跳转。</p>
   *
   * @param uri 原始前端登录页地址
   * @return 携带授权请求完成标记的前端登录页地址
   */
  public static String normalizeAuthRequestLoginUri(String uri) {
    return UriComponentsBuilder.fromUriString(requireText(uri, "前端登录页地址不能为空"))
        .replaceQueryParam(AUTH_REQUEST_QUERY_NAME, AUTH_REQUEST_QUERY_VALUE)
        .build(true)
        .toUriString();
  }

  /**
   * 给 URI 追加查询参数。
   *
   * @param uri 原始 URI
   * @param name 查询参数名
   * @param value 查询参数值
   * @return 追加参数后的 URI
   */
  public static String appendQueryParam(String uri, String name, String value) {
    return UriComponentsBuilder.fromUriString(requireText(uri, "URI 不能为空"))
        .queryParam(requireText(name, "查询参数名不能为空"), requireText(value, "查询参数值不能为空"))
        .build(true)
        .toUriString();
  }

  /**
   * 校验配置项必须包含非空文本。
   *
   * @param value 配置值
   * @param message 配置为空时的错误消息
   * @return 去除首尾空白后的配置值
   */
  private static String requireText(String value, String message) {
    if (value == null) {
      throw new IllegalStateException(message);
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalStateException(message);
    }
    return trimmed;
  }
}
