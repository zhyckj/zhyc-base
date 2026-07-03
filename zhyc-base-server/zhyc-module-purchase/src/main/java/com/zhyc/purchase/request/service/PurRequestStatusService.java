/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

/**
 * 采购申请状态查询服务。
 */
public interface PurRequestStatusService {

  /**
   * 按租户和申请单号查询采购申请状态。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @return 采购申请状态响应
   */
  PurRequestStatusResponse queryStatus(String tenantId, String requestNo);
}
