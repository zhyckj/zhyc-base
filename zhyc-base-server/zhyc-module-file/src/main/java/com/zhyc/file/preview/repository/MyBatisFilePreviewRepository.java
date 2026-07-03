/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.repository;

import com.zhyc.file.preview.domain.FilePreviewLog;
import com.zhyc.file.preview.mapper.FilePreviewMapper;
import com.zhyc.file.preview.service.FilePreviewLogQuery;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的文件预览仓储实现。
 */
@Repository
public class MyBatisFilePreviewRepository implements FilePreviewRepository {

  /** 文件预览 Mapper。 */
  private final FilePreviewMapper filePreviewMapper;

  /**
   * 创建文件预览仓储。
   *
   * @param filePreviewMapper 文件预览 Mapper
   */
  public MyBatisFilePreviewRepository(FilePreviewMapper filePreviewMapper) {
    this.filePreviewMapper = Objects.requireNonNull(filePreviewMapper, "文件预览 Mapper 不能为空");
  }

  @Override
  public void saveLog(FilePreviewLog log) {
    filePreviewMapper.insertLog(log);
  }

  @Override
  public List<FilePreviewLog> findLogs(FilePreviewLogQuery query) {
    return filePreviewMapper.selectLogs(query.tenantId(), query.fileCode());
  }
}
