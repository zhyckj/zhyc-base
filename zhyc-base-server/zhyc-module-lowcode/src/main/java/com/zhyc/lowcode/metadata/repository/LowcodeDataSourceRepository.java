/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;

import java.util.List;
import java.util.Optional;

/**
 * 低代码数据源仓储接口。
 */
public interface LowcodeDataSourceRepository {

  /**
   * 保存数据源定义。
   *
   * @param dataSource 数据源定义
   * @return 保存后的数据源定义
   */
  LowcodeDataSource save(LowcodeDataSource dataSource);

  /**
   * 按租户和数据源编码查找数据源。
   *
   * @param tenantId 租户业务编码
   * @param code 数据源编码
   * @return 匹配的数据源，不存在时返回空
   */
  Optional<LowcodeDataSource> findByTenantIdAndCode(String tenantId, String code);

  /**
   * 按租户和数据源主键查找数据源。
   *
   * @param tenantId 租户业务编码
   * @param id 数据源主键
   * @return 匹配的数据源，不存在时返回空
   */
  Optional<LowcodeDataSource> findByTenantIdAndId(String tenantId, Long id);

  /**
   * 按租户查询数据源列表。
   *
   * @param tenantId 租户业务编码
   * @return 租户内数据源列表
   */
  List<LowcodeDataSource> findByTenantId(String tenantId);
}
