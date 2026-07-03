/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

import com.zhyc.common.api.PageResult;

/**
 * 采购申请命令服务。
 */
public interface PurRequestCommandService {

  /**
   * 创建采购申请草稿。
   *
   * @param command 采购申请创建命令
   * @return 采购申请单号
   */
  String create(PurRequestCreateCommand command);

  /**
   * 分页查询采购申请。
   *
   * @param query 采购申请分页查询条件
   * @return 采购申请分页响应
   */
  PageResult<PurRequestStatusResponse> list(PurRequestQuery query);

  /**
   * 提交采购申请审批。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param starterUserId 发起人用户 ID
   * @return 采购申请提交审批响应
   */
  PurRequestSubmitResponse submit(String tenantId, String requestNo, Long starterUserId);
}
