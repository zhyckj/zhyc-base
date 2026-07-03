/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.repository;

import com.zhyc.file.preview.domain.FilePreviewLog;
import com.zhyc.file.preview.service.FilePreviewLogQuery;
import java.util.List;

/**
 * 文件预览仓储接口。
 */
public interface FilePreviewRepository {

  /**
   * 保存文件预览日志。
   *
   * @param log 文件预览日志
   */
  void saveLog(FilePreviewLog log);

  /**
   * 查询文件预览日志。
   *
   * @param query 文件预览日志查询条件
   * @return 文件预览日志列表
   */
  List<FilePreviewLog> findLogs(FilePreviewLogQuery query);
}
