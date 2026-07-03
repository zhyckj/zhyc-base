/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 调用审计记录器。
 */
public interface ApiCallAuditRecorder {

  /**
   * 记录开放 API 调用审计。
   *
   * @param record 开放 API 调用审计记录
   */
  void record(ApiCallAuditRecord record);
}
