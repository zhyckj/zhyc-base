/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.repository;

import com.zhyc.purchase.request.domain.PurRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 采购申请仓储。
 */
public interface PurRequestRepository {

  /**
   * 按租户和采购申请单号查询采购申请。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @return 采购申请
   */
  Optional<PurRequest> findByTenantIdAndRequestNo(String tenantId, String requestNo);

  /**
   * 保存采购申请草稿。
   *
   * @param purRequest 采购申请领域对象
   */
  void save(PurRequest purRequest);

  /**
   * 更新采购申请提交审批信息。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param processInstanceId 工作流流程实例 ID
   * @param processStatus 提交后的流程状态
   * @param submittedAt 提交审批时间
   */
  void updateSubmitted(String tenantId, String requestNo, String processInstanceId,
                       String processStatus, LocalDateTime submittedAt);

  /**
   * 按租户和流程状态统计采购申请数量。
   *
   * @param tenantId 租户业务编码
   * @param processStatus 流程状态，空表示全部状态
   * @return 采购申请数量
   */
  long countByTenantIdAndProcessStatus(String tenantId, String processStatus);

  /**
   * 按租户和流程状态分页查询采购申请。
   *
   * @param tenantId 租户业务编码
   * @param processStatus 流程状态，空表示全部状态
   * @param offset 起始偏移量
   * @param pageSize 每页记录数
   * @return 采购申请列表
   */
  List<PurRequest> findPageByTenantIdAndProcessStatus(String tenantId,
      String processStatus, long offset, int pageSize);

  /**
   * 更新采购申请流程状态。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param processStatus 流程状态
   * @param updatedAt 更新时间
   */
  void updateProcessStatus(String tenantId, String requestNo, String processStatus,
                           LocalDateTime updatedAt);
}
