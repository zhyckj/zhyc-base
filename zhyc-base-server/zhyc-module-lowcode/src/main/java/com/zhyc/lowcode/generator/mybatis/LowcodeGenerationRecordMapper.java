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
 * 低代码生成记录 MyBatis Mapper。
 *
 * <p>负责保存和查询代码生成审计记录，查询必须按租户编码隔离。</p>
 */
@Mapper
public interface LowcodeGenerationRecordMapper {

  /**
   * 新增生成记录。
   *
   * @param record 生成记录持久化对象
   * @return 影响行数
   */
  @InsertProvider(type = LowcodeGenerationRecordSqlProvider.class, method = "insert")
  int insert(LowcodeGenerationRecordRecord record);

  /**
   * 按租户查询生成记录列表。
   *
   * @param tenantId 租户业务编码
   * @return 租户内生成记录列表
   */
  @SelectProvider(type = LowcodeGenerationRecordSqlProvider.class, method = "selectByTenantId")
  List<LowcodeGenerationRecordRecord> selectByTenantId(@Param("tenantId") String tenantId);
}
