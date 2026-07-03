/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import java.util.List;

/**
 * 低代码表关系仓储接口。
 */
public interface LowcodeTableRelationRepository {

  /**
   * 保存表关系。
   *
   * @param relation 表关系模型
   * @return 保存后的表关系模型
   */
  LowcodeTableRelation save(LowcodeTableRelation relation);

  /**
   * 查询租户内表关系。
   *
   * @param tenantId 租户业务编码
   * @return 表关系列表
   */
  List<LowcodeTableRelation> findByTenantId(String tenantId);
}
