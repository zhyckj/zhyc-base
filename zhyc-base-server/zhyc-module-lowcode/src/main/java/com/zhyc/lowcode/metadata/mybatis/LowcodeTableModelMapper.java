/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 低代码表模型 MyBatis Mapper。
 *
 * <p>负责表模型元数据读写，表模型编码必须在租户内唯一。</p>
 */
@Mapper
public interface LowcodeTableModelMapper {

  /**
   * 新增低代码表模型。
   *
   * @param record 表模型持久化记录
   * @return 影响行数
   */
  @InsertProvider(type = LowcodeTableModelSqlProvider.class, method = "insert")
  int insert(LowcodeTableModelRecord record);

  /**
   * 按租户和模型编码查询表模型。
   *
   * @param tenantId 租户业务编码
   * @param code 表模型编码
   * @return 匹配的表模型记录，不存在时返回 {@code null}
   */
  @SelectProvider(type = LowcodeTableModelSqlProvider.class, method = "selectByTenantIdAndCode")
  LowcodeTableModelRecord selectByTenantIdAndCode(@Param("tenantId") String tenantId,
                                                  @Param("code") String code);

  /**
   * 按租户和表模型主键查询表模型。
   *
   * @param tenantId 租户业务编码
   * @param id 表模型主键
   * @return 匹配的表模型记录，不存在时返回 {@code null}
   */
  @SelectProvider(type = LowcodeTableModelSqlProvider.class, method = "selectByTenantIdAndId")
  LowcodeTableModelRecord selectByTenantIdAndId(@Param("tenantId") String tenantId,
                                                @Param("id") Long id);

  /**
   * 按租户查询表模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 租户内表模型记录列表
   */
  @SelectProvider(type = LowcodeTableModelSqlProvider.class, method = "selectByTenantId")
  List<LowcodeTableModelRecord> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 按租户和模型编码更新表模型。
   *
   * @param record 表模型持久化记录
   * @return 影响行数
   */
  @UpdateProvider(type = LowcodeTableModelSqlProvider.class, method = "updateByTenantIdAndCode")
  int updateByTenantIdAndCode(LowcodeTableModelRecord record);

  /**
   * 按租户和模型编码更新模型状态。
   *
   * @param tenantId 租户业务编码
   * @param code 表模型编码
   * @param status 新模型状态编码
   * @return 影响行数
   */
  @UpdateProvider(type = LowcodeTableModelSqlProvider.class, method = "updateStatusByTenantIdAndCode")
  int updateStatusByTenantIdAndCode(@Param("tenantId") String tenantId,
                                    @Param("code") String code,
                                    @Param("status") String status);
}
