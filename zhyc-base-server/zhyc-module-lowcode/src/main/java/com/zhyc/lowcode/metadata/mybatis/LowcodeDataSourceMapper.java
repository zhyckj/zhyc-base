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
 * 低代码数据源 MyBatis Mapper。
 *
 * <p>负责数据源元数据读写，所有查询和更新必须按租户编码隔离。</p>
 */
@Mapper
public interface LowcodeDataSourceMapper {

  /**
   * 新增低代码数据源。
   *
   * @param record 数据源持久化记录
   * @return 影响行数
   */
  @InsertProvider(type = LowcodeDataSourceSqlProvider.class, method = "insert")
  int insert(LowcodeDataSourceRecord record);

  /**
   * 按租户和数据源编码查询数据源。
   *
   * @param tenantId 租户业务编码
   * @param code 数据源编码
   * @return 匹配的数据源记录，不存在时返回 {@code null}
   */
  @SelectProvider(type = LowcodeDataSourceSqlProvider.class, method = "selectByTenantIdAndCode")
  LowcodeDataSourceRecord selectByTenantIdAndCode(@Param("tenantId") String tenantId,
                                                  @Param("code") String code);

  /**
   * 按租户和数据源主键查询数据源。
   *
   * @param tenantId 租户业务编码
   * @param id 数据源主键
   * @return 匹配的数据源记录，不存在时返回 {@code null}
   */
  @SelectProvider(type = LowcodeDataSourceSqlProvider.class, method = "selectByTenantIdAndId")
  LowcodeDataSourceRecord selectByTenantIdAndId(@Param("tenantId") String tenantId,
                                                @Param("id") Long id);

  /**
   * 按租户查询数据源列表。
   *
   * @param tenantId 租户业务编码
   * @return 租户内数据源记录列表
   */
  @SelectProvider(type = LowcodeDataSourceSqlProvider.class, method = "selectByTenantId")
  List<LowcodeDataSourceRecord> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 按租户和数据源编码更新数据源。
   *
   * @param record 数据源持久化记录
   * @return 影响行数
   */
  @UpdateProvider(type = LowcodeDataSourceSqlProvider.class, method = "updateByTenantIdAndCode")
  int updateByTenantIdAndCode(LowcodeDataSourceRecord record);
}
