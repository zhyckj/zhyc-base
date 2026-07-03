/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.mapper;

import com.zhyc.file.preview.domain.FilePreviewLog;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 文件预览 MyBatis Mapper。
 */
@Mapper
public interface FilePreviewMapper {

  /**
   * 新增文件预览日志。
   *
   * @param log 文件预览日志
   */
  @InsertProvider(type = FilePreviewSqlProvider.class, method = "insertLog")
  void insertLog(FilePreviewLog log);

  /**
   * 查询文件预览日志。
   *
   * @param tenantId 租户业务编码
   * @param fileCode 文件业务编码
   * @return 文件预览日志列表
   */
  @SelectProvider(type = FilePreviewSqlProvider.class, method = "selectLogs")
  List<FilePreviewLog> selectLogs(@Param("tenantId") String tenantId, @Param("fileCode") String fileCode);
}
