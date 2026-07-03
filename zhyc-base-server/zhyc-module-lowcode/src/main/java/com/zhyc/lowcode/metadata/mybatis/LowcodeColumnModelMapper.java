/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 低代码字段模型 MyBatis Mapper。
 *
 * <p>字段模型按表模型整体重建，调用方应在同一事务内先删除旧字段再插入新字段。</p>
 */
@Mapper
public interface LowcodeColumnModelMapper {

  /**
   * 新增低代码字段模型。
   *
   * @param record 字段模型持久化记录
   * @return 影响行数
   */
  @InsertProvider(type = LowcodeColumnModelSqlProvider.class, method = "insert")
  int insert(LowcodeColumnModelRecord record);

  /**
   * 删除指定租户表模型下的全部字段。
   *
   * @param tenantId 租户业务编码
   * @param tableModelId 表模型主键
   * @return 影响行数
   */
  @DeleteProvider(type = LowcodeColumnModelSqlProvider.class, method = "deleteByTenantIdAndTableModelId")
  int deleteByTenantIdAndTableModelId(@Param("tenantId") String tenantId, @Param("tableModelId") Long tableModelId);

  /**
   * 查询指定租户表模型下的字段列表。
   *
   * @param tenantId 租户业务编码
   * @param tableModelId 表模型主键
   * @return 按排序号和主键排序的字段记录
   */
  @SelectProvider(type = LowcodeColumnModelSqlProvider.class, method = "selectByTenantIdAndTableModelId")
  List<LowcodeColumnModelRecord> selectByTenantIdAndTableModelId(@Param("tenantId") String tenantId,
                                                                 @Param("tableModelId") Long tableModelId);
}
