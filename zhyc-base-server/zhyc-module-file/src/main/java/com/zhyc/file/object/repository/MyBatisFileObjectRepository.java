/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.repository;

import com.zhyc.file.object.domain.FileObject;
import com.zhyc.file.object.mapper.FileObjectMapper;
import com.zhyc.file.object.service.FileObjectQuery;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的文件对象仓储实现。
 */
@Repository
public class MyBatisFileObjectRepository implements FileObjectRepository {

  /** 文件对象 Mapper。 */
  private final FileObjectMapper fileObjectMapper;

  /**
   * 创建文件对象仓储实现。
   *
   * @param fileObjectMapper 文件对象 Mapper
   */
  public MyBatisFileObjectRepository(FileObjectMapper fileObjectMapper) {
    this.fileObjectMapper = Objects.requireNonNull(fileObjectMapper, "文件对象 Mapper 不能为空");
  }

  @Override
  public long countByQuery(FileObjectQuery query) {
    return fileObjectMapper.countByQuery(query.tenantId(), query.keyword());
  }

  @Override
  public List<FileObject> findPageByQuery(FileObjectQuery query, int offset) {
    return fileObjectMapper.selectPageByQuery(query.tenantId(), query.keyword(), query.pageSize(), offset);
  }

  @Override
  public void save(FileObject fileObject) {
    fileObjectMapper.insert(fileObject);
  }
}
