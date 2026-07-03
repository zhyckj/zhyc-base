/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.repository;

import com.zhyc.file.storage.domain.FileStorageConfig;
import com.zhyc.file.storage.mapper.FileStorageConfigMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的文件存储配置仓储实现。
 */
@Repository
public class MyBatisFileStorageConfigRepository implements FileStorageConfigRepository {

  /** 文件存储配置 Mapper。 */
  private final FileStorageConfigMapper configMapper;

  /**
   * 创建文件存储配置仓储实现。
   *
   * @param configMapper 文件存储配置 Mapper
   */
  public MyBatisFileStorageConfigRepository(FileStorageConfigMapper configMapper) {
    this.configMapper = Objects.requireNonNull(configMapper, "文件存储配置 Mapper 不能为空");
  }

  @Override
  public List<FileStorageConfig> findByTenantId(String tenantId) {
    return configMapper.selectByTenantId(tenantId);
  }

  @Override
  public void save(FileStorageConfig config) {
    configMapper.upsert(config);
  }
}
