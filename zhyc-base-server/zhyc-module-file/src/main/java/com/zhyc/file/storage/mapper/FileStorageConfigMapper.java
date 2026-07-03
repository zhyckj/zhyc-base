/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.mapper;

import com.zhyc.file.storage.domain.FileStorageConfig;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 文件存储配置 MyBatis Mapper。
 */
@Mapper
public interface FileStorageConfigMapper {

  /**
   * 查询租户文件存储配置。
   *
   * @param tenantId 租户业务编码
   * @return 文件存储配置列表
   */
  @SelectProvider(type = FileStorageConfigSqlProvider.class, method = "selectByTenantId")
  List<FileStorageConfig> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 保存或更新文件存储配置。
   *
   * @param config 文件存储配置
   */
  @InsertProvider(type = FileStorageConfigSqlProvider.class, method = "upsert")
  void upsert(FileStorageConfig config);
}
