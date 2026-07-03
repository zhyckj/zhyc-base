/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 后端服务调用器。
 */
public interface OpenApiBackendInvoker {

  /**
   * 调用已匹配的后端路由。
   *
   * @param route 后端路由
   * @param request 后端转发请求
   * @return 后端转发响应
   */
  OpenApiBackendResponse invoke(OpenApiRoute route, OpenApiBackendRequest request);
}
