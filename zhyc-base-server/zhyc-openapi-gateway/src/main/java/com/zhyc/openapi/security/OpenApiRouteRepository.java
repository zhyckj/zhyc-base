/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Optional;

/**
 * 开放 API 运行态路由仓储。
 */
public interface OpenApiRouteRepository {

  /**
   * 按 HTTP 方法和请求路径查找后端路由。
   *
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 后端路由
   */
  Optional<OpenApiRoute> findRoute(String httpMethod, String requestPath);
}
