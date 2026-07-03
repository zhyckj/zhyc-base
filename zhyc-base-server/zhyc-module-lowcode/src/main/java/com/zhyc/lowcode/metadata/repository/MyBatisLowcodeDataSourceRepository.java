/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import com.zhyc.lowcode.metadata.mybatis.LowcodeDataSourceMapper;
import com.zhyc.lowcode.metadata.mybatis.LowcodeDataSourceRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的低代码数据源仓储实现。
 *
 * <p>按租户和数据源编码执行幂等保存，真实数据库密码只通过密钥引用字段持久化。</p>
 */
@Repository
public class MyBatisLowcodeDataSourceRepository implements LowcodeDataSourceRepository {

  /** 低代码数据源 Mapper。 */
  private final LowcodeDataSourceMapper dataSourceMapper;

  /**
   * 创建 MyBatis 数据源仓储。
   *
   * @param dataSourceMapper 低代码数据源 Mapper
   */
  public MyBatisLowcodeDataSourceRepository(LowcodeDataSourceMapper dataSourceMapper) {
    this.dataSourceMapper = Objects.requireNonNull(dataSourceMapper, "低代码数据源 Mapper 不能为空");
  }

  @Override
  public LowcodeDataSource save(LowcodeDataSource dataSource) {
    Objects.requireNonNull(dataSource, "数据源定义不能为空");
    LowcodeDataSourceRecord record = toRecord(dataSource);
    LowcodeDataSourceRecord existing = dataSourceMapper.selectByTenantIdAndCode(dataSource.getTenantId(), dataSource.getCode());
    if (existing == null) {
      dataSourceMapper.insert(record);
    } else {
      dataSourceMapper.updateByTenantIdAndCode(record);
    }
    LowcodeDataSourceRecord persisted = dataSourceMapper.selectByTenantIdAndCode(dataSource.getTenantId(), dataSource.getCode());
    if (persisted == null) {
      throw new IllegalStateException("保存数据源后无法读取持久化记录: " + dataSource.getCode());
    }
    return toDomain(persisted);
  }

  @Override
  public Optional<LowcodeDataSource> findByTenantIdAndCode(String tenantId, String code) {
    return Optional.ofNullable(dataSourceMapper.selectByTenantIdAndCode(tenantId, code))
        .map(this::toDomain);
  }

  @Override
  public Optional<LowcodeDataSource> findByTenantIdAndId(String tenantId, Long id) {
    return Optional.ofNullable(dataSourceMapper.selectByTenantIdAndId(tenantId, id))
        .map(this::toDomain);
  }

  @Override
  public List<LowcodeDataSource> findByTenantId(String tenantId) {
    return dataSourceMapper.selectByTenantId(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  private LowcodeDataSourceRecord toRecord(LowcodeDataSource dataSource) {
    return new LowcodeDataSourceRecord(
        dataSource.getId(),
        dataSource.getTenantId(),
        dataSource.getCode(),
        dataSource.getName(),
        dataSource.getDialect().getCode(),
        dataSource.getJdbcUrl(),
        dataSource.getUsername(),
        dataSource.getPasswordSecretRef(),
        dataSource.isEnabled());
  }

  private LowcodeDataSource toDomain(LowcodeDataSourceRecord record) {
    return new LowcodeDataSource(
        record.id(),
        record.tenantId(),
        record.code(),
        record.name(),
        LowcodeDatabaseDialect.fromCode(record.dialect()),
        record.jdbcUrl(),
        record.username(),
        record.passwordSecretRef(),
        Boolean.TRUE.equals(record.enabled()));
  }
}
