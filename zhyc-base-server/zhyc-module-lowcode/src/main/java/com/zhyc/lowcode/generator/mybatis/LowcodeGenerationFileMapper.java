/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.mybatis;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 低代码生成文件明细 MyBatis Mapper。
 */
@Mapper
public interface LowcodeGenerationFileMapper {

  /**
   * 新增生成文件明细。
   *
   * @param record 生成文件明细持久化对象
   * @return 影响行数
   */
  @InsertProvider(type = LowcodeGenerationFileSqlProvider.class, method = "insert")
  int insert(LowcodeGenerationFileRecord record);

  /**
   * 按租户和生成记录查询文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @return 生成文件明细列表
   */
  @SelectProvider(type = LowcodeGenerationFileSqlProvider.class, method = "selectByTenantIdAndRecordId")
  List<LowcodeGenerationFileRecord> selectByTenantIdAndRecordId(@Param("tenantId") String tenantId,
                                                                @Param("recordId") Long recordId);
}
