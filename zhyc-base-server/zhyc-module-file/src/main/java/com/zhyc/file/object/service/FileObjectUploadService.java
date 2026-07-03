/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

/**
 * 文件对象上传服务。
 */
public interface FileObjectUploadService {

  /**
   * 上传文件并登记文件对象元数据。
   *
   * @param command 文件对象上传命令
   * @return 文件对象上传响应
   */
  FileObjectUploadResponse upload(FileObjectUploadCommand command);
}
