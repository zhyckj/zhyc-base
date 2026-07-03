/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.repository;

import com.zhyc.file.storage.domain.FileStorageConfig;
import java.util.List;

/**
 * 文件存储配置仓储接口。
 */
public interface FileStorageConfigRepository {

  /**
   * 查询租户文件存储配置。
   *
   * @param tenantId 租户业务编码
   * @return 文件存储配置列表
   */
  List<FileStorageConfig> findByTenantId(String tenantId);

  /**
   * 保存或更新文件存储配置。
   *
   * @param config 文件存储配置
   */
  void save(FileStorageConfig config);
}
