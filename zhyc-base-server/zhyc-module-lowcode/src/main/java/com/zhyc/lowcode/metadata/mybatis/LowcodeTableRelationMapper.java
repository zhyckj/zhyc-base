/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 低代码表关系 MyBatis Mapper。
 */
@Mapper
public interface LowcodeTableRelationMapper {

  /**
   * 新增表关系。
   *
   * @param record 表关系持久化对象
   * @return 影响行数
   */
  @InsertProvider(type = LowcodeTableRelationSqlProvider.class, method = "insert")
  int insert(LowcodeTableRelationRecord record);

  /**
   * 按租户查询表关系。
   *
   * @param tenantId 租户业务编码
   * @return 表关系列表
   */
  @SelectProvider(type = LowcodeTableRelationSqlProvider.class, method = "selectByTenantId")
  List<LowcodeTableRelationRecord> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 按租户和表关系唯一键更新。
   *
   * @param record 表关系持久化对象
   * @return 影响行数
   */
  @UpdateProvider(type = LowcodeTableRelationSqlProvider.class, method = "updateByTenantAndTables")
  int updateByTenantAndTables(LowcodeTableRelationRecord record);
}
