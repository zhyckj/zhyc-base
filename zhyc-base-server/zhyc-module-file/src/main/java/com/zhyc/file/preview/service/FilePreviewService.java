/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.service;

import java.util.List;

/**
 * 文件预览业务服务。
 */
public interface FilePreviewService {

  /**
   * 创建文件预览并记录预览日志。
   *
   * @param command 文件预览创建命令
   * @return 文件预览响应
   */
  FilePreviewResponse createPreview(FilePreviewCreateCommand command);

  /**
   * 渲染文件预览并记录预览日志。
   *
   * @param tenantId 租户业务编码
   * @param fileCode 文件业务编码
   * @param previewType 预览类型
   * @return 文件预览渲染响应
   */
  FilePreviewRenderResponse renderPreview(String tenantId, String fileCode, String previewType);

  /**
   * 查询文件预览日志。
   *
   * @param query 文件预览日志查询条件
   * @return 文件预览日志列表
   */
  List<FilePreviewLogResponse> listPreviewLogs(FilePreviewLogQuery query);
}
