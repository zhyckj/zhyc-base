/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.repository;

import com.zhyc.lowcode.generator.LowcodeGenerationRecord;

import java.util.List;

/**
 * 低代码生成记录仓储接口。
 */
public interface LowcodeGenerationRecordRepository {

  /**
   * 保存生成记录。
   *
   * @param record 生成记录
   * @return 保存后的生成记录
   */
  LowcodeGenerationRecord save(LowcodeGenerationRecord record);

  /**
   * 按租户查询生成记录列表。
   *
   * @param tenantId 租户业务编码
   * @return 租户内生成记录列表
   */
  List<LowcodeGenerationRecord> findByTenantId(String tenantId);
}
