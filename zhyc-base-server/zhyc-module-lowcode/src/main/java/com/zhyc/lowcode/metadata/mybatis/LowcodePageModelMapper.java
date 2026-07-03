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
 * 低代码页面模型 MyBatis Mapper。
 */
@Mapper
public interface LowcodePageModelMapper {

  /**
   * 新增页面模型。
   *
   * @param record 页面模型持久化对象
   * @return 影响行数
   */
  @InsertProvider(type = LowcodePageModelSqlProvider.class, method = "insert")
  int insert(LowcodePageModelRecord record);

  /**
   * 按租户查询页面模型。
   *
   * @param tenantId 租户业务编码
   * @return 页面模型列表
   */
  @SelectProvider(type = LowcodePageModelSqlProvider.class, method = "selectByTenantId")
  List<LowcodePageModelRecord> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 按租户、表模型和页面类型更新。
   *
   * @param record 页面模型持久化对象
   * @return 影响行数
   */
  @UpdateProvider(type = LowcodePageModelSqlProvider.class, method = "updateByTenantTableAndType")
  int updateByTenantTableAndType(LowcodePageModelRecord record);
}
