/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.repository;

import com.zhyc.purchase.request.domain.PurRequest;
import com.zhyc.purchase.request.mapper.PurRequestMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的采购申请仓储实现。
 */
@Repository
public class MyBatisPurRequestRepository implements PurRequestRepository {

  /** 采购申请 Mapper。 */
  private final PurRequestMapper purRequestMapper;

  /**
   * 创建 MyBatis 采购申请仓储。
   *
   * @param purRequestMapper 采购申请 Mapper
   */
  public MyBatisPurRequestRepository(PurRequestMapper purRequestMapper) {
    this.purRequestMapper = Objects.requireNonNull(purRequestMapper, "采购申请 Mapper 不能为空");
  }

  @Override
  public Optional<PurRequest> findByTenantIdAndRequestNo(String tenantId, String requestNo) {
    return Optional.ofNullable(purRequestMapper.selectByTenantIdAndRequestNo(tenantId, requestNo));
  }

  @Override
  public void save(PurRequest purRequest) {
    purRequestMapper.insert(purRequest);
  }

  @Override
  public void updateSubmitted(String tenantId, String requestNo, String processInstanceId,
                              String processStatus, LocalDateTime submittedAt) {
    purRequestMapper.updateSubmitted(tenantId, requestNo, processInstanceId, processStatus, submittedAt);
  }

  @Override
  public long countByTenantIdAndProcessStatus(String tenantId, String processStatus) {
    return purRequestMapper.countByTenantIdAndProcessStatus(tenantId, processStatus);
  }

  @Override
  public List<PurRequest> findPageByTenantIdAndProcessStatus(String tenantId,
      String processStatus, long offset, int pageSize) {
    return purRequestMapper.selectPageByTenantIdAndProcessStatus(tenantId, processStatus,
        offset, pageSize);
  }

  @Override
  public void updateProcessStatus(String tenantId, String requestNo, String processStatus,
                                  LocalDateTime updatedAt) {
    purRequestMapper.updateProcessStatus(tenantId, requestNo, processStatus, updatedAt);
  }
}
