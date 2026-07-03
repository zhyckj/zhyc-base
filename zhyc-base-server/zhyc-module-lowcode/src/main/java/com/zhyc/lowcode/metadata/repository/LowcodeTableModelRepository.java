/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

import java.util.List;
import java.util.Optional;

/**
 * 低代码表模型仓储接口。
 */
public interface LowcodeTableModelRepository {

  /**
   * 保存表模型。
   *
   * @param tableModel 表模型
   * @return 保存后的表模型
   */
  LowcodeTableModel save(LowcodeTableModel tableModel);

  /**
   * 按租户和模型编码查找表模型。
   *
   * @param tenantId 租户业务编码
   * @param code 模型编码
   * @return 匹配的表模型，不存在时返回空
   */
  Optional<LowcodeTableModel> findByTenantIdAndCode(String tenantId, String code);

  /**
   * 按租户和表模型主键查找表模型。
   *
   * @param tenantId 租户业务编码
   * @param id 表模型主键
   * @return 匹配的表模型，不存在时返回空
   */
  Optional<LowcodeTableModel> findByTenantIdAndId(String tenantId, Long id);

  /**
   * 按租户查询表模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 租户内表模型列表
   */
  List<LowcodeTableModel> findByTenantId(String tenantId);
}
