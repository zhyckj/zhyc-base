/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.service;

import com.zhyc.common.api.PageResult;

/**
 * 文件对象业务服务。
 */
public interface FileObjectService {

  /**
   * 登记文件对象元数据。
   *
   * @param command 文件对象登记命令
   * @return 文件业务编码
   */
  String register(FileObjectRegisterCommand command);

  /**
   * 分页查询文件对象。
   *
   * @param query 文件对象查询条件
   * @return 文件对象分页响应
   */
  PageResult<FileObjectResponse> listFiles(FileObjectQuery query);
}
