/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.repository;

import com.zhyc.lowcode.generator.LowcodeGenerationFile;

import java.util.List;

/**
 * 低代码生成文件明细仓储接口。
 */
public interface LowcodeGenerationFileRepository {

  /**
   * 批量保存生成文件明细。
   *
   * @param files 生成文件明细列表
   */
  void saveAll(List<LowcodeGenerationFile> files);

  /**
   * 按租户和生成记录查询文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @return 生成文件明细列表
   */
  List<LowcodeGenerationFile> findByTenantIdAndRecordId(String tenantId, Long recordId);
}
