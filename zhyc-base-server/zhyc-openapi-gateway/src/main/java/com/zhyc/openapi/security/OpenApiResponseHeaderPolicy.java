/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Locale;
import java.util.Set;

/**
 * 开放 API 响应头外显策略。
 *
 * <p>网关只允许下载、缓存和平台追踪类响应头返回给外部调用方，避免后端 Cookie、鉴权头、
 * 内部网关头或服务框架头泄露到开放 API 边界之外。</p>
 */
final class OpenApiResponseHeaderPolicy {

  /** 允许外显给开放 API 调用方的响应头白名单，统一使用小写进行匹配。 */
  private static final Set<String> ALLOWED_RESPONSE_HEADERS = Set.of(
      "x-zhyc-request-id",
      "content-disposition",
      "cache-control",
      "etag",
      "last-modified");

  private OpenApiResponseHeaderPolicy() {
  }

  /**
   * 判断后端响应头是否允许回传给开放 API 调用方。
   *
   * @param headerName 后端响应头名称
   * @return 允许外显返回 {@code true}
   */
  static boolean isAllowedResponseHeader(String headerName) {
    return headerName != null
        && ALLOWED_RESPONSE_HEADERS.contains(headerName.trim().toLowerCase(Locale.ROOT));
  }
}
