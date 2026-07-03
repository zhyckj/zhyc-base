/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.mapper;

import com.zhyc.file.object.domain.FileObject;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 文件对象 MyBatis Mapper。
 */
@Mapper
public interface FileObjectMapper {

  /**
   * 统计文件对象数量。
   *
   * @param tenantId 租户业务编码
   * @param keyword 文件名关键词
   * @return 文件对象数量
   */
  @SelectProvider(type = FileObjectSqlProvider.class, method = "countByQuery")
  long countByQuery(@Param("tenantId") String tenantId, @Param("keyword") String keyword);

  /**
   * 分页查询文件对象。
   *
   * @param tenantId 租户业务编码
   * @param keyword 文件名关键词
   * @param pageSize 每页记录数
   * @param offset 分页偏移量
   * @return 文件对象列表
   */
  @SelectProvider(type = FileObjectSqlProvider.class, method = "selectPageByQuery")
  List<FileObject> selectPageByQuery(@Param("tenantId") String tenantId, @Param("keyword") String keyword,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  /**
   * 写入文件对象。
   *
   * @param fileObject 文件对象
   */
  @InsertProvider(type = FileObjectSqlProvider.class, method = "insert")
  void insert(FileObject fileObject);
}
