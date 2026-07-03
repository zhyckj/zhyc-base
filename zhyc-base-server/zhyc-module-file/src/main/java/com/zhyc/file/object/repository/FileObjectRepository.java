/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.repository;

import com.zhyc.file.object.domain.FileObject;
import com.zhyc.file.object.service.FileObjectQuery;
import java.util.List;

/**
 * 文件对象仓储接口。
 */
public interface FileObjectRepository {

  /**
   * 统计文件对象数量。
   *
   * @param query 文件对象查询条件
   * @return 文件对象数量
   */
  long countByQuery(FileObjectQuery query);

  /**
   * 分页查询文件对象。
   *
   * @param query 文件对象查询条件
   * @param offset 分页偏移量
   * @return 文件对象列表
   */
  List<FileObject> findPageByQuery(FileObjectQuery query, int offset);

  /**
   * 保存文件对象。
   *
   * @param fileObject 文件对象
   */
  void save(FileObject fileObject);
}
