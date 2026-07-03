/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.mapper;

import com.zhyc.purchase.request.domain.PurRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 采购申请 MyBatis Mapper。
 */
@Mapper
public interface PurRequestMapper {

  /**
   * 按租户和申请单号查询采购申请。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @return 采购申请，未找到时返回 {@code null}
   */
  @SelectProvider(type = PurRequestSqlProvider.class, method = "selectByTenantIdAndRequestNo")
  PurRequest selectByTenantIdAndRequestNo(@Param("tenantId") String tenantId,
                                          @Param("requestNo") String requestNo);

  /**
   * 写入采购申请草稿。
   *
   * @param purRequest 采购申请领域对象
   */
  @InsertProvider(type = PurRequestSqlProvider.class, method = "insert")
  void insert(PurRequest purRequest);

  /**
   * 更新采购申请提交审批信息。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param processInstanceId 工作流流程实例 ID
   * @param processStatus 提交后的流程状态
   * @param submittedAt 提交审批时间
   */
  @UpdateProvider(type = PurRequestSqlProvider.class, method = "updateSubmitted")
  void updateSubmitted(@Param("tenantId") String tenantId, @Param("requestNo") String requestNo,
                       @Param("processInstanceId") String processInstanceId,
                       @Param("processStatus") String processStatus,
                       @Param("submittedAt") LocalDateTime submittedAt);

  /**
   * 按租户和流程状态统计采购申请数量。
   *
   * @param tenantId 租户业务编码
   * @param processStatus 流程状态
   * @return 采购申请数量
   */
  @SelectProvider(type = PurRequestSqlProvider.class, method = "countByTenantIdAndProcessStatus")
  long countByTenantIdAndProcessStatus(@Param("tenantId") String tenantId,
                                       @Param("processStatus") String processStatus);

  /**
   * 按租户和流程状态分页查询采购申请。
   *
   * @param tenantId 租户业务编码
   * @param processStatus 流程状态
   * @param offset 起始偏移量
   * @param pageSize 每页记录数
   * @return 采购申请列表
   */
  @SelectProvider(type = PurRequestSqlProvider.class, method = "selectPageByTenantIdAndProcessStatus")
  List<PurRequest> selectPageByTenantIdAndProcessStatus(@Param("tenantId") String tenantId,
                                                        @Param("processStatus") String processStatus,
                                                        @Param("offset") long offset,
                                                        @Param("pageSize") int pageSize);

  /**
   * 更新采购申请流程状态。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param processStatus 流程状态
   * @param updatedAt 更新时间
   */
  @UpdateProvider(type = PurRequestSqlProvider.class, method = "updateProcessStatus")
  void updateProcessStatus(@Param("tenantId") String tenantId, @Param("requestNo") String requestNo,
                           @Param("processStatus") String processStatus,
                           @Param("updatedAt") LocalDateTime updatedAt);
}
