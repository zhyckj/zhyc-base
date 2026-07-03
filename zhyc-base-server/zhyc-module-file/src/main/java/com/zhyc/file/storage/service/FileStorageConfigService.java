/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.service;

import java.util.List;

/**
 * 文件存储配置业务服务。
 */
public interface FileStorageConfigService {

  /**
   * 查询租户文件存储配置。
   *
   * @param tenantId 租户业务编码
   * @return 文件存储配置列表
   */
  List<FileStorageConfigResponse> listConfigs(String tenantId);

  /**
   * 保存或更新文件存储配置。
   *
   * @param command 文件存储配置保存命令
   */
  void save(FileStorageConfigSaveCommand command);
}
